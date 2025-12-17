import com.google.gson.Gson;
import org.junit.Test;

import java.io.FileReader;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class JsonBasedSchedulerTest {

    private final Gson gson = new Gson();

    // ====================== SJF TESTS (ALL 6 FILES) ======================
    @Test
    public void testSJF_AllJsonFiles() throws Exception {

        for (int i = 1; i <= 6; i++) {

            TestFile tf = load(
                    "test_cases_v3/Other_Schedulers/test_" + i + ".json"
            );

            ArrayList<Process> processes =
                    Main.buildProcesses(tf.input.processes);

            SJFResult actual =
                    PreemptiveSJF.simulateSJF(
                            processes,
                            tf.input.contextSwitch
                    );

            SJFExpected expected = tf.expectedOutput.SJF;

            assertEquals(expected.executionOrder, actual.executionOrder);

            for (int j = 0; j < expected.processResults.size(); j++) {
                assertEquals(
                        expected.processResults.get(j).waitingTime,
                        actual.processResults.get(j).waitingTime
                );
                assertEquals(
                        expected.processResults.get(j).turnaroundTime,
                        actual.processResults.get(j).turnaroundTime
                );
            }

            assertEquals(expected.averageWaitingTime, actual.avgWait, 0.01);
            assertEquals(expected.averageTurnaroundTime, actual.avgTAT, 0.01);
        }
    }
    // ====================== PRIORITY TESTS ======================


    // ====================== RR TESTS (ALL 6 FILES) ======================
    @Test
    public void testRR_AllJsonFiles() throws Exception {

        for (int i = 1; i <= 6; i++) {

            TestFile tf = load(
                    "test_cases_v3/Other_Schedulers/test_" + i + ".json"
            );

            ArrayList<Process> processes =
                    Main.buildProcesses(tf.input.processes);

            SJFResult actual =
                    RoundRobin.simulateRR(
                            processes,
                            tf.input.rrQuantum,
                            tf.input.contextSwitch
                    );

            SJFExpected expected = tf.expectedOutput.RR;

            assertEquals(expected.executionOrder, actual.executionOrder);

            for (int j = 0; j < expected.processResults.size(); j++) {
                assertEquals(
                        expected.processResults.get(j).waitingTime,
                        actual.processResults.get(j).waitingTime
                );
                assertEquals(
                        expected.processResults.get(j).turnaroundTime,
                        actual.processResults.get(j).turnaroundTime
                );
            }

            assertEquals(expected.averageWaitingTime, actual.avgWait, 0.01);
            assertEquals(expected.averageTurnaroundTime, actual.avgTAT, 0.01);
        }
    }



    @Test
    public void testAG_AllJsonFiles() throws Exception {

        for (int i = 1; i <= 6; i++) {

            TestFile tf = load("test_cases_v3/AG/AG_test" + i + ".json");

            ArrayList<AGProcess> processes = Main.buildAGProcesses(tf.input.processes);


            AGScheduler scheduler = new AGScheduler(processes);


            AGResult actual = scheduler.simulate();

            SJFExpected expected = tf.expectedOutput.AG;

            // Handle old AG JSON format
            if (expected == null) {
                expected = new SJFExpected();
                expected.executionOrder = tf.expectedOutput.executionOrder;
                expected.processResults = tf.expectedOutput.processResults;
                expected.averageWaitingTime = tf.expectedOutput.averageWaitingTime;
                expected.averageTurnaroundTime = tf.expectedOutput.averageTurnaroundTime;
            }

            assertEquals(expected.executionOrder, actual.executionOrder);

            for (int j = 0; j < expected.processResults.size(); j++) {
                assertEquals(
                        expected.processResults.get(j).waitingTime,
                        actual.processResults.get(j).waitingTime
                );
                assertEquals(
                        expected.processResults.get(j).turnaroundTime,
                        actual.processResults.get(j).turnaroundTime
                );
            }

            assertEquals(expected.averageWaitingTime, actual.avgWait, 0.01);
            assertEquals(expected.averageTurnaroundTime, actual.avgTAT, 0.01);
        }
    }



    @Test
    public void testPriority_AllJsonFiles() throws Exception {

        for (int i = 1; i <= 6; i++) {

            TestFile tf = load(
                    "test_cases_v3/Other_Schedulers/test_" + i + ".json"
            );

            if (tf.expectedOutput.Priority == null) continue;

            ArrayList<Process> processes =
                    Main.buildProcesses(tf.input.processes);

            int aging = tf.input.agingInterval != null
                    ? tf.input.agingInterval
                    : 0;

            SJFResult actual =
                    PreemptivePriority.simulatePriority(
                            processes,
                            tf.input.contextSwitch,
                            aging
                    );

            SJFExpected expected = tf.expectedOutput.Priority;

            assertEquals(expected.executionOrder, actual.executionOrder);

            for (int j = 0; j < expected.processResults.size(); j++) {
                assertEquals(
                        expected.processResults.get(j).waitingTime,
                        actual.processResults.get(j).waitingTime
                );
                assertEquals(
                        expected.processResults.get(j).turnaroundTime,
                        actual.processResults.get(j).turnaroundTime
                );
            }

            assertEquals(expected.averageWaitingTime, actual.avgWait, 0.01);
            assertEquals(expected.averageTurnaroundTime, actual.avgTAT, 0.01);
        }
    }



    // ====================== UTIL ======================
    private TestFile load(String path) throws Exception {
        FileReader reader = new FileReader(path);
        TestFile tf = gson.fromJson(reader, TestFile.class);
        reader.close();
        return tf;
    }
}