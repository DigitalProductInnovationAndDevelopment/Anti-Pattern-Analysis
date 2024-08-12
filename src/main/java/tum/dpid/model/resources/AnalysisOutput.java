package tum.dpid.model.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalysisOutput {

    private String methodName;

    private String className;

    private Integer lineNumber;

    private Integer columnNumber;

    private Double executionTime;

    /**
     * Type of analysis where antipattern found. It might be resulted of dynamic, static or both analysis
     */
    private AnalysisType analysisType;

    private Severity severity;

    /**
     * List of invoked methods called in Loop
     */
    private List<InvokedSubMethod> invokedSubMethodDetails;

    public AnalysisOutput() {}

    public AnalysisOutput(String methodName, String className, Integer lineNumber, Integer columnNumber, Double executionTime, AnalysisType analysisType, Severity severity) {
        this.methodName = methodName;
        this.className = className;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.executionTime = executionTime;
        this.analysisType = analysisType;
        this.severity = severity;
        this.invokedSubMethodDetails = new ArrayList<>();
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

    public Double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Double executionTime) {
        this.executionTime = executionTime;
    }

    public AnalysisType getAnalysisType() {
        return analysisType;
    }

    public void setAnalysisType(AnalysisType analysisType) {
        this.analysisType = analysisType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public Integer getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(Integer columnNumber) {
        this.columnNumber = columnNumber;
    }

    public List<InvokedSubMethod> getInvokedSubMethodDetails() {
        return invokedSubMethodDetails;
    }

    public void setInvokedSubMethodDetails(List<InvokedSubMethod> invokedSubMethodDetails) {
        this.invokedSubMethodDetails = invokedSubMethodDetails;
    }


    /**
     * If method execution time exceeds threshold more than or equal to half of executions: HIGH
     * If method execution time exceeds threshold less than  half of executions and more than once: MEDIUM
     * If method execution time does not exceed but found anti-pattern in static analysis: LOW
     * If method does not execute during sampling or does not exist: NONE
     */
    public enum Severity {
        HIGH(2),
        MEDIUM(1),
        LOW(0),
        NONE(-1);

        private final int level;

        Severity(int level) {
            this.level = level;
        }
        public static Severity fromIntValue(int level) {
            return Arrays.stream(Severity.values())
                    .filter(s -> s.level == level)
                    .findFirst()
                    .orElse(Severity.NONE);
        }
        public int getLevel() {
            return level;
        }
    }
    /**
     * If method found  in static analysis and does not exceed certain threshold: STATIC
     * If method only exists in dynamic analysis: DYNAMIC
     * If method found in both of analysis: BOTH
     */
    public enum AnalysisType {
        STATIC,
        DYNAMIC,
        BOTH
    }
}
