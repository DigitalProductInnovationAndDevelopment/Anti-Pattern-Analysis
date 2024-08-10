package tum.dpid.graph;

import org.eclipse.jdt.core.dom.*;
import java.util.*;

public class CallGraph {
    public static Map<String, List<String>> buildCallGraph(Map<String, MethodDeclaration> methodMap) {
        Map<String, List<String>> callGraph = new HashMap<>();

        for (MethodDeclaration method : methodMap.values()) {
            method.accept(new ASTVisitor() {
                @Override
                public boolean visit(MethodInvocation node) {
                    String caller = method.getName().toString();
                    String callee = node.getName().toString();
                    callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
                    return super.visit(node);
                }

                @Override
                public boolean visit(LambdaExpression node) {
                    node.getBody().accept(new ASTVisitor() {
                        @Override
                        public boolean visit(MethodInvocation lambdaNode) {
                            String caller = method.getName().toString();
                            String callee = lambdaNode.getName().toString();
                            callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
                            return super.visit(lambdaNode);
                        }

                        @Override
                        public boolean visit(ExpressionMethodReference methodReferenceNode) {
                            String caller = method.getName().toString();
                            String callee = methodReferenceNode.getName().toString();
                            callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
                            return super.visit(methodReferenceNode);
                        }

                        @Override
                        public boolean visit(SuperMethodReference methodReferenceNode) {
                            String caller = method.getName().toString();
                            String callee = methodReferenceNode.getName().toString();
                            callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
                            return super.visit(methodReferenceNode);
                        }

                        @Override
                        public boolean visit(TypeMethodReference methodReferenceNode) {
                            String caller = method.getName().toString();
                            String callee = methodReferenceNode.getName().toString();
                            callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
                            return super.visit(methodReferenceNode);
                        }
                    });
                    return super.visit(node);
                }

                @Override
                public boolean visit(ExpressionMethodReference node) {
                    String caller = method.getName().toString();
                    String callee = node.getName().toString();

//                    System.out.println("Caller " + caller + " callee " + callee +"  resolveMethodBinding: " +node.resolveMethodBinding() + " declaration is " + node.resolveMethodBinding().getMethodDeclaration()
//                            + " key: " +  node.resolveMethodBinding().getKey() + " package name: " + node.resolveMethodBinding().getDeclaringClass().getPackage().getName());

                    callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
                    return super.visit(node);
                }

                @Override
                public boolean visit(SuperMethodReference node) {
                    String caller = method.getName().toString();
                    String callee = node.getName().toString();
                    callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
                    return super.visit(node);
                }

                @Override
                public boolean visit(TypeMethodReference node) {
                    String caller = method.getName().toString();
                    String callee = node.getName().toString();
                    callGraph.computeIfAbsent(callee, k -> new ArrayList<>()).add(caller);
                    return super.visit(node);
                }
            });
        }

        return callGraph;
    }

    public static void traceCallChainInOrder(String methodName, Map<String, List<String>> callGraph, Set<String> visited, List<String> callChain, Map<String, MethodDeclaration> methodMap) {
        if (!visited.add(methodName)) {
            return;
        }
        callChain.add(methodName);
        List<String> callees = callGraph.get(methodName);
        if (callees != null) {
            for (String callee : callees) {
                traceCallChainInOrder(callee, callGraph, visited, callChain, methodMap);
            }
        }
    }
}
