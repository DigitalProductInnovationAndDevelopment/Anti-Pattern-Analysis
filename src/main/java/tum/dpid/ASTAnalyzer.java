package tum.dpid;

import org.eclipse.jdt.core.dom.*;
import java.io.*;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class ASTAnalyzer {
    public void analyzeFile(String filePath) {
        String source = readFileToString(filePath);
        ASTParser parser = ASTParser.newParser(AST.JLS_Latest);
        parser.setSource(source.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);
        parser.setEnvironment(null, new String[] { "../fromItestra/LoopAntiPattern" }, new String[] { "UTF-8" }, true);
        parser.setUnitName("");

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        cu.accept(new MethodVisitor());
    }

    class MethodVisitor extends ASTVisitor {
        private boolean isInLoop = false;

        public boolean visit(ForStatement node) {
            System.out.println("For loop");
            isInLoop = true;
            node.getBody().accept(this);
            isInLoop = false;
            return false; // Prevents default traversal to avoid redundant checks
        }

        public boolean visit(WhileStatement node) {
            System.out.println("While loop");
            isInLoop = true;
            node.getBody().accept(this);
            isInLoop = false;
            return false;
        }

        public boolean visit(MethodDeclaration node) {
            if (node.modifiers().stream().anyMatch(modifier -> modifier instanceof Annotation &&
                    ((Annotation) modifier).getTypeName().getFullyQualifiedName().equals("Transactional"))) {
                String methodName = node.getName().getIdentifier();
                int lineNumber = ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition());
                System.out.println("@Transactional method detected: " + methodName + " at line " + lineNumber);
            }
            return true; // Continue visiting child nodes
        }

        public boolean visit(MethodInvocation node) {
            String methodName = node.getName().getIdentifier();
            if (methodName.equals("executeQuery") || methodName.equals("callService")) {
                String context = isInLoop ? "inside a loop" : "outside loop";
                System.out.println("Potential performance issue: " + methodName + " " + context + " at line " +
                        ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()));
            }
            if (methodName.equals("stream")) {
                String context = isInLoop ? "inside a loop" : "outside loop";
                System.out.println("Stream operation detected in " + context + " at line " +
                        ((CompilationUnit) node.getRoot()).getLineNumber(node.getStartPosition()));
            }
            return true;
        }
    }

    private String readFileToString(String filePath) {
        StringBuilder fileData = new StringBuilder(1000);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            char[] buf = new char[10];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData.toString();
    }
}
