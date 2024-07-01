package tum.dpid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jdt.core.dom.*;
import tum.dpid.config.AnalyzerConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CallChainAnalyzer {

    private static final List<String> DB_METHODS = List.of("flush", "save", "update", "persist", "remove", "find",
            "selectById", "selectExistingById", "delete",
            "selectByArticleNumber", "saveWithoutFlush", "merge", "selectFrom", "callSave");

    public static void main(String[] args) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        AnalyzerConfig config;

        try {
            config = mapper.readValue(new File("config.json"), AnalyzerConfig.class);
        } catch (IOException e) {
            System.out.println("Error reading config file:\n" + e.getMessage());
            System.exit(1);
            return;
        }

        String directoryPath = config.getRepositoryDirectory();
        Path dirPath = Paths.get(directoryPath);
        List<String> dbMethods = new ArrayList<>(DB_METHODS);

        try {
            List<Path> javaFiles = Files.walk(dirPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();

            for (Path javaFile : javaFiles) {
                dbMethods.addAll(extractMethodNames(javaFile, config.getExcludedClasses(), config.getExcludedMethods()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String projectDirectoryPath = config.getProjectDirectory();

        File projectDirectory = new File(projectDirectoryPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.err.println("[ERROR] Invalid project directory path.");
            return;
        }

        Map<String, MethodDeclaration> methodMap = collectMethods(projectDirectory, config.getExcludedClasses(), config.getExcludedMethods());
        Map<String, Set<String>> callGraph = buildCallGraph(methodMap);

        for (String targetMethod : dbMethods) {
            System.out.println("\nCall graph for method: " + targetMethod);
            drawCallGraph(targetMethod, callGraph, 0, new HashSet<>());
        }

        checkForDatabaseCallsInLoops(methodMap, callGraph);
    }

    private static List<String> extractMethodNames(Path javaFilePath, List<String> excludedClasses, List<String> excludedMethods) throws IOException {
        String className = javaFilePath.getFileName().toString();
        if (excludedClasses.contains(className)) {
            return Collections.emptyList();
        }
        String content = new String(Files.readAllBytes(javaFilePath));
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(content.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        MethodNameVisitor visitor = new MethodNameVisitor(excludedMethods);
        cu.accept(visitor);
        System.out.println("New db methods found" + visitor.getMethodNames());
        return visitor.getMethodNames();
    }

    private static Map<String, MethodDeclaration> collectMethods(File projectDirectory, List<String> excludedClasses, List<String> excludedMethods) {
        Map<String, MethodDeclaration> methodMap = new HashMap<>();
        List<File> javaFiles = getJavaFiles(projectDirectory);

        if (javaFiles != null) {
            for (File javaFile : javaFiles) {
                String className = javaFile.getName();
                if (!excludedClasses.contains(className)) {
                    try {
                        String source = new String(Files.readAllBytes(javaFile.toPath()));
                        ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
                        parser.setSource(source.toCharArray());
                        parser.setKind(ASTParser.K_COMPILATION_UNIT);

                        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                        cu.accept(new MethodCollector(methodMap, excludedMethods));
                    } catch (IOException e) {
                        System.err.println("[ERROR] Error reading file: " + javaFile.getName());
                        e.printStackTrace();
                    }
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

    private static void drawCallGraph(String methodName, Map<String, Set<String>> callGraph, int level, Set<String> visited) {
        if (!visited.add(methodName)) {
            return;
        }
        printIndented(methodName, level);
        Set<String> callers = callGraph.get(methodName);
        if (callers != null) {
            for (String caller : callers) {
                drawCallGraph(caller, callGraph, level + 1, visited);
            }
        }
    }

    private static void printIndented(String methodName, int level) {
        for (int i = 0; i < level; i++) {
            System.out.print("    ");
        }
        System.out.println(methodName);
    }

    private static void checkForDatabaseCallsInLoops(Map<String, MethodDeclaration> methodMap, Map<String, Set<String>> callGraph) {
        for (MethodDeclaration method : methodMap.values()) {
            method.accept(new ASTVisitor() {
                @Override
                public boolean visit(ForStatement node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(WhileStatement node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(DoStatement node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(EnhancedForStatement node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(LambdaExpression node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(MethodInvocation node) {
                    // Check for stream method calls like map, filter, etc.
                    if (node.getName().getIdentifier().equals("map") || node.getName().getIdentifier().equals("forEach") || node.getName().getIdentifier().equals("stream")) {
                        node.accept(new ASTVisitor() {
                            @Override
                            public boolean visit(LambdaExpression lambdaExpression) {
                                checkMethodInvocationsInLoop(lambdaExpression.getBody(), methodMap, callGraph);
                                return super.visit(lambdaExpression);
                            }

                            @Override
                            public boolean visit(MethodInvocation innerNode) {
                                String methodName = innerNode.getName().getIdentifier();
                                if (DB_METHODS.contains(methodName)) {
                                    reportAntiPattern(innerNode);
                                } else if (methodMap.containsKey(methodName)) {
                                    traceMethodCallsInLoop(methodName, methodMap, callGraph, new HashSet<>(), innerNode);
                                }
                                return super.visit(innerNode);
                            }
                        });
                    }
                    return super.visit(node);
                }
            });
        }
    }

    private static void checkMethodInvocationsInLoop(ASTNode loopBody, Map<String, MethodDeclaration> methodMap, Map<String, Set<String>> callGraph) {
        loopBody.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                String methodName = node.getName().getIdentifier();
                if (DB_METHODS.contains(methodName)) {
                    reportAntiPattern(node);
                } else if (methodMap.containsKey(methodName)) {
                    traceMethodCallsInLoop(methodName, methodMap, callGraph, new HashSet<>(), node);
                }
                return super.visit(node);
            }
        });
    }

    private static void traceMethodCallsInLoop(String methodName, Map<String, MethodDeclaration> methodMap, Map<String, Set<String>> callGraph, Set<String> visited, MethodInvocation originalInvocation) {
        if (!visited.add(methodName)) {
            return;
        }
        Set<String> callees = callGraph.get(methodName);
        if (callees != null) {
            for (String callee : callees) {
                if (DB_METHODS.contains(callee)) {
                    reportAntiPattern(originalInvocation);
                } else if (methodMap.containsKey(callee)) {
                    traceMethodCallsInLoop(callee, methodMap, callGraph, visited, originalInvocation);
                }
            }
        }
    }

    private static void reportAntiPattern(MethodInvocation methodInvocation) {
        System.err.println("[ANTI-PATTERN] Detected: " + methodInvocation.getName().getIdentifier() +
                " call inside a loop at line " +
                ((CompilationUnit) methodInvocation.getRoot()).getLineNumber(methodInvocation.getStartPosition()));
    }

    static class MethodCollector extends ASTVisitor {
        private final Map<String, MethodDeclaration> methodMap;
        private final List<String> excludedMethods;

        MethodCollector(Map<String, MethodDeclaration> methodMap, List<String> excludedMethods) {
            this.methodMap = methodMap;
            this.excludedMethods = excludedMethods;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            if (!excludedMethods.contains(node.getName().toString())) {
                methodMap.put(node.getName().toString(), node);
            }
            return super.visit(node);
        }
    }

    static class MethodNameVisitor extends ASTVisitor {
        private final List<String> methodNames = new ArrayList<>();
        private final List<String> excludedMethods;

        public MethodNameVisitor(List<String> excludedMethods) {
            this.excludedMethods = excludedMethods;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            if (!excludedMethods.contains(node.getName().getIdentifier())) {
                methodNames.add(node.getName().getIdentifier());
            }
            return super.visit(node);
        }

        public List<String> getMethodNames() {
            return methodNames;
        }
    }
}
