package tum.dpid.graph;

import tum.dpid.model.MethodDeclarationWrapper;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CallGraphVisualizer {
    public static void drawCallGraph(String methodName, Map<String, List<String>> callGraph, int level, Set<String> visited, Map<String, MethodDeclarationWrapper> methodMap) {
        MethodDeclarationWrapper wrapper = methodMap.get(methodName);
        if(Objects.isNull(wrapper) || wrapper.isExcluded() || visited.contains(methodName) ) {
            return;
        }
        visited.add(methodName);
        printIndented(methodName, level);
        List<String> callers = callGraph.get(methodName);
        if (callers != null) {
            for (String caller : callers) {
                drawCallGraph(caller, callGraph, level + 1, visited, methodMap);
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