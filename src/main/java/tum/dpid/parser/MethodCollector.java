package tum.dpid.parser;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import tum.dpid.file.FileUtils;
import tum.dpid.model.MethodDeclarationWrapper;
import tum.dpid.util.MethodKeyGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodCollector extends ASTVisitor {
    private final Map<String, MethodDeclarationWrapper> methodMap;
    private final CompilationUnit compilationUnit;
    private final List<String> exclusions;

    public MethodCollector(Map<String, MethodDeclarationWrapper> methodMap, CompilationUnit cu, List<String> exclusions) {
        this.methodMap = methodMap;
        this.compilationUnit = cu;
        this.exclusions = exclusions;
    }

    @Override
    public boolean visit(MethodDeclaration node) {

        String declaringClass = node.resolveBinding().getDeclaringClass().getBinaryName();
        int lineNumber = compilationUnit.getLineNumber(node.getStartPosition());
        int columnNumber = compilationUnit.getColumnNumber(node.getStartPosition());
        if (shouldExcludeMethod(MethodKeyGenerator.generateKey(node))) {
            methodMap.put(node.getName().toString(), new MethodDeclarationWrapper(true, declaringClass, lineNumber, columnNumber, node));
        } else {
            methodMap.put(node.getName().toString(), new MethodDeclarationWrapper(false, declaringClass, lineNumber, columnNumber, node));
        }
        return super.visit(node);
    }

    public static Map<String, MethodDeclarationWrapper> collectMethods(File projectDirectory, ASTParser parser, List<String> exclusions) throws IOException {
        Map<String, MethodDeclarationWrapper> methodMap = new HashMap<>();
        List<File> javaFiles = FileUtils.getJavaFilesRecursively(projectDirectory);
        for (File javaFile : javaFiles) {
            try {
                String source = new String(Files.readAllBytes(javaFile.toPath()));
                parser.setSource(source.toCharArray());
                ASTGenerator.setParserArgs(projectDirectory, parser);

                CompilationUnit cu = (CompilationUnit) parser.createAST(null);
                cu.accept(new MethodCollector(methodMap, cu, exclusions));

            } catch (IOException e) {
                System.err.println("Error reading file: " + javaFile.getName());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Error creating AST " + e);
            }
        }
        return methodMap;
    }

    public boolean shouldExcludeMethod(String methodKey) {
        for (String pattern : this.exclusions) {
            if (pattern.isEmpty())
                return false;
            if (methodKey.startsWith(pattern)) {
                return true;
            }
        }
        return false;
    }
}
