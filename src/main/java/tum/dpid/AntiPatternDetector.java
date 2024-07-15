package tum.dpid;

import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/*
* CompilationUnit
└── TypeDeclaration (class Example)
    ├── MethodDeclaration (methodA)
    │   └── Block
    │       └── ForStatement
    │           ├── Expression (int i = 0)
    │           ├── Expression (i < 10)
    │           ├── Expression (i++)
    │           └── Block
    │               └── MethodInvocation (methodB)
    ├── MethodDeclaration (methodB)
    │   └── Block
    │       └── MethodInvocation (methodC)
    └── MethodDeclaration (methodC)
        └── Block
            └── MethodInvocation (find)
*/

public class AntiPatternDetector {

    private static final List<String> DB_METHODS = List.of("flush", "save", "update", "persist", "remove", "find");

    public static void main(String[] args) {
        String projectDirectoryPath = "../fromItestra/LoopAntiPattern";

        File projectDirectory = new File(projectDirectoryPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        Map<String, MethodDeclaration> methodMap = collectMethods(projectDirectory);

        for (MethodDeclaration method : methodMap.values()) {
            method.accept(new ForLoopVisitor(methodMap));
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
                } else if (file.isFile() && file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }

    private static class MethodCollector extends ASTVisitor {
        private final Map<String, MethodDeclaration> methodMap;

        public MethodCollector(Map<String, MethodDeclaration> methodMap) {
            this.methodMap = methodMap;
        }

        @Override
        public boolean visit(TypeDeclaration node) {
            for (MethodDeclaration method : node.getMethods()) {
                String methodName = method.getName().getIdentifier();
                methodMap.put(methodName, method);
            }
            return super.visit(node);
        }
    }

    private static class ForLoopVisitor extends ASTVisitor {
        private final Map<String, MethodDeclaration> methodMap;

        public ForLoopVisitor(Map<String, MethodDeclaration> methodMap) {
            this.methodMap = methodMap;
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            node.accept(new ASTVisitor() {
                @Override
                public boolean visit(org.eclipse.jdt.core.dom.ForStatement forStatement) {
                    forStatement.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(MethodInvocation methodInvocation) {
                            String methodName = methodInvocation.getName().getIdentifier();
                            if (DB_METHODS.contains(methodName)) {
                                reportAntiPattern(methodInvocation);
                            } else if (methodMap.containsKey(methodName)) {
                                MethodDeclaration methodDeclaration = methodMap.get(methodName);
                                Set<String> visitedMethods = new HashSet<>();
                                visitedMethods.add(node.getName().getIdentifier());
                                methodDeclaration.accept(new MethodInvocationVisitor(methodMap, methodInvocation, visitedMethods));
                            }
                            return super.visit(methodInvocation);
                        }
                    });
                    return super.visit(forStatement);
                }
            });
            return super.visit(node);
        }

        private void reportAntiPattern(MethodInvocation methodInvocation) {
            System.out.println("Anti-pattern detected: " + methodInvocation.getName().getIdentifier() +
                    " call inside a for loop at line " +
                    ((CompilationUnit) methodInvocation.getRoot()).getLineNumber(methodInvocation.getStartPosition()));
        }
    }

    private static class MethodInvocationVisitor extends ASTVisitor {
        private final Map<String, MethodDeclaration> methodMap;
        private final MethodInvocation originalInvocation;
        private final Set<String> visitedMethods;

        public MethodInvocationVisitor(Map<String, MethodDeclaration> methodMap, MethodInvocation originalInvocation, Set<String> visitedMethods) {
            this.methodMap = methodMap;
            this.originalInvocation = originalInvocation;
            this.visitedMethods = visitedMethods;
        }

        @Override
        public boolean visit(MethodInvocation methodInvocation) {
            String methodName = methodInvocation.getName().getIdentifier();
            if (visitedMethods.contains(methodName)) {
                return false; // Skip already visited methods in this call chain
            }
            visitedMethods.add(methodName);

            if (DB_METHODS.contains(methodName)) {
                System.out.println("Anti-pattern detected: " + originalInvocation.getName().getIdentifier() +
                        " indirectly calls " + methodInvocation.getName().getIdentifier() +
                        " inside a for loop at line " +
                        ((CompilationUnit) originalInvocation.getRoot()).getLineNumber(originalInvocation.getStartPosition()));
            } else if (methodMap.containsKey(methodName)) {
                MethodDeclaration methodDeclaration = methodMap.get(methodName);
                methodDeclaration.accept(new MethodInvocationVisitor(methodMap, originalInvocation, visitedMethods));
            }
            return super.visit(methodInvocation);
        }
    }
}