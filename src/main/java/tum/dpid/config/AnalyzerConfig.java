package tum.dpid.config;

import java.util.List;

/**
 * class that represents the json config values as a java class
 */
public class AnalyzerConfig {

    private String projectDirectory;
    private List<String> thirdPartyMethodPaths;
    private List<String> exclusions;

    public AnalyzerConfig() {}

    public AnalyzerConfig(String projectDirectory, List<String> thirdPartyMethodPaths, List<String> exclusions) {
        this.projectDirectory = projectDirectory;
        this.thirdPartyMethodPaths = thirdPartyMethodPaths;
        this.exclusions = exclusions;
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
}
