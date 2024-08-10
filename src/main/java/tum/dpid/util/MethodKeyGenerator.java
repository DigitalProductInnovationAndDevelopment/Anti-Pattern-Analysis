package tum.dpid.util;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodKeyGenerator {

    public static String generateKey(MethodDeclaration methodDeclaration) {
        IMethodBinding binding = methodDeclaration.resolveBinding();
        if (binding != null) {
            return generateKey(binding);
        }
        return null;
    }

    private static String generateKey(IMethodBinding methodBinding) {
        String packageName = methodBinding.getDeclaringClass().getPackage().getName();
        String className = methodBinding.getDeclaringClass().getName();
        String methodName = methodBinding.getName();
        String methodSignature = generateMethodSignature(methodBinding);

        return packageName + "." + className + "#" + methodName + methodSignature;
    }

    private static String generateMethodSignature(IMethodBinding methodBinding) {
        StringBuilder signature = new StringBuilder("(");
        for (ITypeBinding paramType : methodBinding.getParameterTypes()) {
            signature.append(paramType.getName()).append(",");
        }
        if (signature.length() > 1) {
            signature.deleteCharAt(signature.length() - 1); // Remove trailing comma
        }
        signature.append(")");
        return signature.toString();
    }
}
