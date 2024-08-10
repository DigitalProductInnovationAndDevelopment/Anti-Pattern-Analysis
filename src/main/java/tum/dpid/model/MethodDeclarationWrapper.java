package tum.dpid.model;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MethodDeclarationWrapper {

    private boolean excluded;
    private String declaringClass;
    private int lineNumber;
    private int columnNumber;
    private MethodDeclaration declaration;

    public MethodDeclarationWrapper(boolean excluded, String declaringClass, int lineNumber, int columnNumber, MethodDeclaration declaration) {
        this.excluded = excluded;
        this.declaringClass = declaringClass;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.declaration = declaration;
    }

    public boolean isExcluded() {
        return excluded;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
    }

    public MethodDeclaration getDeclaration() {
        return declaration;
    }

    public void setDeclaration(MethodDeclaration declaration) {
        this.declaration = declaration;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
}
