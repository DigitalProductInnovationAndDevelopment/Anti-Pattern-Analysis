package tum.dpid.jdt;


import org.eclipse.jdt.core.dom.*;

public class DeclarationVisitor extends ASTVisitor {

    //    @Override
//    public boolean visit(TypeDeclaration node) {
//        System.out.println("Type Declaration: " + node.getName());
//        return true;
//    }
//
//    @Override
//    public boolean visit(FieldDeclaration node) {
//        System.out.println("Field Declaration: " + node.fragments().get(0));
//        return true;
//    }
//
//    @Override
//    public boolean visit(MethodDeclaration node) {
//        System.out.println("Method Declaration: " + node.getName());
//        return true;
//    }
//
//    @Override
//    public boolean visit(IfStatement node) {
//        System.out.println("IfStatement Declaration: " + node.getExpression() );
//        return true;
//    }
    @Override
    public boolean visit(MarkerAnnotation node) {
        if (node.toString().equals("@Repository") || node.toString().equals("@Service")) {
            TypeDeclaration type = (TypeDeclaration) node.getParent();
            System.out.println("Annotated Class: " + type.getName().getFullyQualifiedName());

            for (MethodDeclaration method : type.getMethods()) {
                method.accept(new ASTVisitor() {
                    @Override
                    public boolean visit(ForStatement forStatement) {
                        System.out.println("For loop found in method: " + method.getName().getFullyQualifiedName());
                        return true;
                    }
                    @Override
                    public boolean visit(MethodInvocation node) {
                        String methodName = node.getName().getIdentifier();
                        if (methodName.equals("stream")) {
                            String expression = String.valueOf(node.getExpression());
                            System.out.println("Found stream() expression: " + expression );

                            if (expression.contains("repo") || expression.contains("Repo")){
                                System.out.println("Detected database call in loop! File name: " + type.getName().getFullyQualifiedName());
                            }
                        }
                        return super.visit(node);
                    }
                });
            }
        }


        return true;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        String methodName = node.getName().getIdentifier();
        if (methodName.equals("stream")) {
            System.out.println("Found stream() at line " + node.getStartPosition() + " expression is " + node.getExpression());
        }
        return super.visit(node);
    }



}
