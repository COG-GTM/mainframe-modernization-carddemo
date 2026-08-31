package ai.cognition.airlift.batch;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;

/**
 * Sequential read over record images. OPEN INPUT resolves the dataset, so its
 * contents are whatever the earlier steps of this run left there, and a second run
 * starts from the beginning.
 */
public final class ImageReader implements ItemStreamReader<byte[]> {

  private final Supplier<List<byte[]>> source;
  private Iterator<byte[]> records;

  public ImageReader(Supplier<List<byte[]>> source) {
    this.source = source;
  }

  @Override
  public void open(ExecutionContext executionContext) {
    records = source.get().iterator();
  }

  @Override
  public void close() {
    records = null;
  }

  @Override
  public byte[] read() {
    return records.hasNext() ? records.next() : null;
  }
}
