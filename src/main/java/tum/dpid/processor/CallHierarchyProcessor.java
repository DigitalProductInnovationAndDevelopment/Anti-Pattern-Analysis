package tum.dpid.processor;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtAbstractVisitor;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.*;
import java.util.stream.Collectors;
import spoon.reflect.visitor.PrinterHelper;


public class CallHierarchyProcessor extends CtAbstractVisitor {
    private Map<String, Set<String>> callHierarchy = new HashMap<>();

    //ToDo add recursion
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
