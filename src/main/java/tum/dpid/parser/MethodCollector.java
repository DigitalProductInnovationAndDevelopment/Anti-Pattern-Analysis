package tum.dpid.parser;

import org.eclipse.jdt.core.dom.*;
import tum.dpid.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodCollector extends ASTVisitor {
    private final Map<String, MethodDeclaration> methodMap;
    private final List<String> excludedMethods;

    public MethodCollector(Map<String, MethodDeclaration> methodMap, List<String> excludedMethods) {
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

    public static Map<String, MethodDeclaration> collectMethods(File projectDirectory, List<String> excludedMethods) throws IOException {
        Map<String, MethodDeclaration> methodMap = new HashMap<>();
        List<File> javaFiles = FileUtils.getJavaFilesRecursively(projectDirectory);

        for (File javaFile : javaFiles) {
            try {
                String source = new String(Files.readAllBytes(javaFile.toPath()));
                ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
                parser.setSource(source.toCharArray());
                parser.setKind(ASTParser.K_COMPILATION_UNIT);

                CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                cu.accept(new MethodCollector(methodMap, excludedMethods));
            } catch (IOException e) {
                System.err.println("Error reading file: " + javaFile.getName());
                e.printStackTrace();
            }
        }

        return methodMap;
    }
}
