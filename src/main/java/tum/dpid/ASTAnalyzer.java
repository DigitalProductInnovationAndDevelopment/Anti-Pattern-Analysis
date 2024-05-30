package tum.dpid;

import org.eclipse.jdt.core.dom.*;

import java.io.*;
import java.util.*;

public class ASTAnalyzer {
    private final Map<String, List<String>> callGraph = new HashMap<>();

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
        private String currentClassName = null;
        private String currentMethodName = null;

        @Override
        public boolean visit(TypeDeclaration node) {
            currentClassName = node.getName().getIdentifier();
            return super.visit(node);
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            currentMethodName = node.getName().getIdentifier();
            String fullMethodName = currentClassName + "." + currentMethodName;
            callGraph.putIfAbsent(fullMethodName, new ArrayList<>());
            System.out.println("Detected method: " + fullMethodName);
            return super.visit(node);
        }

        @Override
        public boolean visit(MethodInvocation node) {
            if (currentClassName != null && currentMethodName != null) {
                String fullMethodName = currentClassName + "." + currentMethodName;
                String invokedMethodName = node.getName().getIdentifier();
                IMethodBinding binding = node.resolveMethodBinding();
                if (binding != null) {
                    ITypeBinding declaringClass = binding.getDeclaringClass();
                    if (declaringClass != null) {
                        String invokedFullMethodName = declaringClass.getName() + "." + invokedMethodName;
                        callGraph.get(fullMethodName).add(invokedFullMethodName);
                        System.out.println("Detected call from " + fullMethodName + " to " + invokedFullMethodName);
                    }
                }
            }
            return super.visit(node);
        }
    }

    public Map<String, List<String>> getCallGraph() {
        return callGraph;
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

    public List<String> findCallChain(String startMethod) {
        List<String> callChain = new ArrayList<>();
        findCallChainHelper(startMethod, new HashSet<>(), callChain);
        return callChain;
    }

    private void findCallChainHelper(String method, Set<String> visited, List<String> callChain) {
        if (visited.contains(method)) {
            return;
        }
        visited.add(method);
        for (String caller : callGraph.keySet()) {
            if (callGraph.get(caller).contains(method)) {
                callChain.add(caller);
                findCallChainHelper(caller, visited, callChain);
            }
        }
    }
}
