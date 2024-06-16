package tum.dpid;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import tum.dpid.processor.CallHierarchyProcessor;

import java.util.Map;
import java.util.Set;


/**
 * Hello world!
 *
 */
public class App 
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


//        /**/
        CallHierarchyProcessor processor = new CallHierarchyProcessor();
        for (CtType<?> type : launcher.getFactory().Class().getAll()) {
            if (type instanceof CtClass) {
                for (CtMethod<?> method : ((CtClass<?>) type).getMethods()) {
                    processor.visitCtMethod(method);
                }
            }
        }

        Map<String, Set<String>> callHierarchy = processor.getCallHierarchy();

        callHierarchy.forEach((method, calls) -> {
            System.out.println(method + " calls:");
            StringBuilder indent = new StringBuilder("---");
            for (String call : calls) {
                System.out.println(indent + "  " + call);
                indent.append("---");
            }
        });

    }

}

