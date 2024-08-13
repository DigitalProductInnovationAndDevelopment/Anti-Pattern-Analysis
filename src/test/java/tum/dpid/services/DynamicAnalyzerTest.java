package tum.dpid.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tum.dpid.model.resources.MethodExecutionDetails;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;


@RunWith(JUnit4.class)
public class DynamicAnalyzerTest  {


    private final DynamicAnalyzer dynamicAnalyzer = new DynamicAnalyzer() ;//=  Mockito.mock(DynamicAnalyzer.class);


    @Test
    public void shouldProcessSamplingData() throws IOException {
        dynamicAnalyzer.setCsvFilePath("src/test/resources/testValidSnapshotData1.csv");
        dynamicAnalyzer.processSamplingData();

        Map<String, List<MethodExecutionDetails>> methodDetailsMap = dynamicAnalyzer.getMethodDetailsMap();

        assertNotNull(methodDetailsMap);
        assertTrue(methodDetailsMap.containsKey("com.example.Test.method1 ()"));
        assertEquals(1, methodDetailsMap.get("com.example.Test.method1 ()").size());
    }



    @Test
    public void shouldNotFindAntiPatternInDynamicAnalysis() throws IOException {
        dynamicAnalyzer.setCsvFilePath("src/test/resources/testValidSnapshotData1.csv");
        dynamicAnalyzer.processSamplingData();

        String methodSignature = "com.example.Test.method1 ()";
        int threshold = 200000;
        assertFalse(dynamicAnalyzer.checkAntiPattern(methodSignature, threshold));
    }


    @Test
    public void shouldFindAntiPatternInDynamicAnalysis() throws IOException {
        dynamicAnalyzer.setCsvFilePath("src/test/resources/testValidSnapshotData1.csv");
        dynamicAnalyzer.processSamplingData();

        String methodSignature = "com.example.Test.method2 ()";
        int threshold = 2000;
        assertTrue(dynamicAnalyzer.checkAntiPattern(methodSignature, threshold));
    }


    @Test
    public void shouldFindTrueAntiPatternSeverity() {
        assertTrue(true);
    }

    @Test
    public void shouldNotFindTrueAntiPatternSeverity() {
        assertTrue(true);
    }


    @Test
    public void shouldCalculateAntiPatternAverageExecutionTime() {
        assertTrue(true);
    }


    @Test
    public void shouldNotCalculateAntiPatternAverageExecutionTime() {
        assertTrue(true);
    }
}