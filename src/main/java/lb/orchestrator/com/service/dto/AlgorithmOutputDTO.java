package lb.orchestrator.com.service.dto;

import java.util.List;

/**
 * A DTO for the {@link lb.orchestrator.com.domain.Worker} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlgorithmOutputDTO {

    private List<List<List<Integer>>> output;

    private int endTime;

    private Long implementationTime;

    public List<List<List<Integer>>> getOutput() {
        return output;
    }

    public void setOutput(List<List<List<Integer>>> output) {
        this.output = output;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Long getImplementationTime() {
        return implementationTime;
    }

    public void setImplementationTime(Long implementationTime) {
        this.implementationTime = implementationTime;
    }
}
