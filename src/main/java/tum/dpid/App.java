package tum.dpid;

import tum.dpid.jdt.Parser;

import java.io.File;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args) {
        //to be parsed project path
        String directoryPath = "../../fromItestra/LoopAntiPattern";
        File directory = new File(directoryPath);

        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Invalid directory path");
            return;
        }
        Parser.processJavaFiles(directory);
    }
}
