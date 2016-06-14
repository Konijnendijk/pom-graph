package nl.konijnendijk.writers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Writes a POM document in GraphML format.
 *
 * @author Geert Konijnendijk
 */
public class GraphMLWriter implements GraphWriter {

    private final Document document;
    private final Element graph;
    private final Path outputFile;

    private final Set<String> nodes;

    public GraphMLWriter(Path outputFile) throws MalformedURLException, SAXException, ParserConfigurationException {
        this.outputFile = outputFile;
        this.nodes = new HashSet<>();

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new URL("http://graphml.graphdrawing.org/xmlns/1.0/graphml-structure.xsd"));

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setSchema(schema);
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        document = documentBuilder.newDocument();

        Element root = document.createElementNS("http://graphml.graphdrawing.org/xmlns", "graphml");
        document.appendChild(root);
        graph = document.createElementNS("http://graphml.graphdrawing.org/xmlns", "graph");
        graph.setAttribute("edgedefault", "directed");
        root.appendChild(graph);
    }

    @Override
    public void addNode(String id) throws Exception {
        if (!nodes.contains(id)) {
            Element node = document.createElementNS("http://graphml.graphdrawing.org/xmlns", "node");
            node.setAttribute("id", id);
            graph.appendChild(node);
            nodes.add(id);
        }
    }

    @Override
    public void addEdge(String projectId, String... dependencyIDs) throws Exception {
        for (String dep : dependencyIDs) {
            addNode(dep);

            Element edge = document.createElementNS("http://graphml.graphdrawing.org/xmlns", "edge");
            edge.setAttribute("source", projectId);
            edge.setAttribute("target", dep);
            graph.appendChild(edge);
        }
    }

    @Override
    public void close() throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource source = new DOMSource(document);
        OutputStream stream = new FileOutputStream(outputFile.toFile());
        StreamResult result = new StreamResult(stream);
        transformer.transform(source, result);
        stream.close();
    }
}
