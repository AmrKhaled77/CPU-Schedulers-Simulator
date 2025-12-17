// ======================= IMPORTS =======================
import com.google.gson.Gson;
import java.io.FileReader;
import java.util.*;
import java.nio.file.Paths;

// ======================= MAIN =======================
public class Main {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        Gson gson = new Gson();

        System.out.println("============================================");
        System.out.println("CPU Scheduling Simulator");
        System.out.println("============================================");
        System.out.println("1 - Run Priority Scheduling (Manual)");
        System.out.println("2 - Enter processes manually (SJF & RR)");
        System.out.println("3 - Run AG Scheduler (Manual)");
        System.out.println("4 - Run Unit Tests (Compare Only)");
        System.out.print("Choose option: ");

        int choice = sc.nextInt();

        if (choice == 1) {
            runPriorityScheduling(sc);
        } else if (choice == 2) {
            runManualInput(sc);
        } else if (choice == 3) {
            runAG(sc);
        } else if (choice == 4) {
            runUnitTests(gson);
        } else {
            System.out.println("Invalid choice ❌");
        }

        sc.close();
    }

    // ================= RUN PRIORITY SCHEDULING =================
    static void runPriorityScheduling(Scanner sc) {
        System.out.print("\nEnter number of processes: ");
        int n = sc.nextInt();

        System.out.print("Enter context switch: ");
        int cs = sc.nextInt();

        System.out.print("Enter aging interval (0 for no aging): ");
        int agingInterval = sc.nextInt();

        ArrayList<Process> processes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess " + (i + 1));

            System.out.print("Name: ");
            String name = sc.next();

            System.out.print("Arrival Time: ");
            int arrival = sc.nextInt();

            System.out.print("Burst Time: ");
            int burst = sc.nextInt();

            System.out.print("Priority (lower = higher priority): ");
            int priority = sc.nextInt();

            processes.add(new Process(name, arrival, burst, priority));
        }

        SJFResult priority = PreemptivePriority.simulatePriority(
                cloneProcesses(processes), cs, agingInterval);

        System.out.println("\n========= PRIORITY SCHEDULING OUTPUT =========");
        System.out.println("Execution Order: " + priority.executionOrder);
        for (ProcessResult pr : priority.processResults) {
            System.out.println(pr.name +
                    " | Waiting=" + pr.waitingTime +
                    " | Turnaround=" + pr.turnaroundTime);
        }
        System.out.printf("\nAverage Waiting Time: %.2f%n", priority.avgWait);
        System.out.printf("Average Turnaround Time: %.2f%n", priority.avgTAT);
    }

    // ================= RUN MANUAL RR =================
    static void runManualInput(Scanner sc) {

        System.out.print("\nEnter number of processes: ");
        int n = sc.nextInt();

        System.out.print("Enter context switch: ");
        int cs = sc.nextInt();

        System.out.print("Enter RR quantum: ");
        int quantum = sc.nextInt();

        ArrayList<Process> processes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess " + (i + 1));

            System.out.print("Name: ");
            String name = sc.next();

            System.out.print("Arrival Time: ");
            int arrival = sc.nextInt();

            System.out.print("Burst Time: ");
            int burst = sc.nextInt();

            System.out.print("Priority: ");
            int priority = sc.nextInt();

            processes.add(new Process(name, arrival, burst, priority));
        }

        // ======= RUN PREEMPTIVE SJF =======
        SJFResult sjf = PreemptiveSJF.simulateSJF(cloneProcesses(processes), cs);

        System.out.println("\n========= SJF OUTPUT =========");
        System.out.println("Execution Order: " + sjf.executionOrder);
        for (ProcessResult pr : sjf.processResults) {
            System.out.println(pr.name +
                    " | Waiting=" + pr.waitingTime +
                    " | Turnaround=" + pr.turnaroundTime);
        }
        System.out.printf("\nAverage Waiting Time: %.2f%n", sjf.avgWait);
        System.out.printf("Average Turnaround Time: %.2f%n", sjf.avgTAT);

        // ======= RUN ROUND ROBIN =======
        SJFResult rr = RoundRobin.simulateRR(cloneProcesses(processes), quantum, cs);

        System.out.println("\n========= ROUND ROBIN OUTPUT =========");
        System.out.println("Execution Order: " + rr.executionOrder);
        for (ProcessResult pr : rr.processResults) {
            System.out.println(pr.name +
                    " | Waiting=" + pr.waitingTime +
                    " | Turnaround=" + pr.turnaroundTime);
        }
        System.out.printf("\nAverage Waiting Time: %.2f%n", rr.avgWait);
        System.out.printf("Average Turnaround Time: %.2f%n", rr.avgTAT);
    }


    // ================= RUN AG (Manual) =================
    static void runAG(Scanner sc) {
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        System.out.print("Enter context switch: ");
        int cs = sc.nextInt();

        ArrayList<AGProcess> processes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess " + (i + 1));
            System.out.print("Name: ");
            String name = sc.next();
            System.out.print("Arrival Time: ");
            int arrival = sc.nextInt();
            System.out.print("Burst Time: ");
            int burst = sc.nextInt();
            System.out.print("Priority: ");
            int priority = sc.nextInt();
            System.out.print("Initial Quantum: ");
            int quantum = sc.nextInt();

            processes.add(new AGProcess(name, arrival, burst, priority, quantum));
        }

        AGScheduler scheduler = new AGScheduler(processes);
        AGResult result = scheduler.simulate();

        System.out.println("\n========= AG SCHEDULER OUTPUT =========");
        System.out.println("Execution Order: " + result.executionOrder);
        for (ProcessResult pr : result.processResults) {
            System.out.println(pr.name +
                    " | Waiting=" + pr.waitingTime +
                    " | Turnaround=" + pr.turnaroundTime +
                    " | Quantum History=" + pr.quantumHistory);
        }
        System.out.printf("Average Waiting Time: %.2f%n", result.avgWait);
        System.out.printf("Average Turnaround Time: %.2f%n", result.avgTAT);
    }

    // ================= UNIT TEST MODE (WITH PASS/FAIL) =================
    static void runUnitTests(Gson gson) throws Exception {

        System.out.println("\n========== UNIT TEST MODE ==========");
        System.out.println("Reading expected & actual from JSON files\n");

        /* ============ SJF & RR UNIT TESTS ============ */
        for (int i = 1; i <= 6; i++) {

            String filePath =
                    "test_cases_v3/Other_Schedulers/test_" + i + ".json";

            System.out.println("\n--- FILE: test_" + i + ".json ---");

            FileReader reader = new FileReader(filePath);
            TestFile testFile = gson.fromJson(reader, TestFile.class);
            reader.close();

            int cs = testFile.input.contextSwitch;
            int quantum = testFile.input.rrQuantum;
            int agingInterval = testFile.input.agingInterval != null ?
                    testFile.input.agingInterval : 0;

            ArrayList<Process> base =
                    buildProcesses(testFile.input.processes);

            // ===== SJF =====
            SJFResult sjf =
                    PreemptiveSJF.simulateSJF(cloneProcesses(base), cs);

            printCompareWithPassFail("SJF", sjf, testFile.expectedOutput.SJF);

            // ===== RR =====
            SJFResult rr =
                    RoundRobin.simulateRR(cloneProcesses(base), quantum, cs);

            printCompareWithPassFail("RR", rr, testFile.expectedOutput.RR);

            // ===== PRIORITY =====
            if (testFile.expectedOutput.Priority != null) {
                SJFResult priority =
                        PreemptivePriority.simulatePriority(
                                cloneProcesses(base), cs, agingInterval);

                printCompareWithPassFail("PRIORITY", priority,
                        testFile.expectedOutput.Priority);
            }
        }

        /* ============ AG UNIT TESTS ============ */
        for (int i = 1; i <= 6; i++) {

            String filePath =
                    "test_cases_v3/AG/AG_test" + i + ".json";

            System.out.println("\n--- FILE: AG_test" + i + ".json ---");

            FileReader reader = new FileReader(filePath);
            TestFile testFile = gson.fromJson(reader, TestFile.class);
            reader.close();

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

            ArrayList<AGProcess> ag =
                    buildAGProcesses(testFile.input.processes);

            AGScheduler scheduler = new AGScheduler(ag);
            AGResult result = scheduler.simulate();
            printCompareWithPassFail("AG", result, testFile.expectedOutput.AG);
        }
    }

    // ============ PRINT COMPARISON WITH PASS/FAIL ============
    static void printCompareWithPassFail(String title,
                                         SJFResult actual,
                                         SJFExpected expected) {

        System.out.println("\n[" + title + "] Execution Order");
        System.out.println("Actual   : " + actual.executionOrder);
        System.out.println("Expected : " + expected.executionOrder);

        boolean pass = true;

        System.out.println("\nProcess Results:");
        System.out.printf("%-6s %-10s %-10s %-10s %-10s %-10s%n",
                "PID", "WT(A)", "WT(E)", "TAT(A)", "TAT(E)", "PASS");

        for (int i = 0; i < expected.processResults.size(); i++) {
            ProcessResult a = actual.processResults.get(i);
            ProcessResult e = expected.processResults.get(i);

            boolean p = (a.waitingTime == e.waitingTime) &&
                    (a.turnaroundTime == e.turnaroundTime);
            if (!p) pass = false;

            System.out.printf("%-6s %-10d %-10d %-10d %-10d %-10s%n",
                    a.name,
                    a.waitingTime, e.waitingTime,
                    a.turnaroundTime, e.turnaroundTime,
                    p ? "✔" : "✖");
        }

        boolean avgPass = Math.abs(actual.avgWait - expected.averageWaitingTime) < 0.01 &&
                Math.abs(actual.avgTAT - expected.averageTurnaroundTime) < 0.01;
        if (!avgPass) pass = false;

        System.out.printf("\nAvg WT  -> Actual: %.2f | Expected: %.2f%n",
                actual.avgWait, expected.averageWaitingTime);
        System.out.printf("Avg TAT -> Actual: %.2f | Expected: %.2f%n",
                actual.avgTAT, expected.averageTurnaroundTime);

        System.out.println("\nSUMMARY: " + (pass ? "PASS ✅" : "FAIL ❌"));
    }


    // ================= UTIL =================
    static ArrayList<Process> buildProcesses(List<ProcessInput> inputs) {
        ArrayList<Process> list = new ArrayList<>();
        for (ProcessInput p : inputs)
            list.add(new Process(p.name, p.arrival, p.burst, p.priority));
        return list;
    }

    static ArrayList<Process> cloneProcesses(ArrayList<Process> original) {
        ArrayList<Process> copy = new ArrayList<>();
        for (Process p : original)
            copy.add(new Process(p.name, p.arrival, p.burst, p.priority));
        return copy;
    }

    static ArrayList<AGProcess> buildAGProcesses(List<ProcessInput> inputs) {
        ArrayList<AGProcess> list = new ArrayList<>();
        for (ProcessInput p : inputs) {
            list.add(new AGProcess(p.name, p.arrival, p.burst, p.priority, p.quantum));
        }
        return list;
    }
}

// ======================= PREEMPTIVE SJF =======================
class PreemptiveSJF {
    static SJFResult simulateSJF(ArrayList<Process> processes, int cs) {
        int time = 0, completed = 0;
        String last = "";
        List<String> order = new ArrayList<>();
        processes.sort(Comparator.comparingInt(p -> p.arrival));

        while (completed < processes.size()) {
            Process cur = null;
            for (Process p : processes)
                if (p.arrival <= time && p.remaining > 0)
                    if (cur == null || p.remaining < cur.remaining)
                        cur = p;

            if (cur == null) { time++; last = ""; continue; }
            if (!last.equals("") && !last.equals(cur.name)) time += cs;
            if (!cur.name.equals(last)) order.add(cur.name);

            cur.remaining--; time++; last = cur.name;
            if (cur.remaining == 0) {
                completed++;
                cur.turnaround = time - cur.arrival;
                cur.waiting = cur.turnaround - cur.burst;
            }
        }
        return ResultBuilder.build(processes, order);
    }
}

// ======================= ROUND ROBIN =======================
class RoundRobin {
    static SJFResult simulateRR(List<Process> processes, int quantum, int cs) {
        Queue<Process> ready = new LinkedList<>();
        List<String> order = new ArrayList<>();
        int time = 0;
        int completed = 0;
        int n = processes.size();
        int idx = 0;
        String last = "";
        processes.sort(Comparator.comparingInt(p -> p.arrival));

        while (completed < n) {
            while (idx < n && processes.get(idx).arrival <= time) {
                ready.add(processes.get(idx));
                idx++;
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            Process cur = ready.poll();

            if (!last.isEmpty() && !cur.name.equals(last)) {
                time += cs;
                while (idx < n && processes.get(idx).arrival <= time) {
                    ready.add(processes.get(idx));
                    idx++;
                }
            }

            order.add(cur.name);
            last = cur.name;

            int exec = Math.min(quantum, cur.remaining);
            cur.remaining -= exec;
            time += exec;

            while (idx < n && processes.get(idx).arrival <= time) {
                ready.add(processes.get(idx));
                idx++;
            }

            if (cur.remaining > 0) {
                ready.add(cur);
            } else {
                cur.completedTime = time;
                completed++;
            }
        }

        for (Process p : processes) {
            p.turnaround = p.completedTime - p.arrival;
            p.waiting = p.turnaround - p.burst;
        }
        return ResultBuilder.build(processes, order);
    }
}

// ======================= PREEMPTIVE PRIORITY WITH AGING =======================
class PreemptivePriority {

    static class PriorityProcess extends Process {
        int originalPriority;
        int effectivePriority;
        int timeInReadyQueue;
        int lastReadyQueueEntry;

        PriorityProcess(String n, int a, int b, int p) {
            super(n, a, b, p);
            this.originalPriority = p;
            this.effectivePriority = p;
            this.timeInReadyQueue = 0;
            this.lastReadyQueueEntry = -1;
        }

        void updatePriority(int agingInterval) {
            if (agingInterval > 0) {
                int reduction = (int) Math.floor((double) timeInReadyQueue / agingInterval);
                // Lower number = higher priority, so we subtract
                effectivePriority = originalPriority - reduction;
                // Ensure priority doesn't go below 0
                if (effectivePriority < 0) effectivePriority = 0;
            }
        }
    }

    static SJFResult simulatePriority(ArrayList<Process> processes, int cs, int agingInterval) {
        int time = 0, completed = 0;
        String last = "";
        List<String> order = new ArrayList<>();

        // Convert to PriorityProcess
        ArrayList<PriorityProcess> procs = new ArrayList<>();
        for (Process p : processes) {
            procs.add(new PriorityProcess(p.name, p.arrival, p.burst, p.priority));
        }

        procs.sort(Comparator.comparingInt(p -> p.arrival));
        PriorityProcess currentProcess = null;

        while (completed < procs.size()) {
            // Update time in ready queue and priorities for all waiting processes
            for (PriorityProcess p : procs) {
                if (p.arrival <= time && p.remaining > 0 && p != currentProcess) {
                    if (p.lastReadyQueueEntry == -1) {
                        p.lastReadyQueueEntry = time;
                    }
                    p.timeInReadyQueue = time - p.lastReadyQueueEntry;
                    p.updatePriority(agingInterval);
                }
            }

            // Find process with highest priority (lowest priority number)
            PriorityProcess selected = null;
            for (PriorityProcess p : procs) {
                if (p.arrival <= time && p.remaining > 0) {
                    if (selected == null ||
                            p.effectivePriority < selected.effectivePriority ||
                            (p.effectivePriority == selected.effectivePriority &&
                                    p.arrival < selected.arrival)) {
                        selected = p;
                    }
                }
            }

            if (selected == null) {
                time++;
                last = "";
                currentProcess = null;
                continue;
            }

            // Context switch if changing processes
            if (!last.equals("") && !last.equals(selected.name)) {
                time += cs;
            }

            // Add to execution order if starting a new process
            if (!selected.name.equals(last)) {
                order.add(selected.name);
            }

            // Execute for 1 time unit
            selected.remaining--;
            time++;
            last = selected.name;
            currentProcess = selected;

            // Reset ready queue tracking for currently executing process
            selected.timeInReadyQueue = 0;
            selected.lastReadyQueueEntry = -1;

            if (selected.remaining == 0) {
                completed++;
                selected.turnaround = time - selected.arrival;
                selected.waiting = selected.turnaround - selected.burst;
            } else {
                // Mark when process will re-enter ready queue
                selected.lastReadyQueueEntry = time;
            }
        }

        // Convert back to regular Process list for result
        ArrayList<Process> resultProcesses = new ArrayList<>();
        for (PriorityProcess pp : procs) {
            Process p = new Process(pp.name, pp.arrival, pp.burst, pp.priority);
            p.waiting = pp.waiting;
            p.turnaround = pp.turnaround;
            p.remaining = pp.remaining;
            p.completedTime = pp.completedTime;
            resultProcesses.add(p);
        }

        return ResultBuilder.build(resultProcesses, order);
    }
}

// ======================= AG SCHEDULER =======================
class AGScheduler {
    private List<AGProcess> processes;
    private Deque<AGProcess> readyQueue;
    private List<String> executionOrder;
    private int currentTime;

    public AGScheduler(List<AGProcess> processes) {
        this.processes = processes;
        this.readyQueue = new LinkedList<>();
        this.executionOrder = new ArrayList<>();
        this.currentTime = 0;
    }

    public AGResult simulate() {
        // Setup Pending Processes
        List<AGProcess> pending = new ArrayList<>(processes);
        pending.sort(Comparator.comparingInt(AGProcess::getArrivalTime));

        List<AGProcess> completed = new ArrayList<>();
        AGProcess current = null;
        int timeUsedInQuantum = 0;

        // --- IMPORTANT: Handle T=0 Arrivals Before Loop ---
        handleArrivals(pending, currentTime);

        // --- SIMULATION LOOP ---
        while (completed.size() < processes.size()) {

            // 1. CPU Idle Handling
            if (current == null) {
                if (!readyQueue.isEmpty()) {
                    current = readyQueue.poll();
                    timeUsedInQuantum = 0;
                    executionOrder.add(current.getName());
                } else {
                    // CPU is idle, just advance time
                    currentTime++;
                    handleArrivals(pending, currentTime);
                    continue;
                }
            }

            // 2. Calculate Quantum Boundaries (User Logic)
            int q = current.getQuantum();
            int q25 = (int) Math.ceil(q * 0.25);
            int q50 = q25 + (int) Math.ceil(q * 0.25);

            AGProcess nextProcess = null;
            String reason = "";

            // 3. Preemption Check (AG Logic)
            //    Note: arrivals for 'currentTime' are NOT in queue yet if they just arrived
            //    This prevents instantaneous preemption by a just-arrived process
// 🔁 Priority phase (NON-preemptive, switch only at 25%)
            if (timeUsedInQuantum == q25) {

                AGProcess bestPrio = getBestPriorityProcess();

                if (bestPrio != null &&
                        bestPrio.getPriority() < current.getPriority()) {

                    // Update quantum of current process
                    int unused = current.getQuantum() - timeUsedInQuantum;
                    int newQuantum = current.getQuantum() + (int) Math.ceil(unused / 2.0);

                    current.setQuantum(newQuantum);
                    current.addQuantumToHistory(newQuantum);

                    // Move current back to ready queue
                    readyQueue.add(current);

                    // Switch to best priority process
                    readyQueue.remove(bestPrio);
                    current = bestPrio;
                    timeUsedInQuantum = 0;
                    executionOrder.add(current.getName());

                    continue; // restart loop with new process
                }
            }
            else if (timeUsedInQuantum >= q50) {
                AGProcess bestSJF = getShortestJobProcess();
                if (bestSJF != null && bestSJF.getRemainingTime() < current.getRemainingTime()) {
                    nextProcess = bestSJF;
                    reason = "SJF";
                }
            }

            // 4. Perform Preemption (Swap)
            if (nextProcess != null) {
                readyQueue.remove(nextProcess);

                int unused = q - timeUsedInQuantum;
                int newQuantum = 0;

                if (reason.equals("Priority")) {
                    newQuantum = q + (int) Math.ceil(unused / 2.0);
                } else { // SJF
                    newQuantum = q + unused;
                }

                current.setQuantum(newQuantum);
                current.addQuantumToHistory(newQuantum);
                readyQueue.add(current);

                current = nextProcess;
                timeUsedInQuantum = 0;
                executionOrder.add(current.getName());
                // Do not increment time; restart loop to let new process run
                continue;
            }

            // 5. Execute One Time Unit
            current.setRemainingTime(current.getRemainingTime() - 1);
            timeUsedInQuantum++;
            currentTime++;

            // 6. Handle Arrivals (Post-Execution)
            //    Now we add processes that arrived during this tick
            handleArrivals(pending, currentTime);

            // 7. Process Completion Check
            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(currentTime);
                current.setQuantum(0);
                current.addQuantumToHistory(0);
                completed.add(current);
                current = null;
            }
            // 8. Quantum Expiry Check
            else if (timeUsedInQuantum == current.getQuantum()) {
                current.setQuantum(current.getQuantum() + 2);
                current.addQuantumToHistory(current.getQuantum());
                readyQueue.add(current);
                current = null;
            }
        }

        // Final Calculations
        for (AGProcess p : processes) {
            p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
        }

        return AGResultBuilder.build(processes, executionOrder);
    }

    // --- Helpers ---

    private void handleArrivals(List<AGProcess> pending, int time) {
        Iterator<AGProcess> it = pending.iterator();
        while (it.hasNext()) {
            AGProcess p = it.next();
            if (p.getArrivalTime() <= time) {
                readyQueue.add(p);
                it.remove();
            } else {
                break;
            }
        }
    }

    private AGProcess getBestPriorityProcess() {
        AGProcess best = null;
        for (AGProcess p : readyQueue) {
            if (best == null || p.getPriority() < best.getPriority()) {
                best = p;
            }
        }
        return best;
    }

    private AGProcess getShortestJobProcess() {
        AGProcess best = null;
        for (AGProcess p : readyQueue) {
            if (best == null || p.getRemainingTime() < best.getRemainingTime()) {
                best = p;
            }
        }
        return best;
    }
}

// ======================= RESULT BUILDER =======================
class ResultBuilder {
    static SJFResult build(List<Process> processes, List<String> order) {
        double tw = 0, tt = 0;
        List<ProcessResult> res = new ArrayList<>();
        for (Process p : processes) {
            res.add(new ProcessResult(p.name, p.waiting, p.turnaround));
            tw += p.waiting; tt += p.turnaround;
        }
        return new SJFResult(order, res, tw / processes.size(), tt / processes.size());
    }
}

// ======================= MODELS =======================
class Process {
    String name;
    int arrival, burst, priority;
    int remaining, waiting, turnaround, completedTime;
    Process(String n, int a, int b, int p) {
        name = n; arrival = a; burst = b; priority = p; remaining = b;
    }
}

class AGProcess extends Process {
    private int quantum;
    private List<Integer> quantumHistory;

    public AGProcess(String name, int arrivalTime, int burstTime, int priority, int quantum) {
        super(name, arrivalTime, burstTime, priority); // Initialize parent fields
        this.quantum = quantum;
        this.quantumHistory = new ArrayList<>();
        this.quantumHistory.add(quantum);
    }

    // Getters mapping to parent fields
    public String getName() { return name; }
    public int getArrivalTime() { return arrival; }
    public int getBurstTime() { return burst; }
    public int getRemainingTime() { return remaining; }
    public int getPriority() { return priority; }
    public int getQuantum() { return quantum; }
    public int getWaitingTime() { return waiting; }
    public int getTurnaroundTime() { return turnaround; }
    public int getCompletionTime() { return completedTime; }
    public List<Integer> getQuantumHistory() { return quantumHistory; }

    // Setters mapping to parent fields
    public void setRemainingTime(int remainingTime) { this.remaining = remainingTime; }
    public void setQuantum(int quantum) { this.quantum = quantum; }
    public void setWaitingTime(int waitingTime) { this.waiting = waitingTime; }
    public void setTurnaroundTime(int turnaroundTime) { this.turnaround = turnaroundTime; }
    public void setCompletionTime(int completionTime) { this.completedTime = completionTime; }
    public void setQuantumHistory(List<Integer> quantumHistory) { this.quantumHistory = quantumHistory; }

    public void addQuantumToHistory(int quantum) { quantumHistory.add(quantum); }
    public boolean isComplete() { return remaining == 0; }
}

class ProcessResult {
    String name;
    int waitingTime, turnaroundTime;
    List<Integer> quantumHistory;

    ProcessResult(String n, int w, int t) {
        this(n, w, t, new ArrayList<>());
    }

    ProcessResult(String n, int w, int t, List<Integer> qh) {
        name = n; waitingTime = w; turnaroundTime = t; quantumHistory = qh;
    }
}

class SJFResult {
    List<String> executionOrder; List<ProcessResult> processResults;
    double avgWait, avgTAT;
    SJFResult(List<String> o, List<ProcessResult> p, double w, double t) {
        executionOrder = o; processResults = p; avgWait = w; avgTAT = t;
    }
}

class AGResult extends SJFResult {
    AGResult(List<String> o, List<ProcessResult> p, double w, double t) { super(o,p,w,t); }
}

class AGResultBuilder {
    static AGResult build(List<AGProcess> processes, List<String> order) {
        double tw = 0, tt = 0; List<ProcessResult> res = new ArrayList<>();
        for (AGProcess p : processes) {
            res.add(new ProcessResult(p.getName(), p.getWaitingTime(), p.getTurnaroundTime(), p.getQuantumHistory()));
            tw += p.getWaitingTime(); tt += p.getTurnaroundTime();
        }
        return new AGResult(order, res, tw / processes.size(), tt / processes.size());
    }
}
// ======================= JSON MODELS =======================
class TestFile {
    Input input;
    ExpectedOutput expectedOutput;
}
class Input {
    int contextSwitch, rrQuantum;
    Integer agingInterval;
    List<ProcessInput> processes;
}
class ProcessInput {
    String name;
    int arrival, burst, priority, quantum;
}
class ExpectedOutput {
    SJFExpected SJF;
    SJFExpected RR;
    SJFExpected Priority;
    SJFExpected AG;

    List<String> executionOrder;
    List<ProcessResult> processResults;
    double averageWaitingTime, averageTurnaroundTime;
}

class SJFExpected {
    List<String> executionOrder;
    List<ProcessResult> processResults;
    double averageWaitingTime, averageTurnaroundTime;
}