package lb.orchestrator.com.helper;

import java.util.Comparator;
import org.springframework.stereotype.Service;

@Service
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
