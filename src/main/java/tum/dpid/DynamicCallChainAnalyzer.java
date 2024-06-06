package com.itestra.callchain;

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
import tum.dpid.CallChainAnalyzer;

public class DynamicCallChainAnalyzer {

    private static List<String> dbMethods;

    public static void main(String[] args) {
        DynamicCallChainAnalyzer analyzer = new DynamicCallChainAnalyzer();
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
            System.out.println("Finding call chains for database method: " + dbMethod);
            Set<String> visited = new HashSet<>();
            List<String> callChain = new ArrayList<>();
            traceCallChainsToMethod(dbMethod, callGraph, new HashSet<>(), callChain, methodMap);
        }
    }

    public void initializeDbMethods() {
        dbMethods = new ArrayList<>();
        // Add common database methods by naming convention
        addCommonDbMethods();

        // Scan classes in the project
        String packageName = "com.itestra"; // Specify your project's base package
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

    public List<String> getDbMethods() {
        return dbMethods;
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
