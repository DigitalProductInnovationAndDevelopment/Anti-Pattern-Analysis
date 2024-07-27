package tum.dpid.services;

import tum.dpid.model.resources.MethodExecutionDetails;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
public class DynamicAnalyzer {
    private final Map<String, List<MethodExecutionDetails>> methodDetailsMap = new HashMap<>();
    //private String csvFilePath ;//= "src/main/resources/snapshot.csv";
    public DynamicAnalyzer(String csvFilePath) throws IOException {

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFilePath))) {
            boolean firstLine = true;
            String [] values;
            while ((values = csvReader.readNext()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                for (int i = 0; i < values.length; i++) {
                    values[i] = values[i].trim().replaceAll("^\"|\"$", "");
                }

                String name = values[0].trim();
                double totalTime = parseTime(values[1].trim());
                double totalTimeCpu = parseTime(values[2].trim());
                int hits = Integer.parseInt(values[3].trim());

                System.out.println("Name: " + name  + " total-time: " + totalTime + " totalTimeCpu: " + totalTimeCpu + " hits: "+ hits);

                MethodExecutionDetails methodExecutionDetails = new MethodExecutionDetails(name, totalTime, totalTimeCpu, hits);

                methodDetailsMap.computeIfAbsent(name, k -> new ArrayList<>()).add(methodExecutionDetails);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
    private double parseTime(String timeStr) {
        String numericPart = timeStr.split(" ")[0].replace(",", "");;
        return Double.parseDouble(numericPart);
    }

    public Map<String, List<MethodExecutionDetails>> getMethodDetailsMap() {
        return methodDetailsMap;
    }

    public List<MethodExecutionDetails> getMethodExecutionDetails(String methodSignature){
        return methodDetailsMap.get(methodSignature);
    }

    public boolean checkAntiPattern(String methodSignature){
        List<MethodExecutionDetails> methodExecutionDetailsList = methodDetailsMap.get(methodSignature);
        for (MethodExecutionDetails details: methodExecutionDetailsList) {
            if (details.getTotalTime() >= 2000){
                return true;
            }
        }
        return false;
    }

}
