package tum.dpid.services.v1;

import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
    Previous version of method call chain processor.
 */
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

//Runner Code Snippet
//        Set<CtMethod<?>> callingMethods = MethodCallTracer.findMethodsCallingDatabaseMethods(model,allMethods, databaseMethods);
//        for (CtMethod<?> method : callingMethods) {
//            System.out.println("Method calling database: " + method.getSignature());
//        }

//
//        //Method Call Tracer File
//        Set<String> callChains = MethodCallTracer.traceMethodCalls(databaseMethods, allMethods);
//        // Print results
//        for (String callChain : callChains) {
//            System.out.println("Call chain: " + callChain);
//        }
