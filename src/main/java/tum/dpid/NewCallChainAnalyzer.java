package tum.dpid;

import org.eclipse.jdt.core.dom.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class NewCallChainAnalyzer {

    private static final List<String> DB_METHODS = List.of("flush", "save", "update", "persist", "remove", "find", "selectById", "selectExistingById", "delete", "selectByArticleNumber", "saveWithoutFlush", "merge", "selectFrom");

    public static void main(String[] args) {
        String projectDirectoryPath = "../fromItestra/LoopAntiPattern";

        File projectDirectory = new File(projectDirectoryPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        Map<String, MethodDeclaration> methodMap = collectMethods(projectDirectory);
        Map<String, Set<String>> callGraph = buildCallGraph(methodMap);

        for (String dbMethod : DB_METHODS) {
            System.out.println("Finding call chains for database method: " + dbMethod);
            Set<String> visited = new HashSet<>();
            List<String> callChain = new ArrayList<>();
            traceCallChainsToMethod(dbMethod, callGraph, new HashSet<>(), callChain, methodMap);
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
                    callGraph.computeIfAbsent(caller, k -> new HashSet<>()).add(callee);
                    return super.visit(node);
                }
            });
        }

        return callGraph;
    }

    private static void traceCallChainsToMethod(String targetMethod, Map<String, Set<String>> callGraph, Set<String> visited, List<String> callChain, Map<String, MethodDeclaration> methodMap) {
        if (!visited.add(targetMethod)) {
            return;
        }
        callChain.add(targetMethod);
        Set<String> callees = callGraph.get(targetMethod);
        if (callees != null) {
            for (String callee : callees) {
                List<String> newCallChain = new ArrayList<>(callChain);
                traceCallChainsToMethod(callee, callGraph, visited, newCallChain, methodMap);
            }
        } else {
            if (DB_METHODS.contains(targetMethod)) {
                System.out.println("Database method call chain: " + String.join(" -> ", callChain));
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

