package tum.dpid.config;

import java.util.List;

/**
 * class that represents the json config values as a java class
 */
public class AnalyzerConfig {

    private String projectDirectory;
    private List<String> thirdPartyMethodPaths;
    private List<String> exclusions;

    private String snapshotCsvFilePath;

    private Integer methodExecutionThresholdMs;
    public AnalyzerConfig() {}

    public AnalyzerConfig(String projectDirectory, List<String> thirdPartyMethodPaths, List<String> exclusions, String snapshotCsvFilePath, Integer methodExecutionThresholdMs) {
        this.projectDirectory = projectDirectory;
        this.thirdPartyMethodPaths = thirdPartyMethodPaths;
        this.exclusions = exclusions;
        this.snapshotCsvFilePath = snapshotCsvFilePath;
        this.methodExecutionThresholdMs = methodExecutionThresholdMs;
    }

    public String getProjectDirectory() {
        return projectDirectory;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public List<String> getThirdPartyMethodPaths() {
        return thirdPartyMethodPaths;
    }

    public void setThirdPartyMethodPaths(List<String> thirdPartyMethodPaths) {
        this.thirdPartyMethodPaths = thirdPartyMethodPaths;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }

    public String getSnapshotCsvFilePath() {
        return snapshotCsvFilePath;
    }

    public void setSnapshotCsvFilePath(String snapshotCsvFilePath) {
        this.snapshotCsvFilePath = snapshotCsvFilePath;
    }

    public Integer getMethodExecutionThresholdMs() {
        return methodExecutionThresholdMs;
    }

    public void setMethodExecutionThresholdMs(Integer methodExecutionThresholdMs) {
        this.methodExecutionThresholdMs = methodExecutionThresholdMs;
    }
}
