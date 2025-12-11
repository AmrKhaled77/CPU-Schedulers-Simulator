import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;

public class Main {

    @Test
    public void testFullSJF() {
        // Hardcoded processes
        ArrayList<Process> processes = new ArrayList<>();
        processes.add(new Process("P1", 0, 8, 3, 4));
        processes.add(new Process("P2", 1, 4, 1, 3));
        processes.add(new Process("P3", 2, 2, 4, 5));
        processes.add(new Process("P4", 3, 1, 2, 2));
        processes.add(new Process("P5", 4, 3, 5, 4));

        // Run SJF
        SJFResult result = PreemptiveSJF.simulateSJF(processes, 1);

        // --- 1. Check execution order ---
        String[] expectedOrder = {"P1", "P2", "P4", "P3", "P2", "P5", "P1"};
        Assert.assertArrayEquals("Execution order mismatch", expectedOrder, result.executionOrder.toArray());

        // --- 2. Check process results ---
        for (ProcessResult p : result.processResults) {
            switch (p.name) {
                case "P1":
                    Assert.assertEquals(16, p.waitingTime);
                    Assert.assertEquals(24, p.turnaroundTime);
                    break;
                case "P2":
                    Assert.assertEquals(7, p.waitingTime);
                    Assert.assertEquals(11, p.turnaroundTime);
                    break;
                case "P3":
                    Assert.assertEquals(4, p.waitingTime);
                    Assert.assertEquals(6, p.turnaroundTime);
                    break;
                case "P4":
                    Assert.assertEquals(1, p.waitingTime);
                    Assert.assertEquals(2, p.turnaroundTime);
                    break;
                case "P5":
                    Assert.assertEquals(9, p.waitingTime);
                    Assert.assertEquals(12, p.turnaroundTime);
                    break;
                default:
                    Assert.fail("Unexpected process: " + p.name);
            }
        }

        // --- 3. Check average waiting time ---
        Assert.assertEquals(7.4, result.averageWaitingTime, 0.0001);

        // --- 4. Check average turnaround time ---
        Assert.assertEquals(11.0, result.averageTurnaroundTime, 0.0001);
    }
}
