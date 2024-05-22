package tum.dpid;

import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiPatternDetector {

    public static void main(String[] args) {
        String projectDirectoryPath = "/Users/melisuensal/Desktop/LoopAntiPattern";

        File projectDirectory = new File(projectDirectoryPath);
        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        processProjectDirectory(projectDirectory);
    }

    private static void processProjectDirectory(File projectDirectory) {
        System.out.println("processProjectDirectory");
        File[] javaFiles = projectDirectory.listFiles((dir, name) -> name.endsWith(".java"));

        if (javaFiles != null) {
            for (File javaFile : javaFiles) {
                try {
                    String source = new String(Files.readAllBytes(javaFile.toPath()));
                    analyzeSourceCode(source);
                } catch (IOException e) {
                    System.err.println("Error reading file: " + javaFile.getName());
                    e.printStackTrace();
                }
            }
        }
    }

       private static void analyzeSourceCode(String source){
           System.out.println("analyzeSourceCode");
        ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        MethodCollector methodCollector = new MethodCollector();
        cu.accept(methodCollector);

        Map<String, MethodDeclaration> methodMap = methodCollector.getMethodMap();

        cu.accept(new ForLoopVisitor(methodMap));
    }
}

class ForLoopVisitor extends ASTVisitor {
    private static final List<String> DB_METHODS = List.of("flush", "save", "update", "persist", "remove", "find");
    private Map<String, MethodDeclaration> methodMap;

    public ForLoopVisitor(Map<String, MethodDeclaration> methodMap) {
        this.methodMap = methodMap;
    }

    @Override
    public boolean visit(ForStatement node) {
        node.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation methodInvocation) {
                String methodName = methodInvocation.getName().getIdentifier();
                if (DB_METHODS.contains(methodName)) {
                    reportAntiPattern(methodInvocation);
                } else if (methodMap.containsKey(methodName)) {
                    MethodDeclaration methodDeclaration = methodMap.get(methodName);
                    methodDeclaration.accept(new MethodInvocationVisitor(methodInvocation));
                }
                return super.visit(methodInvocation);
            }
        });
        return super.visit(node);
    }

    private void reportAntiPattern(MethodInvocation methodInvocation) {
        System.out.println("Anti-pattern detected: " + methodInvocation.getName().getIdentifier() +
                " call inside a for loop at line " +
                ((CompilationUnit) methodInvocation.getRoot()).getLineNumber(methodInvocation.getStartPosition()));
    }

    private class MethodInvocationVisitor extends ASTVisitor {
        private MethodInvocation originalInvocation;

        public MethodInvocationVisitor(MethodInvocation originalInvocation) {
            this.originalInvocation = originalInvocation;
        }

        @Override
        public boolean visit(MethodInvocation methodInvocation) {
            String methodName = methodInvocation.getName().getIdentifier();
            if (DB_METHODS.contains(methodName)) {
                reportAntiPattern(originalInvocation);
            } else if (methodMap.containsKey(methodName)) {
                MethodDeclaration methodDeclaration = methodMap.get(methodName);
                methodDeclaration.accept(new MethodInvocationVisitor(originalInvocation));
            }
            return super.visit(methodInvocation);
        }
    }
}

class MethodCollector extends ASTVisitor {
    private Map<String, MethodDeclaration> methodMap = new HashMap<>();

    @Override
    public boolean visit(MethodDeclaration node) {
        String methodName = node.getName().getIdentifier();
        methodMap.put(methodName, node);
        return super.visit(node);
    }

    public Map<String, MethodDeclaration> getMethodMap() {
        return methodMap;
    }
}
