package tum.dpid.services.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.reflect.declaration.CtClassImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClassHierarchyOrder extends AbstractProcessor<CtClassImpl<?>> {

    private final Map<CtTypeReference<?>, Set<CtTypeReference<?>>> classImplementors = new HashMap<>();

    public void findInheritance(CtTypeReference<?> classRef, CtTypeReference<?> superClass) {
        Set<CtTypeReference<?>> subclasses = classImplementors.computeIfAbsent(superClass, k -> new HashSet<>());
        subclasses.add(classRef);
    }

    @Override
    public void process(CtClassImpl ctClass) {
        if (ctClass.getReference().isAnonymous()) {
            return;
        }
        if (ctClass.getSuperclass() != null) {
            findInheritance(ctClass.getReference(), ctClass.getSuperclass());
        }
        for (Object o : ctClass.getSuperInterfaces()) {
            CtTypeReference<?> superclass = (CtTypeReference<?>) o;
            findInheritance(ctClass.getReference(), superclass);
        }
    }

    public Map<CtTypeReference<?>, Set<CtTypeReference<?>>> getClassImplementors() {
        return classImplementors;
    }

}
