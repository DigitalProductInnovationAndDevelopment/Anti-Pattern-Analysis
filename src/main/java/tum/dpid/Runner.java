package tum.dpid;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import tum.dpid.services.DatabaseMethodFinder;
import tum.dpid.services.processors.ClassHierarchyOrder;
import tum.dpid.services.processors.MethodCallChain;
import tum.dpid.services.processors.MethodOrder;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Runner class of method call chain processor and analyzer by utilizing spoon
 *
 */
public class Runner
{

    public static void main( String[] args )
    {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);

        //String testDirectory = "src/main/resources/tester"; //"../../../resources/tester";
        String directoryPath = "../../fromItestra/LoopAntiPattern";
        launcher.addInputResource(directoryPath);
        launcher.buildModel();
        CtModel model = launcher.getModel();

        //All Java Classes in the project
        List<CtClass<?>> allClasses = model.getElements(new TypeFilter<>(CtClass.class));

        //All Methods in the project
        List<CtMethod<?>> allMethods = model.getElements(new TypeFilter<>(CtMethod.class));

        // Methods which makes request to database in project
        Set<CtMethod<?>> databaseMethods = DatabaseMethodFinder.findDatabaseMethods(model, allClasses);
        for (CtMethod<?> method : databaseMethods) {
            System.out.println("Database Method is: " + method.getSignature() + " (" + method.getDeclaringType().getQualifiedName() + ")");
        }

        //Initialize class hierarchy order processor and start processing it
        ClassHierarchyOrder classHierarchyOrder = new ClassHierarchyOrder();
        launcher.addProcessor(classHierarchyOrder);
        launcher.process();

        //Initialize  method execution order processor (call chain of method) and start processing it
        MethodOrder methodOrder = new MethodOrder();
        launcher.addProcessor(methodOrder);
        launcher.process();

        Map<CtExecutableReference<?>, List<CtExecutableReference<?>>> callList = methodOrder.getCallList();
        Map<CtTypeReference<?>, Set<CtTypeReference<?>>> classHierarchy = classHierarchyOrder.getClassImplementors() ;

        //Process each method in the project and print out their call chain
        for (CtMethod<?> ctMethod: allMethods) {
            List<MethodCallChain> methodCallHierarchies = MethodCallChain.processMethod(ctMethod, callList, classHierarchy);
            if (methodCallHierarchies.isEmpty()) {
                System.out.println("No method  `" + ctMethod.getDeclaringType() + "` found. \n");
            }
            if (methodCallHierarchies.size() > 1) {
                System.out.println("Found " + methodCallHierarchies.size() + " matching methods...\n");
            }
            for (MethodCallChain methodCallHierarchy : methodCallHierarchies) {
                methodCallHierarchy.printCallChain();
                System.out.println();
            }
        }
    }


}

