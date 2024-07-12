package tum.dpid.services.v1;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtAbstractVisitor;

import java.util.*;

/*
    Previous version of method call chain processor but it lacks of all. It only prints first degree of method call chain
 */
public class CallHierarchyProcessor extends CtAbstractVisitor {
    private Map<String, Set<String>> callHierarchy = new HashMap<>();

    /*This prints one degree of children*/
    @Override
    public <T> void visitCtMethod(CtMethod<T> method) {
        String methodSignature = method.getDeclaringType().getQualifiedName() + "#" + method.getSignature();
        Set<String> calledMethods = new HashSet<>();
        method.getElements(e -> e instanceof CtInvocation)
                .forEach(e -> {
                    CtInvocation<?> invocation = (CtInvocation<?>) e;
                    CtExecutableReference<?> executable = invocation.getExecutable();
                    if (executable.getDeclaringType() != null) {
                        //System.out.println("CtExecutableReference is " + executable);
                        String calledMethodSignature = executable.getDeclaringType().getQualifiedName() + "#" + executable.getSignature();
                        calledMethods.add(calledMethodSignature);
                    }
                });
        callHierarchy.put(methodSignature, calledMethods);

    }

    public Map<String, Set<String>> getCallHierarchy() {
        return callHierarchy;
    }

}
