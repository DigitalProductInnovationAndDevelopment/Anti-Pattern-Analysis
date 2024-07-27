package tum.dpid.config;

import java.util.List;

/**
 * class that represents the json config values as a java class
 */
public class AnalyzerConfig {

    private String projectDirectory;
    private String repositoryDirectory;
    private List<String> excludedClasses;
    private List<String> excludedMethods;

    private String snapshotCsvFilePath;
    public AnalyzerConfig() {}

    public AnalyzerConfig(String projectDirectory, String repositoryDirectory, List<String> excludedClasses, List<String> excludedMethods, String snapshotCsvFilePath) {
        this.projectDirectory = projectDirectory;
        this.repositoryDirectory = repositoryDirectory;
        this.excludedClasses = excludedClasses;
        this.excludedMethods = excludedMethods;
        this.snapshotCsvFilePath = snapshotCsvFilePath;
    }

    public String getProjectDirectory() {
        return projectDirectory;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public String getRepositoryDirectory() {
        return repositoryDirectory;
    }

    public void setRepositoryDirectory(String repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
    }

    public List<String> getExcludedClasses() {
        return excludedClasses;
    }

    public void setExcludedClasses(List<String> excludedClasses) {
        this.excludedClasses = excludedClasses;
    }

    public List<String> getExcludedMethods() {
        return excludedMethods;
    }

    public void setExcludedMethods(List<String> excludedMethods) {
        this.excludedMethods = excludedMethods;
    }

    public String getSnapshotCsvFilePath() {
        return snapshotCsvFilePath;
    }

    public void setSnapshotCsvFilePath(String snapshotCsvFilePath) {
        this.snapshotCsvFilePath = snapshotCsvFilePath;
    }
}
