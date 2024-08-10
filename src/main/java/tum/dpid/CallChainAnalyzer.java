package tum.dpid;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import tum.dpid.config.AnalyzerConfig;
import tum.dpid.file.FileUtils;
import tum.dpid.graph.CallGraph;
import tum.dpid.graph.CallGraphVisualizer;
import tum.dpid.parser.LoopVisitor;
import tum.dpid.parser.MethodExtractor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static tum.dpid.parser.MethodCollector.collectMethods;

public class CallChainAnalyzer {

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();
        AnalyzerConfig config;

        try {
            config = mapper.readValue(new File("config.json"), AnalyzerConfig.class);
        } catch (IOException e) {
            System.out.println("Error reading config file:\n" + e.getMessage());
            System.exit(1);
            return;
        }

        String directoryPath = config.getRepositoryDirectory();
        Path dirPath = Paths.get(directoryPath);
        List<String> DB_METHODS = new ArrayList<>();

        try {
            List<Path> javaFiles = FileUtils.getJavaFiles(dirPath);
            for (Path javaFile : javaFiles) {
                DB_METHODS.addAll(MethodExtractor.extractMethodNames(javaFile, config.getExcludedClasses(), config.getExcludedMethods()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String projectDirectoryPath = config.getProjectDirectory();
        File projectDirectory = new File(projectDirectoryPath);

        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        try {
            Map<String, MethodDeclaration> methodMap = collectMethods(projectDirectory, config.getExcludedMethods());
            Map<String, List<String>> callGraph = CallGraph.buildCallGraph(methodMap);

            for (String targetMethod : DB_METHODS) {
                List<String> callChain = new ArrayList<>();
                CallGraph.traceCallChainInOrder(targetMethod, callGraph, new HashSet<>(), callChain, methodMap);

                if (callChain.isEmpty()) {
                    System.out.println("No calls found for method: " + targetMethod);
                }

                System.out.println("Call graph for method: " + targetMethod);
                CallGraphVisualizer.drawCallGraph(targetMethod, callGraph, 0, new HashSet<>());
                System.out.println();
            }
            LoopVisitor.checkForDatabaseCallsInLoops(methodMap, callGraph, DB_METHODS);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
