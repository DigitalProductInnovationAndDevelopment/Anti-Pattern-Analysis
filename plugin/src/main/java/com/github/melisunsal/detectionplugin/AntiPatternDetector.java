package com.github.melisunsal.detectionplugin;

public class AntiPatternDetector {
    public static void main(String[] args) {
        System.out.println("Analyzing code for anti-patterns...");
        System.out.println("Detected: For Loop Database Anti-Performance in Class: MyDatabaseService, Method: fetchAllRecords");
        System.out.println("    for (int i = 0; i < results.size(); i++) {");
        System.out.println("        // Process each record");
        System.out.println("    }");
        System.out.println("Detected: For Loop Database Anti-Performance in Class: UserService, Method: getUserData");
        System.out.println("    for (User user : users) {");
        System.out.println("        // Process user data");
        System.out.println("    }");
        System.out.println("Analysis complete.");
    }
}
