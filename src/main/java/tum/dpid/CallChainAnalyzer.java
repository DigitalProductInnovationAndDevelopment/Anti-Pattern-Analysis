package tum.dpid;

import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CallChainAnalyzer {

    public static void main(String[] args) {

        // find and collect methods under repository folder to create a target list
        String directoryPath = "../LoopAntiPattern/src/main/java/com/example/LoopAntiPattern/data/repository";
        Path dirPath = Paths.get(directoryPath);
        List<String> DB_METHODS = new ArrayList<>();

        try {
            List<Path> javaFiles = Files.walk(dirPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();

            for (Path javaFile : javaFiles) {
                DB_METHODS.addAll(extractMethodNames(javaFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String projectDirectoryPath = "../LoopAntiPattern/src";

        File projectDirectory = new File(projectDirectoryPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        Map<String, MethodDeclaration> methodMap = collectMethods(projectDirectory);
        Map<String, List<String>> callGraph = buildCallGraph(methodMap);

        for (String targetMethod : DB_METHODS) {
            List<String> callChain = new ArrayList<>();
            traceCallChainInOrder(targetMethod, callGraph, new HashSet<>(), callChain, methodMap);

            if (callChain.isEmpty()) {
                System.out.println("No calls found for method: " + targetMethod);
            }

            System.out.println("Call graph for method: " + targetMethod);
            drawCallGraph(targetMethod, callGraph, 0, new HashSet<>());
            System.out.println();

        }
    }

    private static List<String> extractMethodNames(Path javaFilePath) throws IOException {
        String content = new String(Files.readAllBytes(javaFilePath));
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(content.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        MethodNameVisitor visitor = new MethodNameVisitor();
        cu.accept(visitor);
        return visitor.getMethodNames();
    }

    static class MethodNameVisitor extends ASTVisitor {
        private final List<String> methodNames = new ArrayList<>();

        @Override
        public boolean visit(MethodDeclaration node) {
            methodNames.add(node.getName().getIdentifier());
            return super.visit(node);
        }

        public List<String> getMethodNames() {
            return methodNames;
        }
    }

    private static Map<String, MethodDeclaration> collectMethods(File projectDirectory) {
        Map<String, MethodDeclaration> methodMap = new HashMap<>();
        List<File> javaFiles = getJavaFiles(projectDirectory);

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

    private static Map<String, List<String>> buildCallGraph(Map<String, MethodDeclaration> methodMap) {
        Map<String, List<String>> callGraph = new HashMap<>();

        for (MethodDeclaration method : methodMap.values()) {
            if(method.getName().toString().equals("updateProducts"))
            {
                System.out.println("");
            }
            method.accept(new ASTVisitor() {
                @Override
                public boolean visit(MethodInvocation node) {
                    String caller = method.getName().toString();
                    String callee = node.getName().toString();
                    callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);

                    return super.visit(node);
                }

                @Override
                public boolean visit(LambdaExpression node) {
                    node.getBody().accept(new ASTVisitor() {
                        @Override
                        public boolean visit(MethodInvocation lambdaNode) {
                            String caller = method.getName().toString();
                            String callee = lambdaNode.getName().toString();
                            callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);

                            return super.visit(lambdaNode);
                        }
                    });

                    return super.visit(node);
                }
            });
        }

        return callGraph;
    }

    private static void traceCallChainInOrder(String methodName, Map<String, List<String>> callGraph, Set<String> visited, List<String> callChain, Map<String, MethodDeclaration> methodMap) {
        if (!visited.add(methodName)) {
            return;
        }
        callChain.add(methodName);
        List<String> callees = callGraph.get(methodName);
        if (callees != null) {
            for (String callee : callees) {
                traceCallChainInOrder(callee, callGraph, visited, callChain, methodMap);
            }
        }
    }

    private static void drawCallGraph(String methodName, Map<String, List<String>> callGraph, int level, Set<String> visited) {
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
