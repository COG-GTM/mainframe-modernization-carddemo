package ai.cognition.airlift.store;

import org.springframework.jdbc.core.RowMapper;

final class RowMappers {

  static final RowMapper<byte[]> IMAGE = (row, index) -> row.getBytes("image");

  private RowMappers() {}
}
