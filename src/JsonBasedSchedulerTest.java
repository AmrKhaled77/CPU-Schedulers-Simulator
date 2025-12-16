

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class JsonBasedSchedulerTest {

    Gson gson = new Gson();

    // ===================== SJF & RR JSON TESTS =====================
    @Test
    void testSJFAndRR_FromJSONFiles() throws Exception {

        for (int i = 1; i <= 5; i++) {

            String filePath =
                    "test_cases_updated/test_cases/Other_Schedulers/test_" + i + ".json";

            FileReader reader = new FileReader(filePath);
            TestFile testFile = gson.fromJson(reader, TestFile.class);
            reader.close();

            int cs = testFile.input.contextSwitch;
            int quantum = testFile.input.rrQuantum;

            ArrayList<Process> base =
                    buildProcesses(testFile.input.processes);

            // ----------- SJF -----------
            SJFResult sjfActual =
                    PreemptiveSJF.simulateSJF(cloneProcesses(base), cs);

            assertSchedulerResult(
                    "SJF test_" + i,
                    sjfActual,
                    testFile.expectedOutput.SJF
            );

            // ----------- RR -----------
            SJFResult rrActual =
                    RoundRobin.simulateRR(cloneProcesses(base), quantum, cs);

            assertSchedulerResult(
                    "RR test_" + i,
                    rrActual,
                    testFile.expectedOutput.RR
            );
        }
    }

    // ===================== AG JSON TESTS =====================
    @Test
    void testAG_FromJSONFiles() throws Exception {

        for (int i = 1; i <= 6; i++) {

            String filePath =
                    "test_cases_updated/test_cases/AG/AG_test" + i + ".json";

            FileReader reader = new FileReader(filePath);
            TestFile testFile = gson.fromJson(reader, TestFile.class);
            reader.close();

            // Handle flat AG JSON
            if (testFile.expectedOutput.AG == null &&
                    testFile.expectedOutput.executionOrder != null) {

                testFile.expectedOutput.AG = new SJFExpected();
                testFile.expectedOutput.AG.executionOrder =
                        testFile.expectedOutput.executionOrder;
                testFile.expectedOutput.AG.processResults =
                        testFile.expectedOutput.processResults;
                testFile.expectedOutput.AG.averageWaitingTime =
                        testFile.expectedOutput.averageWaitingTime;
                testFile.expectedOutput.AG.averageTurnaroundTime =
                        testFile.expectedOutput.averageTurnaroundTime;
            }

            int cs = testFile.input.contextSwitch;

            ArrayList<AGProcess> processes =
                    buildAGProcesses(testFile.input.processes);



        }
    }

    // ===================== ASSERT HELPERS =====================
    void assertSchedulerResult(
            String testName,
            SJFResult actual,
            SJFExpected expected
    ) {

        // Execution Order
        assertEquals(
                expected.executionOrder,
                actual.executionOrder,
                testName + " → Execution order mismatch"
        );

        // Process Results
        for (int i = 0; i < expected.processResults.size(); i++) {

            ProcessResult a = actual.processResults.get(i);
            ProcessResult e = expected.processResults.get(i);

            assertEquals(
                    e.waitingTime,
                    a.waitingTime,
                    testName + " → Waiting time error for " + a.name
            );

            assertEquals(
                    e.turnaroundTime,
                    a.turnaroundTime,
                    testName + " → Turnaround time error for " + a.name
            );
        }

        // Averages
        assertEquals(
                expected.averageWaitingTime,
                actual.avgWait,
                0.01,
                testName + " → Average waiting mismatch"
        );

        assertEquals(
                expected.averageTurnaroundTime,
                actual.avgTAT,
                0.01,
                testName + " → Average turnaround mismatch"
        );
    }

    void assertAGResult(
            String testName,
            AGResult actual,
            SJFExpected expected
    ) {

        assertEquals(
                expected.executionOrder,
                actual.executionOrder,
                testName + " → Execution order mismatch"
        );

        Map<String, ProcessResult> expectedMap = new HashMap<>();
        for (ProcessResult p : expected.processResults)
            expectedMap.put(p.name, p);

        for (ProcessResult a : actual.processResults) {
            ProcessResult e = expectedMap.get(a.name);

            assertNotNull(e, testName + " → Missing process " + a.name);

            assertEquals(
                    e.waitingTime,
                    a.waitingTime,
                    testName + " → WT error for " + a.name
            );

            assertEquals(
                    e.turnaroundTime,
                    a.turnaroundTime,
                    testName + " → TAT error for " + a.name
            );

            // Quantum history must end with zero
            assertEquals(
                    0,
                    a.quantumHistory.get(a.quantumHistory.size() - 1),
                    testName + " → Quantum history must end with 0"
            );
        }

        assertEquals(
                expected.averageWaitingTime,
                actual.avgWait,
                0.01
        );

        assertEquals(
                expected.averageTurnaroundTime,
                actual.avgTAT,
                0.01
        );
    }

    // ===================== UTIL =====================
    ArrayList<Process> buildProcesses(List<ProcessInput> inputs) {
        ArrayList<Process> list = new ArrayList<>();
        for (ProcessInput p : inputs)
            list.add(new Process(p.name, p.arrival, p.burst, p.priority));
        return list;
    }

    ArrayList<Process> cloneProcesses(ArrayList<Process> original) {
        ArrayList<Process> copy = new ArrayList<>();
        for (Process p : original)
            copy.add(new Process(p.name, p.arrival, p.burst, p.priority));
        return copy;
    }

    ArrayList<AGProcess> buildAGProcesses(List<ProcessInput> inputs) {
        ArrayList<AGProcess> list = new ArrayList<>();
        for (ProcessInput p : inputs) {
            list.add(new AGProcess(
                    p.name,
                    p.arrival,
                    p.burst,
                    p.priority,
                    p.quantum
            ));
        }
        return list;
    }
}
