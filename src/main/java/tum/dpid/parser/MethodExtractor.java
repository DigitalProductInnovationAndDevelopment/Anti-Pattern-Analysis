package tum.dpid.parser;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class MethodExtractor {
    public static List<String> extractMethodNames(File projectDirectory, ASTParser parser, Path javaFilePath) throws IOException {
        String content = new String(Files.readAllBytes(javaFilePath));
        parser.setSource(content.toCharArray());
        ASTGenerator.setParserArgs(projectDirectory, parser);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        MethodNameVisitor visitor = new MethodNameVisitor();
        cu.accept(visitor);
        return visitor.getMethodNames();
    }
}
