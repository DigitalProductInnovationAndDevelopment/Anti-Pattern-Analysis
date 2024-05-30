package tum.dpid;

import org.eclipse.jdt.core.dom.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
public class CallChainAnalyzer {

    private static final List<String> DB_METHODS = List.of("flush", "save", "update", "persist", "remove", "find");

    public static void main(String[] args) {
        String projectDirectoryPath = "/Users/melisuensal/Desktop/LoopAntiPattern/src/main/java/com/example/LoopAntiPattern";

        File projectDirectory = new File(projectDirectoryPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        Map<String, MethodDeclaration> methodMap = collectMethods(projectDirectory);
        Map<String, Set<String>> callGraph = buildCallGraph(methodMap);

        for (String targetMethod : DB_METHODS) {
            System.out.println("Call chain for method: " + targetMethod);
            Set<String> visited = new HashSet<>();
            List<String> callChain = new ArrayList<>();
            traceCallChain(targetMethod, callGraph, visited, callChain);

            if (callChain.isEmpty()) {
                System.out.println("No calls found for method: " + targetMethod);
            } else {
                for (String method : callChain) {
                    System.out.println(method);
                }
            }
            System.out.println();
        }
    }

    private static Map<String, MethodDeclaration> collectMethods(File projectDirectory) {
        Map<String, MethodDeclaration> methodMap = new HashMap<>();
        List<File> javaFiles = getJavaFiles(projectDirectory);

        if (javaFiles != null) {
            for (File javaFile : javaFiles) {
                try {
                    String source = new String(Files.readAllBytes(javaFile.toPath()));
                    ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
                    parser.setSource(source.toCharArray());
                    parser.setKind(ASTParser.K_COMPILATION_UNIT);

                    CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                    cu.accept(new MethodCollector(methodMap));
                } catch (IOException e) {
                    System.err.println("Error reading file: " + javaFile.getName());
                    e.printStackTrace();
                }
            }
        }

        return methodMap;
    }

    private static List<File> getJavaFiles(File directory) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(getJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }

        return javaFiles;
    }

    private static Map<String, Set<String>> buildCallGraph(Map<String, MethodDeclaration> methodMap) {
        Map<String, Set<String>> callGraph = new HashMap<>();

        for (MethodDeclaration method : methodMap.values()) {
            method.accept(new ASTVisitor() {
                @Override
                public boolean visit(MethodInvocation node) {
                    String caller = method.getName().toString();
                    String callee = node.getName().toString();
                    callGraph.computeIfAbsent(callee, k -> new HashSet<>()).add(caller);
                    return super.visit(node);
                }
            });
        }

        return callGraph;
    }

    private static void traceCallChain(String methodName, Map<String, Set<String>> callGraph, Set<String> visited, List<String> callChain) {
        if (!visited.add(methodName)) {
            return;
        }
        callChain.add(methodName);
        Set<String> callers = callGraph.get(methodName);
        if (callers != null) {
            for (String caller : callers) {
                traceCallChain(caller, callGraph, visited, callChain);
            }
        }
    }

    static class MethodCollector extends ASTVisitor {
        private final Map<String, MethodDeclaration> methodMap;

        MethodCollector(Map<String, MethodDeclaration> methodMap) {
            this.methodMap = methodMap;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            methodMap.put(node.getName().toString(), node);
            return super.visit(node);
        }
    }
}
