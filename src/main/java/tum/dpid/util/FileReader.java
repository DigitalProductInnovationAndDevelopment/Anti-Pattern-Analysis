package tum.dpid.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class FileReader {

    public static String readSourceFromFile(File file) {
        StringBuilder sourceCodeBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sourceCodeBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return sourceCodeBuilder.toString();
    }
}
