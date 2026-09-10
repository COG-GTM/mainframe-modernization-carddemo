package com.carddemo.model.codec;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Reads and writes the fixed length records described by the copybooks in {@code app/cpy}.
 *
 * <p>Alphanumeric fields are trimmed of trailing spaces on read and space padded on write, numeric
 * fields keep their COBOL digit count, so a decode/encode round trip reproduces the original record
 * image.
 */
public final class FixedWidthCodec {

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private FixedWidthCodec() {
    }

    public static int recordLength(Class<?> type) {
        return layout(type).length();
    }

    public static <T> T decode(String record, Class<T> type) {
        int length = recordLength(type);
        String padded = record.length() >= length ? record : record + " ".repeat(length - record.length());
        try {
            T target = type.getDeclaredConstructor().newInstance();
            for (Field field : fields(type)) {
                CobolField spec = field.getAnnotation(CobolField.class);
                String raw = padded.substring(spec.offset(), spec.offset() + spec.length());
                field.setAccessible(true);
                field.set(target, convertFromRecord(raw, spec, field.getType()));
            }
            return target;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot decode " + type.getName(), e);
        }
    }

    public static String encode(Object source) {
        Class<?> type = source.getClass();
        StringBuilder record = new StringBuilder(" ".repeat(recordLength(type)));
        try {
            for (Field field : fields(type)) {
                CobolField spec = field.getAnnotation(CobolField.class);
                field.setAccessible(true);
                String raw = convertToRecord(field.get(source), spec);
                record.replace(spec.offset(), spec.offset() + spec.length(), raw);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot encode " + type.getName(), e);
        }
        return record.toString();
    }

    private static Object convertFromRecord(String raw, CobolField spec, Class<?> javaType) {
        switch (spec.type()) {
            case ALPHANUMERIC:
                return stripTrailing(raw);
            case UNSIGNED:
                Long unsigned = ZonedDecimal.decodeUnsigned(raw);
                if (unsigned == null) {
                    return null;
                }
                if (javaType == Integer.class) {
                    return Math.toIntExact(unsigned);
                }
                if (javaType == String.class) {
                    return raw;
                }
                return unsigned;
            case SIGNED:
                return ZonedDecimal.decodeSigned(raw, spec.scale());
            default:
                throw new IllegalStateException("Unsupported PIC type " + spec.type());
        }
    }

    private static String convertToRecord(Object value, CobolField spec) {
        switch (spec.type()) {
            case ALPHANUMERIC:
                String text = value == null ? "" : value.toString();
                if (text.length() > spec.length()) {
                    throw new IllegalArgumentException(spec.name() + " exceeds PIC X(" + spec.length() + ")");
                }
                return text + " ".repeat(spec.length() - text.length());
            case UNSIGNED:
                Long number = value == null ? null : Long.valueOf(((Number) value).longValue());
                return ZonedDecimal.encodeUnsigned(number, spec.length());
            case SIGNED:
                return ZonedDecimal.encodeSigned((BigDecimal) value, spec.length(), spec.scale());
            default:
                throw new IllegalStateException("Unsupported PIC type " + spec.type());
        }
    }

    private static String stripTrailing(String raw) {
        int end = raw.length();
        while (end > 0) {
            char c = raw.charAt(end - 1);
            if (c != ' ' && c != '\u0000') {
                break;
            }
            end--;
        }
        return raw.substring(0, end);
    }

    private static CobolRecord layout(Class<?> type) {
        CobolRecord layout = type.getAnnotation(CobolRecord.class);
        if (layout == null) {
            throw new IllegalArgumentException(type.getName() + " is not annotated with @CobolRecord");
        }
        return layout;
    }

    private static List<Field> fields(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, key -> {
            List<Field> annotated = new ArrayList<>();
            for (Field field : key.getDeclaredFields()) {
                if (field.isAnnotationPresent(CobolField.class)) {
                    annotated.add(field);
                }
            }
            return List.copyOf(annotated);
        });
    }
}
