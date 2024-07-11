package tum.dpid.processor;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MethodCallTracer {

    public static Set<CtMethod<?>> findMethodsCallingDatabaseMethods(CtModel model, List<CtMethod<?>> allMethods, Set<CtMethod<?>> databaseMethods) {
        Set<CtMethod<?>> callingMethods = new HashSet<>();

        for (CtMethod<?> method : allMethods) {
            for (CtExecutableReference<?> calledMethodRef : method.getElements(new TypeFilter<>(CtExecutableReference.class))) {
                for (CtMethod<?> dbMethod : databaseMethods) {
                    if (calledMethodRef.getSimpleName().equals(dbMethod.getSimpleName()) &&
                            calledMethodRef.getDeclaringType().equals(dbMethod.getDeclaringType().getReference())) {
                        callingMethods.add(method);
                    }
                }
            }
        }
        return callingMethods;
    }


    public static Set<String> traceMethodCalls(Set<CtMethod<?>> databaseMethods, List<CtMethod<?>> allMethods) {

        Set<String> callChains = new HashSet<>();
        Set<CtMethod<?>> visitedMethods = new HashSet<>();

        for (CtMethod<?> dbMethod : databaseMethods) {
            traceMethod(dbMethod, allMethods, "", callChains, visitedMethods);
        }

        return callChains;
    }

    private static void traceMethod(CtMethod<?> method, List<CtMethod<?>> allMethods, String callChain, Set<String> callChains, Set<CtMethod<?>> visitedMethods) {
        if (visitedMethods.contains(method)) {
            return;
        }
        visitedMethods.add(method);

        callChain = method.getSimpleName() + " #" + method.getDeclaringType().getQualifiedName() + " -> " + callChain;

        for (CtMethod<?> caller : findCallingMethods(method, allMethods)) {
            traceMethod(caller, allMethods, callChain, callChains, visitedMethods);
        }
        callChains.add(callChain);

    }


    private static Set<CtMethod<?>> findCallingMethods(CtMethod<?> method, List<CtMethod<?>> allMethods) {
        Set<CtMethod<?>> callingMethods = new HashSet<>();

        for (CtMethod<?> candidateMethod : allMethods) {
            for (CtInvocation<?> invocation : candidateMethod.getElements(new TypeFilter<>(CtInvocation.class))) {
                CtExecutableReference<?> executableRef = invocation.getExecutable();
                if (executableRef.getDeclaration() != null && executableRef.getDeclaration().equals(method)) {
                    callingMethods.add(candidateMethod);
                }
            }
        }

        return callingMethods;
    }

}
