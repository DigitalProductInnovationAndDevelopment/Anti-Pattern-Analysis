package tum.dpid.processor;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseMethodFinder {

    public static Set<CtMethod<?>> findDatabaseMethods(CtModel model, List<CtClass<?>> allClasses) {
        Set<CtMethod<?>> databaseMethods = new HashSet<>();

        for (CtClass<?> ctClass : allClasses) {
            if (isDatabaseClass(ctClass)) {
                for(CtMethod<?> method : ctClass.getMethods()) {
                    if (!isGetterOrSetter(method)) {
                        databaseMethods.add(method);
                    }
                }
            }
        }
        return databaseMethods;
    }

    private static boolean isDatabaseClass(CtClass<?> ctClass) {
        // Define the package name where database-related classes are located
        String databasePackage = "com.example.LoopAntiPattern.data.repository";
        return ctClass.getPackage().getQualifiedName().startsWith(databasePackage);
    }

    private static boolean isGetterOrSetter(CtMethod<?> method) {
        String methodName = method.getSimpleName();
        boolean isStatic = method.getModifiers().contains(ModifierKind.STATIC);

        // Check for getter method
        boolean isGetter = !isStatic && method.getParameters().isEmpty() &&
                (methodName.startsWith("get") || methodName.startsWith("is"));

        // Check for setter method
        boolean isSetter = !isStatic && method.getParameters().size() == 1 &&
                methodName.startsWith("set");

        return isGetter || isSetter;
    }
}
