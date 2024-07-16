package tum.dpid.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallGraphVisualizer {
    public static void drawCallGraph(String methodName, Map<String, List<String>> callGraph, int level, Set<String> visited) {
        if (!visited.add(methodName)) {
            return;
        }
        printIndented(methodName, level);
        List<String> callers = callGraph.get(methodName);
        if (callers != null) {
            for (String caller : callers) {
                drawCallGraph(caller, callGraph, level + 1, visited);
            }
        }
    }

    private static void printIndented(String methodName, int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("---- ");
        }
        System.out.println(methodName);
    }
}
