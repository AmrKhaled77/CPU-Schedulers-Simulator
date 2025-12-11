import java.util.List;

public class SJFResult {
     public List<String> executionOrder;
     public List<ProcessResult> processResults;
     public double averageWaitingTime;
     public double averageTurnaroundTime;

     public SJFResult(List<String> executionOrder,
                      List<ProcessResult> processResults,
                      double avgWait,
                      double avgTAT) {

         this.executionOrder = executionOrder;
         this.processResults = processResults;
         this.averageWaitingTime = avgWait;
         this.averageTurnaroundTime = avgTAT;
     }
 }
