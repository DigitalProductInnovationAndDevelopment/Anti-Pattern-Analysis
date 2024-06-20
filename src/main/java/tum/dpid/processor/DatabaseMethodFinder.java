package tum.dpid.processor;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseMethodFinder {

    public static Set<CtMethod<?>> findDatabaseMethods(CtModel model, List<CtClass<?>> allClasses) {
        Set<CtMethod<?>> databaseMethods = new HashSet<>();

        for (CtClass<?> ctClass : allClasses) {
            if (isDatabaseClass(ctClass)) {
                databaseMethods.addAll(ctClass.getMethods());
            }
        }
        return databaseMethods;
    }

    private static boolean isDatabaseClass(CtClass<?> ctClass) {
        // Define the package name where database-related classes are located
        String databasePackage = "com.example.LoopAntiPattern.data.repository";
        return ctClass.getPackage().getQualifiedName().startsWith(databasePackage);
    }
}
