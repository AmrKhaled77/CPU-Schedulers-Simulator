public class Process {
    String name;
    int arrival;
    int burst;
    int priority;
    int remaining;
    int waiting = 0;
    int turnaround = 0;
    int quantum = 0;

    public Process(String name, int arrival, int burst, int priority, int quantum) {
        this.name = name;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.remaining = burst;
        this.quantum = quantum;
    }
}
