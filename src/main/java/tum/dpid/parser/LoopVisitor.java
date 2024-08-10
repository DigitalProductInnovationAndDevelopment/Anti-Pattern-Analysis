package tum.dpid.parser;


import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class LoopVisitor {

    public static void checkForDatabaseCallsInLoops(Map<String, MethodDeclaration> methodMap, Map<String, List<String>> callGraph, List<String> dbMethods) {
        for (MethodDeclaration method : methodMap.values()) {
            method.accept(new ASTVisitor() {
                @Override
                public boolean visit(ForStatement node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph, dbMethods);
                    return super.visit(node);
                }

                @Override
                public boolean visit(WhileStatement node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph, dbMethods);
                    return super.visit(node);
                }

                @Override
                public boolean visit(DoStatement node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph, dbMethods);
                    return super.visit(node);
                }

                @Override
                public boolean visit(EnhancedForStatement node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph, dbMethods);
                    return super.visit(node);
                }

                @Override
                public boolean visit(LambdaExpression node) {
                    checkMethodInvocationsInLoop(node.getBody(), methodMap, callGraph, dbMethods);
                    return super.visit(node);
                }

                @Override
                public boolean visit(MethodInvocation node) {
                    // Check for stream method calls like map, filter, etc.
                    if (node.getName().getIdentifier().equals("map") || node.getName().getIdentifier().equals("forEach") || node.getName().getIdentifier().equals("stream") || node.getName().getIdentifier().equals("LongStream")) {
                        checkMethodInvocationsInLoop(node, methodMap, callGraph, dbMethods);
                    }
                    return super.visit(node);
                }
            });
        }
    }

    private static void checkMethodInvocationsInLoop(ASTNode loopBody, Map<String, MethodDeclaration> methodMap, Map<String, List<String>> callGraph, List<String> dbMethods) {
        loopBody.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                String methodName = node.getName().getIdentifier();
                //System.out.println("checkMethodInvocationsInLoop node " +  methodName);
                if (dbMethods.contains(methodName)) {
                    //reportAntiPattern(node);
                    System.out.println("Anti Pattern  " + node.getName() + " methodName " + methodName);
                } else if (methodMap.containsKey(methodName)) {
                    traceMethodCallsInLoop(methodName, methodMap, callGraph, new HashSet<>(), node, dbMethods);
                }
                return super.visit(node);
            }

            @Override
            public boolean visit(LambdaExpression node) {
                node.getBody().accept(new ASTVisitor() {
                    @Override
                    public boolean visit(MethodInvocation methodInvocationNode) {
                        System.out.println("1Loop Visitor lambda body: methodInvocation "  + methodInvocationNode.getName().toString());
                        if (dbMethods.contains(methodInvocationNode.getName().toString()) || dbMethods.contains(methodInvocationNode.getName().getIdentifier())) {
                            //reportAntiPattern(node);
                            System.out.println("Anti pattern lambda node" + methodInvocationNode.getName().toString() );
                        } else if (methodMap.containsKey(methodInvocationNode.getName().toString()) || methodMap.containsKey(methodInvocationNode.getName().getIdentifier())) {
                            traceMethodCallsInLoop(methodInvocationNode.getName().toString(), methodMap, callGraph, new HashSet<>(), methodInvocationNode, dbMethods);
                        }
                        //return super.visit(innerNode);
                        return super.visit(methodInvocationNode);
                    }

                    @Override
                    public boolean visit(ExpressionMethodReference lambdaNode) {
                        System.out.println("Loop Visitor lambda body: ExpressionMethodReference "  + lambdaNode.getName().toString());

                        return super.visit(lambdaNode);
                    }

                    @Override
                    public boolean visit(SuperMethodReference lambdaNode) {
                        System.out.println("Loop Visitor lambda body: SuperMethodReference "  + lambdaNode.getName().toString());

                        return super.visit(lambdaNode);
                    }

                    @Override
                    public boolean visit(TypeMethodReference lambdaNode) {
                        System.out.println("Loop Visitor lambda body: TypeMethodReference "  + lambdaNode.getName().toString());

                        return super.visit(lambdaNode);
                    }
                });

                return super.visit(node);
            }

            @Override
            public boolean visit(ExpressionMethodReference lambdaNode) {
                System.out.println("1Loop Visitor lambda body: ExpressionMethodReference "  + lambdaNode.getName().toString());
                //traceMethodCallsInLoop(lambdaNode.getName().toString(), methodMap, callGraph, new HashSet<>(), lambdaNode, dbMethods);
                if (dbMethods.contains(lambdaNode.getName().toString())) {
                    //reportAntiPattern(lambdaNode);
                    System.out.println("Anti pattern lambdaNode Anti Pattern Found " + lambdaNode.toString());
                } else if (methodMap.containsKey(lambdaNode.getName().toString())) {
                    traceMethodCallsInLoop(lambdaNode.getName().toString(), methodMap, callGraph, new HashSet<>(), lambdaNode, dbMethods);
                }
                return super.visit(lambdaNode);
            }
        });
    }

    private static void traceMethodCallsInLoop(String methodName, Map<String, MethodDeclaration> methodMap, Map<String, List<String>> callGraph, Set<String> visited, ASTNode originalInvocation, List<String> dbMethods) {
        if (!visited.add(methodName)) {
            return;
        }
        List<String> callees = callGraph.get(methodName);
        if (callees != null) {
            for (String callee : callees) {

                if (dbMethods.contains(callee)) {
                    //System.out.println("traceMethodCallsInLoop db method " + callee);
                    //reportAntiPattern(originalInvocation);
                    System.out.println("Anti Pattern Found callee " + callee + " Node " + originalInvocation.toString());
                } else if (methodMap.containsKey(callee)) {
                    traceMethodCallsInLoop(callee, methodMap, callGraph, visited, originalInvocation, dbMethods);
                }
            }
        }
    }

    private static void reportAntiPattern(MethodInvocation methodInvocation) {

        System.err.println("[ANTI-PATTERN] Detected: " + methodInvocation.getName().getIdentifier() +
                " call inside a loop at line " +
                ((CompilationUnit) methodInvocation.getRoot()).getLineNumber(methodInvocation.getStartPosition()));

    }

}
