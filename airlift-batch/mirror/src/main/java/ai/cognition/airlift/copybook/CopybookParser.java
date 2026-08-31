package ai.cognition.airlift.copybook;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the fixed-format copybooks in {@code app/cpy} and derives record layouts
 * from them.
 *
 * <p>The mirror does not carry hand-written offsets: every layout it reads or
 * writes comes from the same copybook text the COBOL programs COPY, so a change
 * to a copybook moves both sides at once. FILLER items are kept as unnamed
 * padding rather than dropped, because the mirror has to rewrite records
 * byte-for-byte to stay comparable with the COBOL outputs.
 */
public final class CopybookParser {

  private static final int INDICATOR_COLUMN = 6;
  private static final int CODE_END = 72;

  private static final Pattern LEVEL =
      Pattern.compile("^\\s*(\\d\\d)\\s+([A-Z0-9$#@-]+|FILLER)\\b(.*)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern PICTURE =
      Pattern.compile("\\b(?:PIC|PICTURE)\\s+(?:IS\\s+)?(\\S+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern USAGE =
      Pattern.compile(
          "\\b(?:USAGE\\s+(?:IS\\s+)?)?(COMP-3|COMPUTATIONAL-3|PACKED-DECIMAL|COMP-5|COMP-4"
              + "|COMP|COMPUTATIONAL|BINARY|DISPLAY)\\b",
          Pattern.CASE_INSENSITIVE);
  private static final Pattern REDEFINES =
      Pattern.compile("\\bREDEFINES\\s+([A-Z0-9$#@-]+)", Pattern.CASE_INSENSITIVE);
  private static final Pattern OCCURS = Pattern.compile("\\bOCCURS\\s+(\\d+)", Pattern.CASE_INSENSITIVE);

  private final Path copybookDirectory;

  public CopybookParser(Path copybookDirectory) {
    this.copybookDirectory = copybookDirectory;
  }

  /** Load one 01-level record from {@code <copybook>.cpy}. */
  public RecordLayout load(String copybook, String recordName) {
    Path source = resolve(copybook);
    String text;
    try {
      text = Files.readString(source, StandardCharsets.ISO_8859_1);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
    for (RecordLayout layout : parse(text)) {
      if (layout.name().equalsIgnoreCase(recordName)) {
        return layout;
      }
    }
    throw new CopybookException(recordName + " not found in " + source);
  }

  private Path resolve(String copybook) {
    for (String suffix : new String[] {".cpy", ".CPY"}) {
      Path candidate = copybookDirectory.resolve(copybook + suffix);
      if (Files.exists(candidate)) {
        return candidate;
      }
    }
    throw new CopybookException("copybook " + copybook + " not found under " + copybookDirectory);
  }

  /** Parse copybook text into every 01-level record it declares. */
  public List<RecordLayout> parse(String text) {
    List<RecordLayout> layouts = new ArrayList<>();
    List<Node> roots = new ArrayList<>();
    Deque<Node> open = new ArrayDeque<>();
    for (String statement : statements(text)) {
      Matcher matcher = LEVEL.matcher(statement);
      if (!matcher.matches()) {
        continue;
      }
      int level = Integer.parseInt(matcher.group(1));
      if (level == 88) {
        continue;
      }
      String name = matcher.group(2).toUpperCase(Locale.ROOT);
      String rest = matcher.group(3);
      while (!open.isEmpty() && open.peek().level >= level) {
        open.pop();
      }
      Node parent = open.peek();
      if (parent == null && level != 1) {
        continue;
      }
      Node node = new Node(name, level);
      Matcher redefines = REDEFINES.matcher(rest);
      if (redefines.find()) {
        node.redefines = redefines.group(1).toUpperCase(Locale.ROOT);
      }
      Matcher occurs = OCCURS.matcher(rest);
      int repeat = occurs.find() ? Integer.parseInt(occurs.group(1)) : 1;
      Matcher usage = USAGE.matcher(rest);
      String usageName = usage.find() ? usage.group(1).toUpperCase(Locale.ROOT) : "DISPLAY";
      Matcher picture = PICTURE.matcher(rest);
      if (picture.find()) {
        node.picture = trimTrailingPeriod(picture.group(1));
        Picture parsed = Picture.of(node.picture, usageName);
        node.size = parsed.size() * repeat;
        node.digits = parsed.digits();
        node.scale = parsed.scale();
        node.signed = parsed.signed();
        node.numeric = parsed.numeric();
        node.usage = parsed.numeric() ? parsed.usage() : FieldDef.Usage.DISPLAY;
      }
      if (parent == null) {
        roots.add(node);
      } else {
        if (node.redefines != null) {
          node.offset =
              parent.children.stream()
                  .filter(sibling -> sibling.name.equals(node.redefines))
                  .findFirst()
                  .map(sibling -> sibling.offset)
                  .orElse(parent.offset);
        } else if (parent.children.isEmpty()) {
          node.offset = parent.offset;
        } else {
          Node previous = parent.children.get(parent.children.size() - 1);
          node.offset = previous.offset + previous.size;
        }
        parent.children.add(node);
      }
      open.push(node);
      if (node.redefines == null) {
        for (Node ancestor : open) {
          ancestor.size = Math.max(ancestor.size, node.offset + node.size - ancestor.offset);
        }
      }
    }
    for (Node root : roots) {
      List<FieldDef> fields = new ArrayList<>();
      flatten(root, "", fields);
      layouts.add(new RecordLayout(root.name, root.size, fields));
    }
    return layouts;
  }

  private void flatten(Node node, String prefix, List<FieldDef> fields) {
    for (Node child : node.children) {
      if (child.redefines != null) {
        continue;
      }
      String name = prefix.isEmpty() ? child.name : prefix + "." + child.name;
      if (!child.children.isEmpty()) {
        flatten(child, name, fields);
      } else if (!"FILLER".equals(child.name)) {
        fields.add(
            new FieldDef(
                name,
                child.offset,
                child.size,
                child.picture,
                child.usage,
                child.digits,
                child.scale,
                child.signed,
                child.numeric));
      }
    }
  }

  /** Strip the sequence area, comment lines and columns past 72; split on periods. */
  private List<String> statements(String text) {
    List<String> out = new ArrayList<>();
    StringBuilder buffer = new StringBuilder();
    for (String raw : text.split("\\R", -1)) {
      String padded = raw.length() < CODE_END ? raw + " ".repeat(CODE_END - raw.length()) : raw;
      char indicator = padded.charAt(INDICATOR_COLUMN);
      if (indicator == '*' || indicator == '/') {
        continue;
      }
      String code = padded.substring(INDICATOR_COLUMN, CODE_END).trim();
      if (code.isEmpty()) {
        continue;
      }
      buffer.append(buffer.isEmpty() ? "" : " ").append(code);
      int period;
      while ((period = buffer.indexOf(".")) >= 0) {
        String head = buffer.substring(0, period).trim();
        if (!head.isEmpty()) {
          out.add(head);
        }
        buffer = new StringBuilder(buffer.substring(period + 1).trim());
      }
    }
    if (!buffer.isEmpty()) {
      out.add(buffer.toString().trim());
    }
    return out;
  }

  private static String trimTrailingPeriod(String value) {
    return value.endsWith(".") ? value.substring(0, value.length() - 1) : value;
  }

  private static final class Node {
    private final String name;
    private final int level;
    private final List<Node> children = new ArrayList<>();
    private int offset;
    private int size;
    private String picture;
    private FieldDef.Usage usage = FieldDef.Usage.DISPLAY;
    private int digits;
    private int scale;
    private boolean signed;
    private boolean numeric;
    private String redefines;

    private Node(String name, int level) {
      this.name = name;
      this.level = level;
    }
  }
}
