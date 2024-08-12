package tum.dpid.services;

import tum.dpid.model.resources.MethodExecutionDetails;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
public class DynamicAnalyzer {

    private String csvFilePath;

    private final Map<String, List<MethodExecutionDetails>> methodDetailsMap = new HashMap<>();

    public DynamicAnalyzer() {}

    public DynamicAnalyzer(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    public void processSamplingData() throws IOException {

        try (CSVReader csvReader = new CSVReader(new FileReader(this.csvFilePath))) {
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
                Double totalTime = parseTime(values[1].trim());
                Double totalTimeCpu = parseTime(values[2].trim());
                int hits;
                if (values[3].contains(","))
                    hits = Integer.parseInt(values[3].trim().replace(",", ""));
                else
                    hits = Integer.parseInt(values[3].trim());

                //System.out.println("Name: " + name  + " total-time: " + totalTime + " totalTimeCpu: " + totalTimeCpu + " hits: "+ hits);

                MethodExecutionDetails methodExecutionDetails = new MethodExecutionDetails(name, totalTime, totalTimeCpu, hits);

                methodDetailsMap.computeIfAbsent(name, k -> new ArrayList<>()).add(methodExecutionDetails);
            }
        } catch (IOException e) {
           throw new IOException(e);
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
    }
    private double parseTime(String timeStr) {
        String numericPart = timeStr.split(" ")[0].replace(",", "");
        return Double.parseDouble(numericPart);
    }

    public Map<String, List<MethodExecutionDetails>> getMethodDetailsMap() {
        return methodDetailsMap;
    }

    public List<MethodExecutionDetails> getMethodExecutionDetails(String methodSignature){
        return methodDetailsMap.get(methodSignature);
    }

    /**
     *
     * @param methodSignature package name + method name
     * @return if statically found antipattern exceed certain threshold
     */
    public boolean checkAntiPattern(String methodSignature){
        return methodDetailsMap.containsKey(methodSignature);
    }


    /**
     *
     * @param methodSignature package name + method name
     * @return average total execution time of method. If it is not executed during sampling, returns 0.0
     */
    public Double getFunctionAvgTime(String methodSignature){
        if (!methodDetailsMap.containsKey(methodSignature) || methodDetailsMap.get(methodSignature).isEmpty() || methodDetailsMap.isEmpty())
            return 0.0;

        OptionalDouble avgTime = methodDetailsMap.get(methodSignature).stream()
                 .mapToDouble(MethodExecutionDetails::getTotalTime).average();

        return avgTime.isPresent() ? avgTime.getAsDouble() : 0.0;
    }

    public String getCsvFilePath() {
        return csvFilePath;
    }

    public void setCsvFilePath(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }
}
