package ai.cognition.airlift.io;

import ai.cognition.airlift.codec.CodecException;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fixed-length unblocked dataset access: no record delimiter, every record is the
 * same number of bytes, which is what RECFM=FB gives the COBOL side.
 */
public final class FixedRecordFile {

  private FixedRecordFile() {}

  public static List<byte[]> read(Path path, int recordLength) {
    byte[] data;
    try {
      data = Files.readAllBytes(path);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
    if (data.length % recordLength != 0) {
      throw new CodecException(
          path + ": length " + data.length + " is not a multiple of " + recordLength);
    }
    List<byte[]> records = new ArrayList<>(data.length / recordLength);
    for (int offset = 0; offset < data.length; offset += recordLength) {
      records.add(Arrays.copyOfRange(data, offset, offset + recordLength));
    }
    return records;
  }

  /** OPEN OUTPUT on a sequential dataset: create or replace, then append records. */
  public static Writer writer(Path path) {
    return new Writer(path);
  }

  public static final class Writer implements AutoCloseable {

    private final OutputStream stream;

    private Writer(Path path) {
      try {
        Files.createDirectories(path.getParent());
        this.stream =
            new BufferedOutputStream(
                Files.newOutputStream(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING));
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
    }

    public void write(byte[] record) {
      try {
        stream.write(record);
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
    }

    @Override
    public void close() {
      try {
        stream.close();
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
    }
  }
}
