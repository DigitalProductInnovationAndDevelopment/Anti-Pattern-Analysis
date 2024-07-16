package tum.dpid.parser;

import org.eclipse.jdt.core.dom.*;

import java.util.List;
import java.util.Map;

public class MethodCollector extends ASTVisitor {
    private final Map<String, MethodDeclaration> methodMap;
    private final List<String> excludedMethods;

    public MethodCollector(Map<String, MethodDeclaration> methodMap, List<String> excludedMethods) {
        this.methodMap = methodMap;
        this.excludedMethods = excludedMethods;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (!excludedMethods.contains(node.getName().toString())) {
            methodMap.put(node.getName().toString(), node);
        }
        return super.visit(node);
    }
}
