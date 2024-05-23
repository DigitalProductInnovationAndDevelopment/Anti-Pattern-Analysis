package tum.dpid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        String projectPath = "../fromItestra/LoopAntiPattern"; // Specific project directory
        //"C:/Users/cagat/OneDrive/Belgeler/GitHub/fromItestra/LoopAntiPattern";
        List<File> javaFiles = listJavaFiles(new File(projectPath));

        ASTAnalyzer analyzer = new ASTAnalyzer();
        System.out.println("Analyzing: ");
        for (File file : javaFiles) {
            System.out.println(file.getName());
            analyzer.analyzeFile(file.getAbsolutePath());
        }
        System.out.println("Analysis complete!");
    }

    private static List<File> listJavaFiles(File root) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = root.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(listJavaFiles(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }
        return javaFiles;
    }
}
