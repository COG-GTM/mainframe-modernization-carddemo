package ai.cognition.airlift.codec;

/** Raised when record bytes do not match the copybook layout they are read with. */
public class CodecException extends RuntimeException {

  public CodecException(String message) {
    super(message);
  }
}
