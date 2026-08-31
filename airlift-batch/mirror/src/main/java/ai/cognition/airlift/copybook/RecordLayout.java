package ai.cognition.airlift.copybook;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A copybook 01-level record: its total length and its elementary items. */
public final class RecordLayout {

  private final String name;
  private final int length;
  private final List<FieldDef> fields;
  private final Map<String, FieldDef> byName;

  public RecordLayout(String name, int length, List<FieldDef> fields) {
    this.name = name;
    this.length = length;
    this.fields = List.copyOf(fields);
    Map<String, FieldDef> index = new LinkedHashMap<>();
    for (FieldDef field : this.fields) {
      index.put(field.name(), field);
      String unqualified = field.name().substring(field.name().lastIndexOf('.') + 1);
      index.putIfAbsent(unqualified, field);
    }
    this.byName = Map.copyOf(index);
  }

  public String name() {
    return name;
  }

  public int length() {
    return length;
  }

  public List<FieldDef> fields() {
    return fields;
  }

  public FieldDef field(String name) {
    FieldDef field = byName.get(name.toUpperCase());
    if (field == null) {
      throw new CopybookException(this.name + " has no field " + name);
    }
    return field;
  }
}
