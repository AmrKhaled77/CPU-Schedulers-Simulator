import com.google.gson.Gson;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class JsonBasedSchedulerTest {

    private final Gson gson = new Gson();

    // ====================== SJF TESTS ======================
    @TestFactory
    Stream<DynamicTest> sjfTests() {
        return IntStream.rangeClosed(1, 6).mapToObj(i ->
                DynamicTest.dynamicTest("SJF test_" + i, () -> {

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
                })
        );
    }

    // ====================== PRIORITY TESTS ======================
    @TestFactory
    Stream<DynamicTest> priorityTests() {
        return IntStream.rangeClosed(1, 6).mapToObj(i ->
                DynamicTest.dynamicTest("PRIORITY test_" + i, () -> {

                    TestFile tf = load(
                            "test_cases_v3/Other_Schedulers/test_" + i + ".json"
                    );

                    if (tf.expectedOutput.Priority == null)
                        return; // skip tests without priority output

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
                })
        );
    }

    // ====================== ROUND ROBIN TESTS ======================
    @TestFactory
    Stream<DynamicTest> rrTests() {
        return IntStream.rangeClosed(1, 6).mapToObj(i ->
                DynamicTest.dynamicTest("RR test_" + i, () -> {

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
                })
        );
    }

    // ====================== AG TESTS ======================
    @TestFactory
    Stream<DynamicTest> agTests() {
        return IntStream.rangeClosed(1, 6).mapToObj(i ->
                DynamicTest.dynamicTest("AG test_" + i, () -> {

                    TestFile tf = load(
                            "test_cases_v3/AG/AG_test" + i + ".json"
                    );

                    ArrayList<AGProcess> processes =
                            Main.buildAGProcesses(tf.input.processes);

                    AGScheduler scheduler = new AGScheduler(processes);
                    AGResult actual = scheduler.simulate();

                    SJFExpected expected = tf.expectedOutput.AG;


                    if (expected == null) {
                        expected = new SJFExpected();
                        expected.executionOrder = tf.expectedOutput.executionOrder;
                        expected.processResults = tf.expectedOutput.processResults;
                        expected.averageWaitingTime =
                                tf.expectedOutput.averageWaitingTime;
                        expected.averageTurnaroundTime =
                                tf.expectedOutput.averageTurnaroundTime;
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
                })
        );
    }

    // ====================== UTIL ======================
    private TestFile load(String path) throws Exception {
        FileReader reader = new FileReader(path);
        TestFile tf = gson.fromJson(reader, TestFile.class);
        reader.close();
        return tf;
    }
}
