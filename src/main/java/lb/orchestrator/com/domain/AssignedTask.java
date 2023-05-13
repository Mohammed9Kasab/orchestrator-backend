package lb.orchestrator.com.domain;

public class AssignedTask {
    int jobID;
    int taskID;
    int start;
    int duration;

    // Ctor
    public AssignedTask(int jobID, int taskID, int start, int duration) {
        this.jobID = jobID;
        this.taskID = taskID;
        this.start = start;
        this.duration = duration;
    }

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
