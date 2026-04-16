package com.cardemo.batch.orchestration.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Captures the result of executing a multi-step batch job flow.
 * Tracks per-step return codes and the overall flow outcome.
 */
public class JobFlowResult {

    private final String flowName;
    private final List<StepResult> stepResults;
    private final Instant startTime;
    private Instant endTime;
    private ReturnCode overallReturnCode;

    public JobFlowResult(String flowName) {
        this.flowName = flowName;
        this.stepResults = new ArrayList<>();
        this.startTime = Instant.now();
        this.overallReturnCode = new ReturnCode(ReturnCode.SUCCESS);
    }

    public void addStepResult(StepResult stepResult) {
        stepResults.add(stepResult);
        if (stepResult.getReturnCode().getCode() > overallReturnCode.getCode()) {
            overallReturnCode = stepResult.getReturnCode();
        }
    }

    public void complete() {
        this.endTime = Instant.now();
    }

    public String getFlowName() {
        return flowName;
    }

    public List<StepResult> getStepResults() {
        return Collections.unmodifiableList(stepResults);
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public ReturnCode getOverallReturnCode() {
        return overallReturnCode;
    }

    public boolean isSuccessful() {
        return !overallReturnCode.isError();
    }

    /**
     * Result of a single step within the flow.
     */
    public static class StepResult {
        private final String stepName;
        private final ReturnCode returnCode;
        private final Instant startTime;
        private final Instant endTime;

        public StepResult(String stepName, ReturnCode returnCode,
                          Instant startTime, Instant endTime) {
            this.stepName = stepName;
            this.returnCode = returnCode;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getStepName() {
            return stepName;
        }

        public ReturnCode getReturnCode() {
            return returnCode;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public Instant getEndTime() {
            return endTime;
        }
    }
}
