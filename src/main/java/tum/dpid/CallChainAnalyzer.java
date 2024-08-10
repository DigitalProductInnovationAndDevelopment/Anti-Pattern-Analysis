package tum.dpid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.jdt.core.dom.ASTParser;
import tum.dpid.config.AnalyzerConfig;
import tum.dpid.file.CallChainUtils;
import tum.dpid.file.FileUtils;
import tum.dpid.graph.CallChainVisitor;
import tum.dpid.graph.CallGraph;
import tum.dpid.model.CallChainEntity;
import tum.dpid.model.MethodDeclarationWrapper;
import tum.dpid.model.resources.AnalysisOutput;
import tum.dpid.model.resources.InvokedSubMethod;
import tum.dpid.parser.ASTGenerator;
import tum.dpid.parser.MethodExtractor;
import tum.dpid.services.DynamicAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        String projectDirectoryPath = config.getProjectDirectory();
        File projectDirectory = new File(projectDirectoryPath);

        if (!projectDirectory.exists() || !projectDirectory.isDirectory()) {
            System.out.println("Invalid project directory path.");
            return;
        }

        ASTParser parser = ASTGenerator.createParser();

        Set<String> DB_METHODS = new HashSet<>();
        List<String> directoryPaths = config.getThirdPartyMethodPaths();
        for (String directoryPath : directoryPaths) {
            Path dirPath = Paths.get(directoryPath);
            try {
                List<Path> javaFiles = FileUtils.getJavaFiles(dirPath);
                for (Path javaFile : javaFiles) {
                    DB_METHODS.addAll(MethodExtractor.extractMethodNames(projectDirectory, parser, javaFile));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Map<String, MethodDeclarationWrapper> methodMap = collectMethods(projectDirectory, parser, config.getExclusions());
            Map<String, List<String>> callGraph = CallGraph.buildCallGraph(methodMap);

            Set<CallChainEntity> allEntryPointsWithAntiPattern = new HashSet<>();
            DynamicAnalyzer dynamicAnalyzer = new DynamicAnalyzer(config.getSnapshotCsvFilePath());
            List<AnalysisOutput> analysisOutputList = new ArrayList<>();

            for (String targetMethod : DB_METHODS) {
                List<CallChainEntity> callChain = new ArrayList<>();
                CallChainVisitor.traceCallChainInOrder(targetMethod, null, callGraph, callChain, new HashSet<>(), methodMap);

                if (callChain.isEmpty()) {
                    System.out.println("No calls found for method: " + targetMethod);
                }

                List<List<CallChainEntity>> allCallChains = new ArrayList<>();
                for (CallChainEntity entity : callChain) {
                    if (allCallChains.isEmpty()) {
                        List<CallChainEntity> newCallChain = new ArrayList<>();
                        newCallChain.add(entity);
                        allCallChains.add(newCallChain);
                    }
                    List<CallChainEntity> tempChain = null;
                    for (List<CallChainEntity> callChainTemp : allCallChains) {
                        tempChain = new ArrayList<>(callChainTemp);
                        CallChainEntity entityFromAllChains = callChainTemp.get(callChainTemp.size() - 1);
                        if (entity.getInvokedMethod() != null && entity.getInvokedMethod().equals(entityFromAllChains.getName())) {
                            tempChain.add(entity);
                            break;
                        }
                    }
                    allCallChains.add(tempChain);
                }

                CallChainUtils.removeSublists(allCallChains);
                allCallChains.forEach(Collections::reverse);

                allCallChains.forEach(chain -> {
                    List<CallChainEntity> entitiesWithLoops = new ArrayList<>();
                    CallChainEntity entryMethod = chain.get(0);
                    boolean hasAntiPattern = false;
                    for (CallChainEntity callChainEntity : chain) {
                        if (callChainEntity.isInvokesChildInLoop() && !allEntryPointsWithAntiPattern.contains(entryMethod)) {
                            hasAntiPattern = true;
                            allEntryPointsWithAntiPattern.add(entryMethod);
                            entitiesWithLoops.add(callChainEntity);
                        }
                    }

                    if (hasAntiPattern) {
                        MethodDeclarationWrapper method = methodMap.get(entryMethod.getName());
                        String methodSignatureToCheck = method.getDeclaringClass() + "." + entryMethod.getName() + " ()";
                        //System.out.println("AntiPattern Entry point "+ methodSignatureToCheck);

                        System.out.println("Call chain starting with method " + entryMethod.getName() + " at position " + method.getLineNumber() + ":" + method.getColumnNumber() + " in file: " + method.getDeclaringClass() + " has a loop anti pattern!");
                        AnalysisOutput analysisOutput = new AnalysisOutput(entryMethod.getName() , method.getDeclaringClass(), method.getLineNumber(), method.getColumnNumber(), dynamicAnalyzer.getFunctionAvgTime(methodSignatureToCheck), AnalysisOutput.AnalysisType.STATIC, AnalysisOutput.Severity.LOW);

                        boolean foundAntiPatternInDynamicAnalysis = dynamicAnalyzer.checkAntiPattern(methodSignatureToCheck, config.getMethodExecutionThresholdMs());
                        if (foundAntiPatternInDynamicAnalysis){
                            //analysisOutput.setExecutionTime(dynamicAnalyzer.getFunctionAvgTime(methodSignatureToCheck));
                            analysisOutput.setAnalysisType(AnalysisOutput.AnalysisType.BOTH);
                            analysisOutput.setSeverity(AnalysisOutput.Severity.HIGH);
                        }

                        List<InvokedSubMethod> invokedSubMethodArrayList = new ArrayList<>();
                        entitiesWithLoops.forEach(entity -> {
                            MethodDeclarationWrapper entityMethod = methodMap.get(entity.getName());
                            System.out.println("\tMethod " + entity.getName() + " at position " + entityMethod.getLineNumber() + ":" + entityMethod.getColumnNumber() + " in file: " + entityMethod.getDeclaringClass() + " invokes method: " + entity.getInvokedMethod() + " in a LOOP!");
                            InvokedSubMethod invokedSubMethod = new InvokedSubMethod(entity.getName() , entityMethod.getDeclaringClass() , entityMethod.getLineNumber(), entityMethod.getColumnNumber() ,entity.getInvokedMethod());
                            invokedSubMethodArrayList.add(invokedSubMethod);
                        });
                        analysisOutput.setInvokedSubMethodDetails(invokedSubMethodArrayList);
                        analysisOutputList.add(analysisOutput);
                        System.out.println();
                    }
                });

            }

            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            try {
                File outputFile = new File("analysisOutput.json");
                mapper.writeValue(outputFile, analysisOutputList);
                System.out.println("Analysis output is available at: " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
