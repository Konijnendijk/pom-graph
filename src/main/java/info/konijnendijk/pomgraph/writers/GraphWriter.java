package info.konijnendijk.pomgraph.writers;

/**
 * Interface to be implemented by classes writing a POM graph to a file
 *
 * @author Geert Konijnendijk
 */
public interface GraphWriter extends AutoCloseable {

    /**
     * Add a node to the graph. It is allowed to call this method multiple times with the same node.
     * @param id The node's ID
     */
    void addNode(String id) throws Exception;

    /**
     * Add an edge to the graph. It is not allowed to call this method multiple times for the same edge.
     * @param projectId The source node
     * @param dependencyIDs The target nodes
     */
    void addEdge(String projectId, String... dependencyIDs) throws Exception;


}
