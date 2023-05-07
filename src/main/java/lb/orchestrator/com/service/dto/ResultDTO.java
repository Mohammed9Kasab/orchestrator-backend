package lb.orchestrator.com.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultDTO implements Serializable {

    private boolean existSolution;
    private String solution;
    private String value;
    private String map;
    private String statistics;
    private String conflicts;
    private String branches;
    private String wallTime;
    private List<List<List<Integer>>> outputMap = new ArrayList<>();
    private List<List<List<Integer>>> FCFS_Output = new ArrayList<>();
    private List<List<List<Integer>>> MMR_Output = new ArrayList<>();

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getStatistics() {
        return statistics;
    }

    public void setStatistics(String statistics) {
        this.statistics = statistics;
    }

    public String getConflicts() {
        return conflicts;
    }

    public void setConflicts(String conflicts) {
        this.conflicts = conflicts;
    }

    public String getBranches() {
        return branches;
    }

    public void setBranches(String branches) {
        this.branches = branches;
    }

    public String getWallTime() {
        return wallTime;
    }

    public void setWallTime(String wallTime) {
        this.wallTime = wallTime;
    }

    public boolean isExistSolution() {
        return existSolution;
    }

    public void setExistSolution(boolean existSolution) {
        this.existSolution = existSolution;
    }

    public List<List<List<Integer>>> getOutputMap() {
        return outputMap;
    }

    public void setOutputMap(List<List<List<Integer>>> outputMap) {
        this.outputMap = outputMap;
    }

    public List<List<List<Integer>>> getFCFS_Output() {
        return FCFS_Output;
    }

    public void setFCFS_Output(List<List<List<Integer>>> FCFS_Output) {
        this.FCFS_Output = FCFS_Output;
    }

    public List<List<List<Integer>>> getMMR_Output() {
        return MMR_Output;
    }

    public void setMMR_Output(List<List<List<Integer>>> MMR_Output) {
        this.MMR_Output = MMR_Output;
    }
}
