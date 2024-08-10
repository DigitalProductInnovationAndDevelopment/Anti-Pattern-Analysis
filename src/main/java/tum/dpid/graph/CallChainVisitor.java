package tum.dpid.graph;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import tum.dpid.model.CallChainEntity;
import tum.dpid.model.MethodDeclarationWrapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallChainVisitor {

    public static void traceCallChainInOrder(String currentMethodName, String childMethodName, Map<String, List<String>> callGraph, List<CallChainEntity> callChain, Set<String> visited, Map<String, MethodDeclarationWrapper> methodMap) {
        MethodDeclarationWrapper wrapper = methodMap.get(currentMethodName);
        if (wrapper == null || wrapper.isExcluded() || visited.contains(currentMethodName)) {
            return;
        }
        visited.add(currentMethodName);

        if (childMethodName == null) {
            callChain.add(new CallChainEntity(currentMethodName, null, false));
        } else {
            MethodDeclaration declaration = wrapper.getDeclaration();
            declaration.accept(new ASTVisitor() {
                @Override
                public boolean visit(ForStatement node) {
                    boolean alreadyInChain = callChain.contains(new CallChainEntity(currentMethodName, childMethodName, false));
                    if (node.getBody().toString().contains(childMethodName) && !alreadyInChain) {
                        callChain.add(new CallChainEntity(currentMethodName, childMethodName, true));
                    }
                    return super.visit(node);
                }

                @Override
                public boolean visit(EnhancedForStatement node) {
                    boolean alreadyInChain = callChain.contains(new CallChainEntity(currentMethodName, childMethodName, false));
                    if (node.getBody().toString().contains(childMethodName) && !alreadyInChain) {
                        callChain.add(new CallChainEntity(currentMethodName, childMethodName, true));
                    }
                    return super.visit(node);
                }

                @Override
                public boolean visit(MethodInvocation node) {
                    boolean alreadyInChain = callChain.contains(new CallChainEntity(currentMethodName, childMethodName, false));
                    Set<String> streamAPICalls = new HashSet<>(Set.of("forEach", "map", "filter", "reduce", "collect"));
                    String nodeName = node.getName().toString();
                    if (streamAPICalls.contains(nodeName) && node.arguments().toString().contains(childMethodName) && !alreadyInChain) {
                        callChain.add(new CallChainEntity(currentMethodName, childMethodName, true));
                    }
                    return super.visit(node);
                }
            });
        }
        boolean alreadyInChain = callChain.contains(new CallChainEntity(currentMethodName, childMethodName, false));
        if (!alreadyInChain) {
            callChain.add(new CallChainEntity(currentMethodName, childMethodName, false));
        }
        List<String> callees = callGraph.get(currentMethodName);
        if (callees != null) {
            for (String callee : callees) {
                traceCallChainInOrder(callee, currentMethodName, callGraph, callChain, visited, methodMap);
            }
        }
    }
}
