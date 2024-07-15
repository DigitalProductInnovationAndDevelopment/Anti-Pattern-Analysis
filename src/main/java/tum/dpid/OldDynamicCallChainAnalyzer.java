package tum.dpid;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

public class OldDynamicCallChainAnalyzer {

    private static List<String> dbMethods;

    public static void main(String[] args) {
        OldDynamicCallChainAnalyzer analyzer = new OldDynamicCallChainAnalyzer();
        analyzer.initializeDbMethods();

        String projectDirectoryPath = "../fromItestra/LoopAntiPattern";

        File projectDirectory = new File(projectDirectoryPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        Map<String, MethodDeclaration> methodMap = collectMethods(projectDirectory);
        Map<String, Set<String>> callGraph = buildCallGraph(methodMap);

        for (String dbMethod : dbMethods) {
            System.out.println("Call chain for method: " + dbMethod);
            Set<String> visited = new HashSet<>();
            List<String> callChain = new ArrayList<>();
            traceCallChainInOrder(dbMethod, callGraph, new HashSet<>(), callChain, methodMap);

            if (callChain.isEmpty()) {
                System.out.println("No calls found for method: " + dbMethod);
            } else {
                for (String method : callChain) {
                    System.out.println(method);
                }
            }

            System.out.println("Call graph for method: " + dbMethod);
            drawCallGraph(dbMethod, callGraph, 0, new HashSet<>());
            System.out.println();
        }
    }

    public void initializeDbMethods() {
        dbMethods = new ArrayList<>();
        // Add common database methods by naming convention
        addCommonDbMethods();

        // Scan classes in the project
        String packageName = "com.example.LoopAntiPattern"; // Specify your project's base package
        List<Class<?>> classes = getClasses(packageName);

        for (Class<?> clazz : classes) {
            // Check if class is a repository
            if (clazz.isAnnotationPresent(Repository.class)) {
                scanClassForDbMethods(clazz);
            }

            // Check if methods are transactional
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Transactional.class)) {
                    dbMethods.add(method.getName());
                }
            }
        }
    }

    private void addCommonDbMethods() {
        dbMethods.add("flush");
        dbMethods.add("save");
        dbMethods.add("update");
        dbMethods.add("persist");
        dbMethods.add("remove");
        dbMethods.add("find");
        dbMethods.add("selectById");
        dbMethods.add("selectExistingById");
        dbMethods.add("delete");
        dbMethods.add("selectByArticleNumber");
        dbMethods.add("saveWithoutFlush");
        dbMethods.add("merge");
        dbMethods.add("selectFrom");
    }

    private void scanClassForDbMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (isDatabaseMethod(method)) {
                dbMethods.add(method.getName());
            }
        }
    }

    private boolean isDatabaseMethod(Method method) {
        String methodName = method.getName().toLowerCase();
        // Check common database operation prefixes
        return methodName.startsWith("save") ||
                methodName.startsWith("find") ||
                methodName.startsWith("delete") ||
                methodName.startsWith("update") ||
                methodName.startsWith("select") ||
                methodName.startsWith("remove") ||
                methodName.startsWith("persist") ||
                methodName.startsWith("flush") ||
                methodName.startsWith("merge");
    }

    private List<Class<?>> getClasses(String packageName) {
        // Use ClassGraph to scan and retrieve all classes in the package
        List<Class<?>> classes = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().acceptPackages(packageName).scan()) {
            for (ClassInfo classInfo : scanResult.getAllClasses()) {
                classes.add(Class.forName(classInfo.getName()));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return classes;
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

    private static void traceCallChainInOrder(String methodName, Map<String, Set<String>> callGraph, Set<String> visited, List<String> callChain, Map<String, MethodDeclaration> methodMap) {
        if (!visited.add(methodName)) {
            return;
        }
        callChain.add(methodName);
        Set<String> callees = callGraph.get(methodName);
        if (callees != null) {
            for (String callee : callees) {
                traceCallChainInOrder(callee, callGraph, visited, callChain, methodMap);
            }
        }
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
            if (dbMethods.contains(targetMethod)) {
                System.out.println("Database method call chain: " + String.join(" -> ", callChain));
            }
        }
    }

    public static class MethodCollector extends ASTVisitor {
        private final Map<String, MethodDeclaration> methodMap;

        public MethodCollector(Map<String, MethodDeclaration> methodMap) {
            this.methodMap = methodMap;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            methodMap.put(node.getName().toString(), node);
            return super.visit(node);
        }
    }
}
