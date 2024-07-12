package tum.dpid.services.processors;

import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;

import java.util.*;

public class MethodCallChain {

    private final CtExecutableReference<?> executableReference;
    private final Map<CtExecutableReference<?> , List<CtExecutableReference<?> >> callList;
    private final Map<CtTypeReference<?> , Set<CtTypeReference<?> >> classHierarchy;

    private MethodCallChain(CtExecutableReference<?>  executableReference,
                            Map<CtExecutableReference<?> , List<CtExecutableReference<?> >> callList,
                            Map<CtTypeReference<?> , Set<CtTypeReference<?> >> classHierarchy) {
        this.executableReference = executableReference;
        this.callList = callList;
        this.classHierarchy = classHierarchy;
    }

    public static List<MethodCallChain> processMethod(CtMethod<?> methodName,
                                                      Map<CtExecutableReference<?> , List<CtExecutableReference<?>>> callList,
                                                      Map<CtTypeReference<?> , Set<CtTypeReference<?> >> classHierarchy) {
        ArrayList<MethodCallChain> result = new ArrayList<>();
        for (CtExecutableReference<?>  executableReference : findExecutablesForMethod(methodName, callList)) {
            result.add(new MethodCallChain(executableReference, callList, classHierarchy));
        }
        return result;
    }

    static List<CtExecutableReference<?> > findExecutablesForMethod(CtMethod<?> methodName, Map<CtExecutableReference<?> , List<CtExecutableReference<?> >> callList) {
        ArrayList<CtExecutableReference<?> > result = new ArrayList<>();
        for (CtExecutableReference<?>  executableReference : callList.keySet()) {
            if (executableReference.equals(methodName.getReference())){
                result.add(executableReference);
            }
        }
        return result;
    }

    public void printCallChain() {
        System.out.println("Method call hierarchy of " + executableReference + "");
        printCallChain(executableReference, "\t", new HashSet<CtExecutableReference<?> >());
    }

    private void printCallChain(CtExecutableReference<?>  method, String indents, Set<CtExecutableReference<?> > alreadyVisited) {
        if (alreadyVisited.contains(method)) {
            return;
        }
        alreadyVisited.add(method);
        List<CtExecutableReference<?> > callListForMethod = callList.get(method);
        if (callListForMethod == null) {
            return;
        }
        for (CtExecutableReference<?>  eachReference : callListForMethod) {
            System.out.println(indents + eachReference.toString());

            printCallChain(eachReference, indents.concat("\t"), alreadyVisited);
            Set<CtTypeReference<?> > subclasses = classHierarchy.get(eachReference.getDeclaringType());
            if (subclasses != null) {
                for (CtTypeReference<?>  subclass : subclasses) {
                    CtExecutableReference<?>  ctExecutableReference = eachReference.getOverridingExecutable(subclass);
                    if (ctExecutableReference != null) {
                        System.out.println(indents + "* " + ctExecutableReference.toString());
                        printCallChain( ctExecutableReference, indents.concat("\t"), alreadyVisited);
                    }
                }
            }
        }
    }
}
