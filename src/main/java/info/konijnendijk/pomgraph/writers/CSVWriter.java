package info.konijnendijk.pomgraph.writers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * Writes an adjacency list in CSV format, which should be readable by programs like Gephi.
 *
 * The resulting file will contain a line for each {@lonk CSVWriter#addEdge} call.
 *
 * @author Geert Konijnendijk
 */
public class CSVWriter implements GraphWriter {

    private final BufferedWriter csvWriter;

    public CSVWriter(Path outputFile) throws IOException {
        csvWriter = Files.newBufferedWriter(outputFile);
    }

    @Override
    public void addNode(String id) {

    }

    @Override
    public void addEdge(String projectId, String... dependencyIDs) throws Exception {
        if (dependencyIDs.length==0) {
            csvWriter.write(projectId);
        } else {
            List<String> columns = Arrays.asList(dependencyIDs);
            columns.add(0, projectId);
            csvWriter.write(String.join(";", columns));
        }
        csvWriter.newLine();
    }

    @Override
    public void close() throws Exception {
        csvWriter.close();
    }
}
