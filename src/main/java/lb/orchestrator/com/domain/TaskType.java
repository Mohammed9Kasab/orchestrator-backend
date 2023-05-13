package lb.orchestrator.com.domain;

import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.IntervalVar;

public class TaskType {
    IntVar start;
    IntVar end;
    IntervalVar interval;

    public IntVar getStart() {
        return start;
    }

    public void setStart(IntVar start) {
        this.start = start;
    }

    public IntVar getEnd() {
        return end;
    }

    public void setEnd(IntVar end) {
        this.end = end;
    }

    public IntervalVar getInterval() {
        return interval;
    }

    public void setInterval(IntervalVar interval) {
        this.interval = interval;
    }
}
