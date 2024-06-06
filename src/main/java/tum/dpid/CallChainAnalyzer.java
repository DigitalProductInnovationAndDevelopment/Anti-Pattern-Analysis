package tum.dpid;

import org.eclipse.jdt.core.dom.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CallChainAnalyzer {

    private static final List<String> DB_METHODS = List.of("flush", "save", "update", "persist", "remove", "find",
            "selectById", "selectExistingById", "delete",
            "selectByArticleNumber", "saveWithoutFlush", "merge", "selectFrom");

    public static void main(String[] args) {
        String projectDirectoryPath = "../fromItestra/LoopAntiPattern";

        File projectDirectory = new File(projectDirectoryPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        Map<String, MethodDeclaration> methodMap = collectMethods(projectDirectory);
        Map<String, Set<String>> callGraph = buildCallGraph(methodMap);

        for (String targetMethod : DB_METHODS) {
            System.out.println("Call graph for method: " + targetMethod);
            drawCallGraph(targetMethod, callGraph, 0, new HashSet<>());
            System.out.println();
        }

        checkForDatabaseCallsInLoops(methodMap, callGraph);
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
                    System.out.println("For statement visited.");
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(WhileStatement node) {
                    System.out.println("While statement visited.");
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(DoStatement node) {
                    System.out.println("Do-While statement visited.");
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(EnhancedForStatement node) {
                    System.out.println("EnhancedForStatement statement visited.");
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(LambdaExpression node) {
                    System.out.println("Lambda statement visited.");
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph);
                    return super.visit(node);
                }

                @Override
                public boolean visit(MethodInvocation node) {
                    // Check for stream method calls like map, filter, etc.
                    if (node.getName().getIdentifier().equals("map") || node.getName().getIdentifier().equals("forEach") || node.getName().getIdentifier().equals("stream")) {
                        System.out.println("Stream-related method visited: " + node.getName().getIdentifier());
                        node.accept(new ASTVisitor() {
                            @Override
                            public boolean visit(LambdaExpression lambdaExpression) {
                                System.out.println("Lambda statement visited inside stream operation.");
                                checkMethodInvocationsInLoop(lambdaExpression.getBody(), methodMap, callGraph);
                                return super.visit(lambdaExpression);
                            }

                            @Override
                            public boolean visit(MethodInvocation innerNode) {
                                System.out.println("MethodInvocation inside lambda visited: " + innerNode.getName().getIdentifier());
                                String methodName = innerNode.getName().getIdentifier();
                                if (DB_METHODS.contains(methodName)) {
                                    reportAntiPattern(innerNode);
                                } else if (methodMap.containsKey(methodName)) {
                                    System.out.println("Tracing method: " + methodName);
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
                System.out.println("MethodInvocation visited: " + methodName);
                if (DB_METHODS.contains(methodName)) {
                    reportAntiPattern(node);
                } else if (methodMap.containsKey(methodName)) {
                    System.out.println("Tracing method: " + methodName);
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
                System.out.println("Callee: " + callee);
                if (DB_METHODS.contains(callee)) {
                    System.out.println("DB method detected in call chain: " + callee);
                    reportAntiPattern(originalInvocation);
                } else if (methodMap.containsKey(callee)) {
                    traceMethodCallsInLoop(callee, methodMap, callGraph, visited, originalInvocation);
                }
            }
        }
    }

    private static void reportAntiPattern(MethodInvocation methodInvocation) {
        System.out.println("Anti-pattern detected: " + methodInvocation.getName().getIdentifier() +
                " call inside a loop at line " +
                ((CompilationUnit) methodInvocation.getRoot()).getLineNumber(methodInvocation.getStartPosition()));
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
