package ai.cognition.airlift.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * The boundaries of a COBOL run: WORKING-STORAGE is fresh when the step starts, and
 * whatever the program does after its main loop falls through happens when the step
 * ends.
 */
final class ProgramStepListener implements StepExecutionListener {

  private final Runnable start;
  private final Runnable end;

  ProgramStepListener(Runnable start, Runnable end) {
    this.start = start;
    this.end = end;
  }

  @Override
  public void beforeStep(StepExecution stepExecution) {
    start.run();
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    end.run();
    return stepExecution.getExitStatus();
  }
}
