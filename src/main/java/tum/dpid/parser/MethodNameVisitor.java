package tum.dpid.parser;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class MethodNameVisitor extends ASTVisitor {
    private final List<String> methodNames = new ArrayList<>();
    private final List<String> excludedMethods;

    public MethodNameVisitor(List<String> excludedMethods) {
        this.excludedMethods = excludedMethods;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (!excludedMethods.contains(node.getName().getIdentifier())) {
            methodNames.add(node.getName().getIdentifier());
        }
        return super.visit(node);
    }

    public List<String> getMethodNames() {
        return methodNames;
    }
}
