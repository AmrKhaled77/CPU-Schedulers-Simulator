import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PreemptiveSJF {

    public static SJFResult simulateSJF(ArrayList<Process> processes, int contextSwitch) {

        int time = 0;
        int completed = 0;
        int n = processes.size();
        String lastProcess = "";

        ArrayList<String> executionOrder = new ArrayList<>();

        // Sort by arrival time initially
        processes.sort(Comparator.comparingInt(p -> p.arrival));

        while (completed < n) {

            Process current = null;

            // Choose shortest remaining time among arrived processes
            for (Process p : processes) {
                if (p.arrival <= time && p.remaining > 0) {
                    if (current == null || p.remaining < current.remaining) {
                        current = p;
                    }
                }
            }

            if (current != null) {

                // Context switch when process changes
                if (!lastProcess.equals("") && !lastProcess.equals(current.name)) {
//                    executionOrder.add("CS");
                    time += contextSwitch;
                }

                // Add new process to execution timeline
                if (!current.name.equals(lastProcess)) {
                    executionOrder.add(current.name);
                }

                // Execute 1 time unit
                current.remaining--;
                time++;
                lastProcess = current.name;

                // Process completed
                if (current.remaining == 0) {
                    completed++;
                    current.turnaround = time - current.arrival;
                    current.waiting = current.turnaround - current.burst;
                }

            } else {
                time++;          // CPU idle
                lastProcess = "";
            }
        }

        // Build process result list
        List<ProcessResult> resultList = new ArrayList<>();
        double totalWait = 0, totalTAT = 0;

        for (Process p : processes) {
            resultList.add(new ProcessResult(p.name, p.waiting, p.turnaround));
            totalWait += p.waiting;
            totalTAT += p.turnaround;
        }

        double avgWait = totalWait / n;
        double avgTAT = totalTAT / n;

        return new SJFResult(executionOrder, resultList, avgWait, avgTAT);
    }
}
