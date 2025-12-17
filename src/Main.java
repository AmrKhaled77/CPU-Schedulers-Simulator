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
        System.out.println("2 - Enter processes manually ");
        System.out.println("3 - Run AG Scheduler (Manual)");
        System.out.println("4 - Run Unit Tests (Compare Only)");
        System.out.print("Choose option: ");

        int choice = sc.nextInt();

        if (choice == 1) {

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

    // ================= RUN TEST FILES =================


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

        AGResult result = AGScheduler.simulate(processes, cs);

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
                    "test_cases_updated/test_cases/Other_Schedulers/test_" + i + ".json";

            System.out.println("\n--- FILE: test_" + i + ".json ---");

            FileReader reader = new FileReader(filePath);
            TestFile testFile = gson.fromJson(reader, TestFile.class);
            reader.close();

            int cs = testFile.input.contextSwitch;
            int quantum = testFile.input.rrQuantum;

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
        }

        /* ============ AG UNIT TESTS ============ */
        for (int i = 1; i <= 6; i++) {

            String filePath =
                    "test_cases_updated/test_cases/AG/AG_test" + i + ".json";

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

            AGResult result =
                    AGScheduler.simulate(ag, cs);

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
        int time = 0, completed = 0, n = processes.size();
        boolean[] added = new boolean[n];
        processes.sort(Comparator.comparingInt(p -> p.arrival));

        while (completed < n) {
            for (int i = 0; i < n; i++)
                if (!added[i] && processes.get(i).arrival <= time) {
                    ready.add(processes.get(i)); added[i] = true;
                }

            if (ready.isEmpty()) { time++; continue; }

            Process cur = ready.poll(); order.add(cur.name);
            int exec = Math.min(quantum, cur.remaining);
            cur.remaining -= exec; time += exec;

            for (int i = 0; i < n; i++)
                if (!added[i] && processes.get(i).arrival <= time) {
                    ready.add(processes.get(i)); added[i] = true;
                }

            if (cur.remaining > 0) ready.add(cur);
            else { cur.completedTime = time; completed++; }

            if (!ready.isEmpty()) time += cs;
        }

        for (Process p : processes) {
            p.turnaround = p.completedTime - p.arrival;
            p.waiting = p.turnaround - p.burst;
        }
        return ResultBuilder.build(processes, order);
    }
}

// ======================= AG SCHEDULER (FIXED) =======================
class AGScheduler {

    static AGResult simulate(List<AGProcess> processes, int contextSwitch) {
        Deque<AGProcess> ready = new LinkedList<>();
        List<String> executionOrder = new ArrayList<>();

        int time = 0;
        int completedCount = 0;
        int n = processes.size();

        processes.sort(Comparator.comparingInt(p -> p.arrival));
        int processIndex = 0;

        AGProcess cur = null;

        while (completedCount < n) {
            while (processIndex < n && processes.get(processIndex).arrival <= time) {
                ready.add(processes.get(processIndex));
                processes.get(processIndex).added = true;
                processIndex++;
            }

            if (cur == null) {
                if (ready.isEmpty()) {
                    time++;
                    continue;
                }
                cur = ready.poll();
            }

            executionOrder.add(cur.name);

            int q = cur.quantum;
            int limitF1 = (int) Math.ceil(q * 0.25);
            int limitF2 = (int) Math.ceil(q * 0.50);

            int timeSlice = 0;
            boolean finished = false;
            boolean preempted = false;

            // --- PHASE 1: FCFS ---
            while (timeSlice < limitF1) {
                cur.remaining--;
                timeSlice++;
                time++;

                while (processIndex < n && processes.get(processIndex).arrival <= time) {
                    ready.add(processes.get(processIndex));
                    processes.get(processIndex).added = true;
                    processIndex++;
                }

                if (cur.remaining == 0) {
                    cur.finish(time);
                    completedCount++;
                    cur = null;
                    finished = true;
                    break;
                }
            }

            if (finished) continue;

            // --- PHASE 2: Priority ---
            while (timeSlice < limitF2) {
                AGProcess higher = null;
                for (AGProcess p : ready) {
                    if (p.priority < cur.priority) {
                        if (higher == null || p.priority < higher.priority) {
                            higher = p;
                        }
                    }
                }

                if (higher != null) {
                    int unused = q - timeSlice;
                    cur.quantum += (int) Math.ceil(unused / 2.0);
                    cur.quantumHistory.add(cur.quantum);

                    ready.add(cur);
                    ready.remove(higher);
                    ready.addFirst(higher);

                    cur = null;
                    preempted = true;
                    break;
                }

                cur.remaining--;
                timeSlice++;
                time++;

                while (processIndex < n && processes.get(processIndex).arrival <= time) {
                    ready.add(processes.get(processIndex));
                    processes.get(processIndex).added = true;
                    processIndex++;
                }

                if (cur.remaining == 0) {
                    cur.finish(time);
                    completedCount++;
                    cur = null;
                    finished = true;
                    break;
                }
            }

            if (finished || preempted) continue;

            // --- PHASE 3: SJF ---
            while (timeSlice < q) {
                AGProcess shorter = null;
                for (AGProcess p : ready) {
                    if (p.remaining < cur.remaining) {
                        if (shorter == null || p.remaining < shorter.remaining) {
                            shorter = p;
                        }
                    }
                }

                if (shorter != null) {
                    int unused = q - timeSlice;
                    cur.quantum += unused;
                    cur.quantumHistory.add(cur.quantum);

                    ready.add(cur);
                    ready.remove(shorter);
                    ready.addFirst(shorter);

                    cur = null;
                    preempted = true;
                    break;
                }

                cur.remaining--;
                timeSlice++;
                time++;

                while (processIndex < n && processes.get(processIndex).arrival <= time) {
                    ready.add(processes.get(processIndex));
                    processes.get(processIndex).added = true;
                    processIndex++;
                }

                if (cur.remaining == 0) {
                    cur.finish(time);
                    completedCount++;
                    cur = null;
                    finished = true;
                    break;
                }
            }

            if (finished || preempted) continue;

            // --- PHASE 4: Quantum Exhausted ---
            cur.quantum += 2;
            cur.quantumHistory.add(cur.quantum);
            ready.add(cur);
            cur = null;
        }

        return AGResultBuilder.build(processes, executionOrder);
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
    int quantum;
    boolean added = false;
    List<Integer> quantumHistory = new ArrayList<>();

    AGProcess(String n, int a, int b, int p, int q) {
        super(n,a,b,p);
        this.quantum = q;
        this.quantumHistory.add(q);
    }
    void finish(int time) {
        completedTime = time;
        turnaround = time - arrival;
        waiting = turnaround - burst;
        quantum = 0;
        quantumHistory.add(0);
    }
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
            res.add(new ProcessResult(p.name, p.waiting, p.turnaround, p.quantumHistory));
            tw += p.waiting; tt += p.turnaround;
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
    List<ProcessInput> processes;
}
class ProcessInput {
    String name;
    int arrival, burst, priority, quantum;
}
class ExpectedOutput {
    SJFExpected SJF;
    SJFExpected RR;
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




