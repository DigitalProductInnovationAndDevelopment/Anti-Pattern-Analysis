package tum.dpid.jdt;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import tum.dpid.util.FileReader;

import java.io.File;

public class Parser {

    /**
     * Recursively read files in the directory and create AST then visit nodes
     */
    public static void processJavaFiles(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                processJavaFiles(file);
            } else if (file.getName().endsWith(".java")) {
                System.out.println("Parsing file "+ file.getName());
                CompilationUnit cu = parseSourceCode(file);
                assert cu != null;
                //Compilation Unit: The source range for this type of node is ordinarily the entire source file, including leading and trailing whitespace and comments.
                //System.out.println("Compilation Unit: " + cu.toString());

                cu.accept(new DeclarationVisitor());
            }
        }
    }

    /**
     *
     * @param file Source code file
     * @return CompilationUnit of file (root of AST)
     */
    public static CompilationUnit parseSourceCode(File file) {
        String sourceCode = FileReader.readSourceFromFile(file);
        if (sourceCode == null) {
            return null;
        }

        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setStatementsRecovery(true);
        parser.setUnitName("Program.java");

        String[] classPathEntries = {};
        String[] sourcePathEntries = {"src"};

        parser.setEnvironment(classPathEntries, sourcePathEntries, new String[]{"UTF-8"}, true);

        parser.setSource(sourceCode.toCharArray());
        return (CompilationUnit) parser.createAST(null);
    }
}
