package lb.orchestrator.com.service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultDTO implements Serializable {

    private boolean existSolution;

    private List<List<List<Integer>>> jspOutput = new ArrayList<>();

    private Integer jspEndTime ;

    private List<List<List<Integer>>> fcfsOutput = new ArrayList<>();

    private Integer fcfsEndTime ;

    private List<List<List<Integer>>> mrrOutput = new ArrayList<>();

    private Integer mrrEndTime ;

    private List<List<List<Integer>>> inputTuples = new ArrayList<>();

    public boolean isExistSolution() {
        return existSolution;
    }

    public void setExistSolution(boolean existSolution) {
        this.existSolution = existSolution;
    }

    public List<List<List<Integer>>> getFcfsOutput() {
        return fcfsOutput;
    }

    public void setFcfsOutput(List<List<List<Integer>>> fcfsOutput) {
        this.fcfsOutput = fcfsOutput;
    }

    public List<List<List<Integer>>> getJspOutput() {
        return jspOutput;
    }

    public void setJspOutput(List<List<List<Integer>>> jspOutput) {
        this.jspOutput = jspOutput;
    }

    public Integer getJspEndTime() {
        return jspEndTime;
    }

    public void setJspEndTime(Integer jspEndTime) {
        this.jspEndTime = jspEndTime;
    }

    public Integer getFcfsEndTime() {
        return fcfsEndTime;
    }

    public void setFcfsEndTime(Integer fcfsEndTime) {
        this.fcfsEndTime = fcfsEndTime;
    }

    public List<List<List<Integer>>> getMrrOutput() {
        return mrrOutput;
    }

    public void setMrrOutput(List<List<List<Integer>>> mrrOutput) {
        this.mrrOutput = mrrOutput;
    }

    public Integer getMrrEndTime() {
        return mrrEndTime;
    }

    public void setMrrEndTime(Integer mrrEndTime) {
        this.mrrEndTime = mrrEndTime;
    }

    public List<List<List<Integer>>> getInputTuples() {
        return inputTuples;
    }

    public void setInputTuples(List<List<List<Integer>>> inputTuples) {
        this.inputTuples = inputTuples;
    }
}
