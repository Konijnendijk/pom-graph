package nl.konijnendijk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads all POM files in a directory and creates a graph from their dependencies
 */
public class PomGraph {

    private static final Logger logger = LogManager.getLogger(PomGraph.class);

    private final MavenXpp3Reader pomReader;
    private final BufferedWriter csvWriter;

    private final boolean includeVersion = false;

    public PomGraph(Path outputCSV) throws IOException {
        pomReader = new MavenXpp3Reader();
        csvWriter = Files.newBufferedWriter(outputCSV);
    }

    public void parsePomFiles(Path directory) {
        try {
            Files.find(directory, Integer.MAX_VALUE, (file, attributes) -> file.toString().endsWith(".pom")).forEach(this::readPom);
        } catch (IOException e) {
            logger.error("Could not iterate over files", e);
        }
    }

    public void readPom(Path pomFile) {
        logger.info("Parsing " + pomFile);
        try {
            Model model = pomReader.read(Files.newInputStream(pomFile));
            model.setPomFile(pomFile.toFile());
            MavenProject project = new MavenProject(model);
            addNode(project);
        } catch (IOException e) {
            logger.error("Could not read POM file", e);
        } catch (XmlPullParserException e) {
            logger.error("Could not parse POM file", e);
        }
    }

    private void addNode(MavenProject project) {
        String nodeId = buildNodeIdentifier(project);

        String dependencyIds = String.join(";", project.getDependencies()
                .stream()
                .map(this::buildNodeIdentifier)
                .toArray(l -> new String[l]));

        try {
            if ("".equals(dependencyIds)) {
                csvWriter.write(nodeId);
            }
            else {
                csvWriter.write(String.join(";", nodeId, dependencyIds));
            }
            csvWriter.newLine();
        } catch (IOException e) {
            logger.error("Could not write to CSV file", e);
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

    private String buildNodeIdentifier (String groupId, String artifactId) {
        return String.format("%s:%s", groupId, artifactId);
    }

    private String buildNodeIdentifier (String groupId, String artifactId, String version) {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    public void close() throws IOException {
        csvWriter.close();
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            logger.error("Usage: PomGraph <directory to scan> <CSV output file>");
            System.exit(0);
        }
        try {
            PomGraph pomGraph = new PomGraph(Paths.get(args[1]));
            pomGraph.parsePomFiles(Paths.get(args[0]));
            pomGraph.close();
        } catch (IOException e) {
            logger.error("Could not create CSV file", e);
        }
    }

}
