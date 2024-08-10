package tum.dpid.parser;

import org.eclipse.jdt.core.dom.*;
import tum.dpid.util.MethodKeyGenerator;

import java.util.ArrayList;
import java.util.List;

public class MethodNameVisitor extends ASTVisitor {
    private final List<String> methodNames = new ArrayList<>();

    public MethodNameVisitor() {
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        methodNames.add(node.getName().toString());
        return super.visit(node);
    }

    public List<String> getMethodNames() {
        return methodNames;
    }
}
