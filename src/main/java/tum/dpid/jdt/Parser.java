package tum.dpid.jdt;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.util.mxCellRenderer;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import tum.dpid.util.FileReader;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

//import guru.nidi.graphviz.engine.Format;
//import guru.nidi.graphviz.engine.Graphviz;
//import guru.nidi.graphviz.model.Node;
//import static guru.nidi.graphviz.model.Factory.graph;
//import static guru.nidi.graphviz.model.Factory.node;


public class Parser {

    /**
     * Recursively read files in the directory and create AST then visit nodes
     */
    private static ASTGraphGenerator generator = new ASTGraphGenerator();

    public static void processJavaFiles(File directory) {

        if (directory == null || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processJavaFiles(file);
            } else if (file.getName().endsWith(".java")) {
                System.out.println("Parsing file "+ file.getName());

                CompilationUnit cu = parseSourceCode(file);
//                System.out.println(cu);
//                System.out.println("**************************");
                assert cu != null;
                //Compilation Unit: The source range for this type of node is ordinarily the entire source file, including leading and trailing whitespace and comments.
                //System.out.println("Compilation Unit: " + cu.toString());

                generator.addClassToGraph(cu);
                cu.accept(new DeclarationVisitor());
            }
        }
    }

    public static void visualiseGraph() throws IOException {
        // Visualize the combined graph
        // Visualisation with JGraphXAdapter, it is using gui library therefore it is big library and slow
        Graph<String, DefaultEdge> graph = generator.getGraph();
        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<String, DefaultEdge>(graph);
        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());
        File imgFile = new File("src/main/resources/graph1.png");
        BufferedImage image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2, Color.WHITE, true, null);
        ImageIO.write(image, "PNG", imgFile);

        /* Lightweight graph visualisation library GraphViz*/
//        Map<String, Node> nodeMap = new HashMap<>();
//        System.out.println("graph.vertexSet() " + graph.vertexSet());
//        for (String className : graph.vertexSet()) {
//            nodeMap.put(className, node(className));
//        }
//
//        guru.nidi.graphviz.model.Graph g = graph("example");
//        for (DefaultEdge edge : graph.edgeSet()) {
//            String source = graph.getEdgeSource(edge);
//            String target = graph.getEdgeTarget(edge);
//            g = g.with(nodeMap.get(source).link(nodeMap.get(target)));
//        }
//
//        Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("src/main/resources/example.png"));
    }
    /**
     *
     * @param file Source code file
     * @return CompilationUnit of file (root of AST)
     */
    public static CompilationUnit parseSourceCode(File file) {
        String sourceCode = FileReader.readSourceFromFile(file);
        if (sourceCode == null) {
            return null;
        }

        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);

        parser.setBindingsRecovery(true);
        //parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setUnitName("Program.java");

        String[] classPathEntries = {};
        String[] sourcePathEntries = {"src"};

        parser.setEnvironment(classPathEntries, sourcePathEntries, new String[]{"UTF-8"}, true);

        parser.setSource(sourceCode.toCharArray());
        return (CompilationUnit) parser.createAST(null);
    }
}
