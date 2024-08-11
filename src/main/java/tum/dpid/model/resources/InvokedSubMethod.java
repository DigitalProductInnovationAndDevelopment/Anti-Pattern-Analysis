package tum.dpid.model.resources;

public class InvokedSubMethod {

    private String methodName;

    private String className;

    private Integer lineNumber;

    private Integer columnNumber;

    private String invokedMethod;

    public InvokedSubMethod(){}

    public InvokedSubMethod(String methodName, String className, Integer lineNumber, Integer columnNumber, String invokedMethod) {
        this.methodName = methodName;
        this.className = className;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.invokedMethod = invokedMethod;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getInvokedMethod() {
        return invokedMethod;
    }

    public void setInvokedMethod(String invokedMethod) {
        this.invokedMethod = invokedMethod;
    }
}
