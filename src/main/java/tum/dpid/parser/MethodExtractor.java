package tum.dpid.parser;

import org.eclipse.jdt.core.dom.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.Collections;

public class MethodExtractor {
    public static List<String> extractMethodNames(Path javaFilePath, List<String> excludedClasses, List<String> excludedMethods) throws IOException {
        String className = javaFilePath.getFileName().toString();
        if(excludedClasses.contains(className)) {
            return Collections.emptyList();
        }
        String content = new String(Files.readAllBytes(javaFilePath));
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(content.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        MethodNameVisitor visitor = new MethodNameVisitor(excludedMethods);
        cu.accept(visitor);
        return visitor.getMethodNames();
    }
}
