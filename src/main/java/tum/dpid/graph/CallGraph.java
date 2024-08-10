package tum.dpid.graph;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import tum.dpid.model.MethodDeclarationWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallGraph {
    public static Map<String, List<String>> buildCallGraph(Map<String, MethodDeclarationWrapper> methodMap) {
        Map<String, List<String>> callGraph = new HashMap<>();

        for (MethodDeclarationWrapper wrapper : methodMap.values()) {
            MethodDeclaration method = wrapper.getDeclaration();
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
                    });
                    return super.visit(node);
                }

                @Override
                public boolean visit(ExpressionMethodReference node) {
                    String caller = method.getName().toString();
                    String callee = node.getName().toString();
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
