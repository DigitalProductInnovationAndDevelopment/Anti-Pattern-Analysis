package tum.dpid;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import tum.dpid.processor.DatabaseMethodFinder;
import tum.dpid.processor.MethodCallTracer;

import java.util.List;
import java.util.Set;


/**
 * Hello world!
 *
 */
public class Runner
{
    public static void main( String[] args )
    {
        Launcher launcher =new Launcher();
        launcher.getEnvironment().setNoClasspath(true);

        //String testDirectory = "src/main/resources/tester"; //"../../../resources/tester";
        String directoryPath = "../../fromItestra/LoopAntiPattern";
        launcher.addInputResource(directoryPath);
        launcher.buildModel();
        CtModel model = launcher.getModel();

        List<CtClass<?>> allClasses = model.getElements(new TypeFilter<>(CtClass.class));
        List<CtMethod<?>> allMethods = model.getElements(new TypeFilter<>(CtMethod.class));
        // Step 1: Identify Database Methods

        Set<CtMethod<?>> databaseMethods = DatabaseMethodFinder.findDatabaseMethods(model, allClasses);

        // Step 2: Trace Method Calls
        Set<CtMethod<?>> callingMethods = MethodCallTracer.findMethodsCallingDatabaseMethods(model,allMethods, databaseMethods);

        // Print results
        for (CtMethod<?> method : callingMethods) {
            System.out.println("Method calling database: " + method.getSignature());
        }

        Set<String> callChains = MethodCallTracer.traceMethodCalls(databaseMethods, allMethods);
        // Print results
        for (String callChain : callChains) {
            System.out.println("Call chain: " + callChain);
        }
//
////        /*First Degree Caller*/
//        CallHierarchyProcessor processor = new CallHierarchyProcessor();
//        for (CtType<?> type : launcher.getFactory().Class().getAll()) {
//            if (type instanceof CtClass) {
//                for (CtMethod<?> method : ((CtClass<?>) type).getMethods()) {
//                    processor.visitCtMethod(method);
//                }
//            }
//        }
//
//        Map<String, Set<String>> callHierarchy = processor.getCallHierarchy();
//
//        callHierarchy.forEach((method, calls) -> {
//            System.out.println(method + " calls:");
//            StringBuilder indent = new StringBuilder("---");
//            for (String call : calls) {
//                System.out.println(indent + "  " + call);
//                indent.append("---");
//            }
//        });

    }

}

