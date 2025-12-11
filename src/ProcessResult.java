
import java.util.List;

public class ProcessResult {
    public String name;
    public int waitingTime;
    public int turnaroundTime;

    public ProcessResult(String name, int waitingTime, int turnaroundTime) {
        this.name = name;
        this.waitingTime = waitingTime;
        this.turnaroundTime = turnaroundTime;
    }
}



