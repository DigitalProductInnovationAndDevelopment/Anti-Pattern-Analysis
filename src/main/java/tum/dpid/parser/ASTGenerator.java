package tum.dpid.parser;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import java.io.File;

public class ASTGenerator {

    public static ASTParser createParser() {
        return ASTParser.newParser(AST.JLS17);
    }

    public static void setParserArgs(File projectDirectory, ASTParser parser) {
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        String[] sourcePathEntries = { projectDirectory.getAbsolutePath() + "/src/main/java"};
        parser.setEnvironment(null, sourcePathEntries, null, true);
        parser.setResolveBindings(true);
        parser.setStatementsRecovery(true);
        parser.setBindingsRecovery(true);
        parser.setCompilerOptions(JavaCore.getOptions());
        parser.setUnitName("com.example.LoopAntiPattern");//could be random unit name
    }
}
