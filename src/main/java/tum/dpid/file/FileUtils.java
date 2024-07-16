package tum.dpid.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
    public static List<Path> getJavaFiles(Path dirPath) throws IOException {
        return Files.walk(dirPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .toList();
    }

    public static List<File> getJavaFilesRecursively(File directory) {
        List<File> javaFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    javaFiles.addAll(getJavaFilesRecursively(file));
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file);
                }
            }
        }

        return javaFiles;
    }
}