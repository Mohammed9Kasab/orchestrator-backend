package lb.orchestrator.com.helper;

import lb.orchestrator.com.domain.AssignedTask;

import java.util.Comparator;

public class SortTasks implements Comparator<AssignedTask> {
    @Override
    public int compare(AssignedTask a, AssignedTask b) {
        if (a.getStart() != b.getStart()) {
            return a.getStart() - b.getStart();
        } else {
            return a.getDuration() - b.getDuration();
        }
    }
}
