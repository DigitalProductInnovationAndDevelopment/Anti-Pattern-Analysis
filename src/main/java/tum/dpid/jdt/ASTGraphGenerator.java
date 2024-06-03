package tum.dpid.jdt;

import org.eclipse.jdt.core.dom.*;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultDirectedGraph;

public class ASTGraphGenerator {

    private Graph<String, DefaultEdge> graph;

    public ASTGraphGenerator() {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    /* Experimental graph creation, edge should be checked */
    public void addClassToGraph(CompilationUnit cu) {
        graph.addVertex(cu.toString());
        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(TypeDeclaration node) {
                String className = node.getName().getIdentifier();
                graph.addVertex(className);
                return super.visit(node);
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                String methodName = node.getName().getIdentifier();
                graph.addVertex(methodName);
                // Assuming the parent node is a TypeDeclaration (class)
                String className = ((TypeDeclaration) node.getParent()).getName().getIdentifier();
                graph.addEdge(methodName, className);
                return super.visit(node);
            }

            @Override
            public boolean visit(FieldDeclaration node) {
                for (Object fragment : node.fragments()) {
                    VariableDeclarationFragment varFragment = (VariableDeclarationFragment) fragment;
                    String fieldName = varFragment.getName().getIdentifier();
                    graph.addVertex(fieldName);
                    String className = ((TypeDeclaration) node.getParent()).getName().getIdentifier();
                    graph.addEdge(fieldName, className);
                }
                return super.visit(node);
            }
        });
    }
    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }

}
