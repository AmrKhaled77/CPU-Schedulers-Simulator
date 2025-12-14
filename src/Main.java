import com.google.gson.Gson;
import java.io.FileReader;
import java.util.*;

/* ======================= MAIN ======================= */
public class Main {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
        Gson gson = new Gson();

        System.out.println("============================================");
        System.out.println("CPU Scheduling Simulator");
        System.out.println("============================================");
        System.out.println("1 - Run test case files (JSON)");
        System.out.println("2 - Enter processes manually");
        System.out.print("Choose option: ");

        int choice = sc.nextInt();

        if (choice == 1) {
            runTestFiles(gson);
        } else if (choice == 2) {
            runManualInput(sc);
        } else {
            System.out.println("Invalid choice ❌");
        }

        sc.close();
    }

    /* ================= RUN TEST FILES ================= */
    static void runTestFiles(Gson gson) throws Exception {

        for (int i = 1; i <= 5; i++) {

            String filePath =
                    "test_cases_updated/test_cases/Other_Schedulers/test_" + i + ".json";

            System.out.println("\n\n============================================");
            System.out.println("RUNNING TEST FILE: test_" + i + ".json");
            System.out.println("============================================");

            FileReader reader = new FileReader(filePath);
            TestFile testFile = gson.fromJson(reader, TestFile.class);
            reader.close();

            int cs = testFile.input.contextSwitch;
            int quantum = testFile.input.rrQuantum;

            ArrayList<Process> base =
                    buildProcesses(testFile.input.processes);

            /* ===== SJF ===== */
            SJFResult sjfActual =
                    PreemptiveSJF.simulateSJF(cloneProcesses(base), cs);

            printAndValidate("SJF", sjfActual,
                    testFile.expectedOutput.SJF);

            /* ===== RR ===== */
            SJFResult rrActual =
                    RoundRobin.simulateRR(cloneProcesses(base), quantum, cs);

            printAndValidate("RR", rrActual,
                    testFile.expectedOutput.RR);
        }
    }

    /* ================= RUN MANUAL INPUT ================= */
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

        SJFResult rr =
                RoundRobin.simulateRR(cloneProcesses(processes), quantum, cs);

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

    /* ================= UTILITIES ================= */
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

    /* ================= PRINT + VALIDATE ================= */
    static void printAndValidate(String title,
                                 SJFResult actual,
                                 SJFExpected expected) {

        System.out.println("\n========= " + title + " OUTPUT =========");

        // Execution Order
        System.out.println("\nExecution Order:");
        System.out.println("Actual   : " + actual.executionOrder);
        System.out.println("Expected : " + expected.executionOrder);

        boolean pass = true;

        // Process Results
        System.out.println("\nProcess Results:");
        System.out.printf("%-5s %-15s %-15s %-15s %-15s%n",
                "PID", "Wait(A)", "Wait(E)", "TAT(A)", "TAT(E)");

        for (int i = 0; i < expected.processResults.size(); i++) {

            ProcessResult a = actual.processResults.get(i);
            ProcessResult e = expected.processResults.get(i);

            System.out.printf("%-5s %-15d %-15d %-15d %-15d%n",
                    a.name,
                    a.waitingTime, e.waitingTime,
                    a.turnaroundTime, e.turnaroundTime);

            if (a.waitingTime != e.waitingTime ||
                    a.turnaroundTime != e.turnaroundTime)
                pass = false;
        }

        // Averages
        System.out.println("\nAverages:");
        System.out.printf("Average Waiting Time   -> Actual: %.2f | Expected: %.2f%n",
                actual.avgWait, expected.averageWaitingTime);

        System.out.printf("Average Turnaround Time-> Actual: %.2f | Expected: %.2f%n",
                actual.avgTAT, expected.averageTurnaroundTime);

        if (Math.abs(actual.avgWait - expected.averageWaitingTime) > 0.01 ||
                Math.abs(actual.avgTAT - expected.averageTurnaroundTime) > 0.01)
            pass = false;

        System.out.println("\n============================================");
        System.out.println(pass
                ? "🎉 " + title + " TEST PASSED"
                : "🔥 " + title + " TEST FAILED");
        System.out.println("============================================");
    }
}

/* ======================= SJF ======================= */
class PreemptiveSJF {

    public static SJFResult simulateSJF(ArrayList<Process> processes, int cs) {

        int time = 0, completed = 0;
        String last = "";
        ArrayList<String> order = new ArrayList<>();

        processes.sort(Comparator.comparingInt(p -> p.arrival));

        while (completed < processes.size()) {

            Process cur = null;
            for (Process p : processes)
                if (p.arrival <= time && p.remaining > 0)
                    if (cur == null || p.remaining < cur.remaining)
                        cur = p;

            if (cur == null) {
                time++;
                last = "";
                continue;
            }

            if (!last.equals("") && !last.equals(cur.name))
                time += cs;

            if (!cur.name.equals(last))
                order.add(cur.name);

            cur.remaining--;
            time++;
            last = cur.name;

            if (cur.remaining == 0) {
                completed++;
                cur.turnaround = time - cur.arrival;
                cur.waiting = cur.turnaround - cur.burst;
            }
        }

        return ResultBuilder.build(processes, order);
    }
}

/* ======================= ROUND ROBIN ======================= */
class RoundRobin {

    public static SJFResult simulateRR(List<Process> processes,
                                       int quantum,
                                       int contextSwitch) {

        Queue<Process> ready = new LinkedList<>();
        List<String> order = new ArrayList<>();

        int time = 0, completed = 0, n = processes.size();
        processes.sort(Comparator.comparingInt(p -> p.arrival));
        boolean[] added = new boolean[n];

        while (completed < n) {

            for (int i = 0; i < n; i++)
                if (!added[i] && processes.get(i).arrival <= time) {
                    ready.add(processes.get(i));
                    added[i] = true;
                }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            Process cur = ready.poll();
            order.add(cur.name);

            int exec = Math.min(quantum, cur.remaining);
            cur.remaining -= exec;
            time += exec;

            for (int i = 0; i < n; i++)
                if (!added[i] && processes.get(i).arrival <= time) {
                    ready.add(processes.get(i));
                    added[i] = true;
                }

            if (cur.remaining > 0)
                ready.add(cur);
            else {
                cur.completedTime = time;
                completed++;
            }

            if (!ready.isEmpty())
                time += contextSwitch;
        }

        for (Process p : processes) {
            p.turnaround = p.completedTime - p.arrival;
            p.waiting = p.turnaround - p.burst;
        }

        return ResultBuilder.build(processes, order);
    }
}

/* ======================= RESULT BUILDER ======================= */
class ResultBuilder {

    static SJFResult build(List<Process> processes, List<String> order) {

        double tw = 0, tt = 0;
        List<ProcessResult> results = new ArrayList<>();

        for (Process p : processes) {
            results.add(new ProcessResult(p.name, p.waiting, p.turnaround));
            tw += p.waiting;
            tt += p.turnaround;
        }

        return new SJFResult(order, results,
                tw / processes.size(),
                tt / processes.size());
    }
}

/* ======================= MODELS ======================= */
class Process {
    String name;
    int arrival, burst, priority;
    int remaining, waiting, turnaround;
    int completedTime;

    Process(String n, int a, int b, int p) {
        name = n;
        arrival = a;
        burst = b;
        priority = p;
        remaining = b;
    }
}

class ProcessResult {
    String name;
    int waitingTime, turnaroundTime;

    ProcessResult(String n, int w, int t) {
        name = n;
        waitingTime = w;
        turnaroundTime = t;
    }
}

class SJFResult {
    List<String> executionOrder;
    List<ProcessResult> processResults;
    double avgWait, avgTAT;

    SJFResult(List<String> o, List<ProcessResult> p, double w, double t) {
        executionOrder = o;
        processResults = p;
        avgWait = w;
        avgTAT = t;
    }
}

/* ======================= JSON ======================= */
class TestFile {
    String name;
    Input input;
    ExpectedOutput expectedOutput;
}

class Input {
    int contextSwitch;
    int rrQuantum;
    int agingInterval;
    List<ProcessInput> processes;
}

class ProcessInput {
    String name;
    int arrival, burst, priority;
}

class ExpectedOutput {
    SJFExpected SJF;
    SJFExpected RR;
    SJFExpected Priority;
}

class SJFExpected {
    List<String> executionOrder;
    List<ProcessResult> processResults;
    double averageWaitingTime;
    double averageTurnaroundTime;
}
