package ai.cognition.airlift.batch;

import ai.cognition.airlift.io.FixedRecordFile;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.batch.item.ExecutionContext;

/**
 * Applies one record's worth of program logic and appends whatever that logic
 * produced to an output dataset, which is opened for the life of the step exactly
 * as an OPEN OUTPUT / CLOSE pair spans a COBOL run.
 */
public final class DatasetWriter implements ItemStreamWriter<byte[]> {

  private final Path path;
  private final Function<byte[], List<byte[]>> logic;
  private FixedRecordFile.Writer writer;

  public DatasetWriter(Path path, Function<byte[], List<byte[]>> logic) {
    this.path = path;
    this.logic = logic;
  }

  @Override
  public void open(ExecutionContext executionContext) {
    writer = FixedRecordFile.writer(path);
  }

  @Override
  public void write(Chunk<? extends byte[]> chunk) {
    for (byte[] image : chunk) {
      logic.apply(image).forEach(writer::write);
    }
  }

  @Override
  public void close() {
    if (writer != null) {
      writer.close();
      writer = null;
    }
  }
}
