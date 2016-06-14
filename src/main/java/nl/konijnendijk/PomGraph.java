package nl.konijnendijk;

import nl.konijnendijk.writers.CSVWriter;
import nl.konijnendijk.writers.GraphMLWriter;
import nl.konijnendijk.writers.GraphWriter;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads all POM files in a directory and creates a graph from their dependencies.
 * Nodes have labels in the form <i>groupID:artifactID:versionID</i> or <i>groupID:artifactID</i>.
 * In the latter case all versions with the same groupID and artifactID will be merged into one node.
 *
 * @author Geert Konijnendijk
 */
public class PomGraph {

    private static final Logger logger = LogManager.getLogger(PomGraph.class);

    private final MavenXpp3Reader pomReader;
    private final GraphWriter writer;

    private final boolean includeVersion;

    /**
     * @param writer Determines the format to write to
     */
    public PomGraph(GraphWriter writer) {
        this(writer, false);
    }

    /**
     * @param writer         Determines the format to write t
     * @param includeVersion Whether to format node labels as <i>groupID:artifactID:versionID</i> or <i>groupID:artifactID</i>
     */
    public PomGraph(GraphWriter writer, boolean includeVersion) {
        this.includeVersion = includeVersion;
        pomReader = new MavenXpp3Reader();
        this.writer = writer;
    }

    /**
     * Read all POM files in a directory recursively and add them to the graph.
     *
     * @param directory The directory containing pom files
     */
    public void parsePomFiles(Path directory) {
        try {
            Files.find(directory, Integer.MAX_VALUE, (file, attributes) -> file.toString().endsWith(".pom") || "pom.xml".equals(file.getFileName().toString())).forEach(this::readPom);
        } catch (IOException e) {
            logger.error("Could not iterate over files", e);
        }
    }

    /**
     * Read a single POM file and add it to the graph.
     *
     * @param pomFile
     */
    public void readPom(Path pomFile) {
        logger.debug("Parsing " + pomFile);
        try {
            Model model = pomReader.read(Files.newInputStream(pomFile));
            model.setPomFile(pomFile.toFile());
            MavenProject project = new MavenProject(model);
            addNode(project);
        } catch (Exception e) {
            logger.error("Could not parse POM file " + pomFile, e);
        }
    }

    /**
     * Add a parsed POM to the graph
     *
     * @param project The project parsed from a POM file.
     */
    private void addNode(MavenProject project) {
        String nodeId = buildNodeIdentifier(project);


        String[] dependencyIDs = project.getDependencies()
                .stream()
                .map(this::buildNodeIdentifier).toArray(s -> new String[s]);

        try {
            writer.addNode(nodeId);
            writer.addEdge(nodeId, dependencyIDs);
        } catch (Exception e) {
            logger.error("Could not write to file", e);
        }
    }

    private String buildNodeIdentifier(MavenProject project) {
        if (includeVersion)
            return buildNodeIdentifier(project.getGroupId(), project.getArtifactId(), project.getVersion());
        else
            return buildNodeIdentifier(project.getGroupId(), project.getArtifactId());
    }

    private String buildNodeIdentifier(Dependency dependency) {
        if (includeVersion)
            return buildNodeIdentifier(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
        else
            return buildNodeIdentifier(dependency.getGroupId(), dependency.getArtifactId());
    }

    private String buildNodeIdentifier(String groupId, String artifactId) {
        return String.format("%s:%s", groupId, artifactId);
    }

    private String buildNodeIdentifier(String groupId, String artifactId, String version) {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    public void close() throws Exception {
        writer.close();
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "Prints this message");
        options.addOption("f", "format", true, "Output format, can be \"csv\" (adjacency list) or \"graphml\", required");
        options.addOption("d", "directory", true, "Directory that will be scanned recursively for POM files, required");
        options.addOption("o", "output", true, "Output file, required");
        options.addOption("i", "include-version", false, "Whether to create a separate node for each version of an artifact, or merge them all into one node");

        HelpFormatter formatter = new HelpFormatter();

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            formatter.printHelp("pom-graph", options);
            return;
        }

        String format = cmd.getOptionValue("format");
        if (cmd.hasOption("help") || !cmd.hasOption("format") || !cmd.hasOption("output") || !cmd.hasOption("directory")
                || (!"graphml".equals(format) && !"csv".equals(format))) {
            formatter.printHelp("pom-graph", options);
            return;
        }

        try {
            GraphWriter writer;
            Path outputFile = Paths.get(cmd.getOptionValue("output"));
            if ("graphml".equals(format))
                writer = new GraphMLWriter(outputFile);
            else
                writer = new CSVWriter(outputFile);
            PomGraph pomGraph = new PomGraph(writer, cmd.hasOption("include-version"));
            pomGraph.parsePomFiles(Paths.get(cmd.getOptionValue("directory")));
            pomGraph.close();
        } catch (Exception e) {
            logger.error("Could not create graph", e);
        }
    }

}
