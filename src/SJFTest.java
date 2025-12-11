//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//public class SJFTest {
//
//    @Test
//    public void testSJFUsingTestCase1() throws Exception {
//
//        // Load test JSON
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode root = mapper.readTree(new File("test_1.json"));
//
//        // ------------------- INPUT -------------------
//        JsonNode processList = root.get("input").get("processes");
//        ArrayList<Process> processes = new ArrayList<>();
//
//        for (JsonNode p : processList) {
//            processes.add(new Process(
//                    p.get("name").asText(),
//                    p.get("arrival").asInt(),
//                    p.get("burst").asInt(),
//                    p.get("priority").asInt(),
//                    p.get("quantum").asInt()
//            ));
//        }
//
//        // ------------------- EXPECTED -------------------
//        JsonNode expectedSJF = root.get("expectedOutput").get("SJF");
//
//        List<String> expectedExec = new ArrayList<>();
//        for (JsonNode e : expectedSJF.get("executionOrder"))
//            expectedExec.add(e.asText());
//
//        List<ProcessResult> expectedProcResults = new ArrayList<>();
//        for (JsonNode p : expectedSJF.get("processResults")) {
//            expectedProcResults.add(new ProcessResult(
//                    p.get("name").asText(),
//                    p.get("waitingTime").asInt(),
//                    p.get("turnaroundTime").asInt()
//            ));
//        }
//
//        double expectedAvgWait = expectedSJF.get("averageWaitingTime").asDouble();
//        double expectedAvgTAT = expectedSJF.get("averageTurnaroundTime").asDouble();
//
//        // ------------------- RUN SJF -------------------
//        SJFResult actual = PreemptiveSJF.simulateSJF(processes, 0);
//
//        // ------------------- ASSERTIONS -------------------
//
//        // 1. Execution Order
//        Assertions.assertEquals(expectedExec, actual.executionOrder,
//                "Execution order mismatch!");
//
//        // 2. Per-process waiting + turnaround
//        for (ProcessResult expected : expectedProcResults) {
//            ProcessResult actualP = actual.processResults.stream()
//                    .filter(x -> x.name.equals(expected.name))
//                    .findFirst()
//                    .orElse(null);
//
//            Assertions.assertNotNull(actualP, "Missing process: " + expected.name);
//
//            Assertions.assertEquals(expected.waitingTime, actualP.waitingTime,
//                    "Waiting time mismatch for " + expected.name);
//
//            Assertions.assertEquals(expected.turnaroundTime, actualP.turnaroundTime,
//                    "Turnaround time mismatch for " + expected.name);
//        }
//
//        // 3. Averages
//        Assertions.assertEquals(expectedAvgWait, actual.averageWaitingTime, 0.0001,
//                "Avg waiting mismatch!");
//
//        Assertions.assertEquals(expectedAvgTAT, actual.averageTurnaroundTime, 0.0001,
//                "Avg turnaround mismatch!");
//    }
//}
