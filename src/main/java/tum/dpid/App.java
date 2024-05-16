package tum.dpid;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Path;
import java.util.*;

public class App {

    // List of database-related methods for analysis
    private static final Set<String> DATABASE_METHODS = new HashSet<>(Arrays.asList(
            "flush", "save", "update", "persist", "remove", "find"
    ));

    public static void main(String[] args) throws Exception {
        // Set the source directory path for Java files
        Path sourceDirectory = Path.of("../LoopAntiPattern/src");
        SourceRoot sourceRoot = new SourceRoot(sourceDirectory);

        // Parse all Java files to create a map of all method declarations
        Map<String, MethodDeclaration> methodMap = new HashMap<>();

        sourceRoot.parse("", (localPath, absolutePath, result) -> {
            if (result.isSuccessful()) {
                result.getResult().ifPresent(cu -> cu.findAll(MethodDeclaration.class).forEach(method -> {
                    String methodSignature = getMethodSignature(method);
                    methodMap.put(methodSignature, method);
                }));
            }
            return SourceRoot.Callback.Result.DONT_SAVE;
        });

        // Analyze which methods lead to database interactions
        Set<String> methodsLeadingToDatabase = new HashSet<>();

        for (String methodSignature : methodMap.keySet()) {
            if (leadsToDatabaseInteraction(methodSignature, methodMap, new HashSet<>())) {
                methodsLeadingToDatabase.add(methodSignature);
            }
        }

        // Check if these methods are called within loops or stream-based operations
        Set<String> methodsCalledInLoops = new HashSet<>();

        for (MethodDeclaration method : methodMap.values()) {
            if (isCalledInLoopOrStream(method, methodMap, methodsLeadingToDatabase, new HashSet<>())) {
                methodsCalledInLoops.add(getMethodSignature(method));
            }
        }

        System.out.println("Methods that lead to database interactions:");
        methodsLeadingToDatabase.forEach(System.out::println);

        System.out.println("Methods called within loops or stream-based operations:");
        methodsCalledInLoops.forEach(System.out::println);
    }

    // Function to determine if a method ultimately leads to database interactions
    private static boolean leadsToDatabaseInteraction(String methodName, Map<String, MethodDeclaration> methodMap, Set<String> visitedMethods) {
        if (visitedMethods.contains(methodName)) {
            return false;  // Avoid recursion or infinite loops
        }

        visitedMethods.add(methodName);

        MethodDeclaration method = methodMap.get(methodName);
        if (method == null) {
            return false;
        }

        List<MethodCallExpr> methodCalls = new ArrayList<>();
        method.walk(MethodCallExpr.class, methodCalls::add);

        for (MethodCallExpr methodCall : methodCalls) {
            String calledMethod = getMethodSignatureFromCall(methodCall);

            if (calledMethod != null) {
                // If this method call leads to database interactions, return true
                if (DATABASE_METHODS.stream().anyMatch(calledMethod::contains)) {
                    return true;
                }

                if (leadsToDatabaseInteraction(calledMethod, methodMap, visitedMethods)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Function to determine if a method is ultimately called within a loop or stream-based structure
    private static boolean isCalledInLoopOrStream(MethodDeclaration method, Map<String, MethodDeclaration> methodMap, Set<String> targetMethods, Set<String> visitedMethods) {
        if (visitedMethods.contains(getMethodSignature(method))) {
            return false;  // Avoid recursion
        }

        visitedMethods.add(getMethodSignature(method));

        if (method.getBody().isEmpty()) {
            return false;
        }

        BlockStmt body = method.getBody().get();
        boolean isInLoopOrStream = false;

        // Check for loop structures and stream-based operations
        for (var statement : body.getStatements()) {
            if (statement instanceof ForStmt || statement instanceof ForEachStmt || statement instanceof WhileStmt) {
                isInLoopOrStream = true;
            }

            List<MethodCallExpr> methodCalls = new ArrayList<>();
            statement.walk(MethodCallExpr.class, methodCalls::add);

            // Check if any method calls are within loop or stream structures
            for (MethodCallExpr methodCall : methodCalls) {
                String calledMethod = getMethodSignatureFromCall(methodCall);

                // Stream API method names to check for
                List<String> streamMethods = Arrays.asList("forEach", "map", "filter");

                boolean isStreamOperation = streamMethods.contains(methodCall.getNameAsString());

                if (targetMethods.contains(calledMethod) && (isInLoopOrStream || isStreamOperation)) {
                    return true;
                }

                MethodDeclaration calledDeclaration = methodMap.get(calledMethod);
                if (calledDeclaration != null) {
                    if (isCalledInLoopOrStream(calledDeclaration, methodMap, targetMethods, visitedMethods)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Function to get a method signature from a method call expression
    private static String getMethodSignatureFromCall(MethodCallExpr methodCall) {
        var optionalScope = methodCall.getScope();
        return optionalScope.map(expression -> expression + "." + methodCall.getNameAsString()).orElse(null);
    }

    // Function to generate a method signature from a method declaration
    private static String getMethodSignature(MethodDeclaration method) {
        String methodName = method.getNameAsString();
        List<String> parameterTypes = new ArrayList<>();
        method.getParameters().forEach(param -> parameterTypes.add(param.getType().asString()));

        return methodName + "(" + String.join(", ", parameterTypes) + ")";
    }
}
