package ai.cognition.airlift.copybook;

/** Raised when a copybook cannot be parsed or a field is not part of a layout. */
public class CopybookException extends RuntimeException {

  public CopybookException(String message) {
    super(message);
  }
}
