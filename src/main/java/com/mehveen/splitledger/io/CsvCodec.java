package com.mehveen.splitledger.io;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal CSV encoder/decoder (RFC 4180 style quoting) for single-line records.
 * Written by hand so the project has zero runtime dependencies.
 */
public final class CsvCodec {

    private CsvCodec() {
    }

    public static String encode(List<String> fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(fields.get(i)));
        }
        return line.toString();
    }

    static String escape(String field) {
        if (field == null) {
            return "";
        }
        if (field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("Fields cannot contain line breaks");
        }
        if (field.indexOf(',') >= 0 || field.indexOf('"') >= 0
                || (!field.isEmpty() && (Character.isWhitespace(field.charAt(0))
                || Character.isWhitespace(field.charAt(field.length() - 1))))) {
            return '"' + field.replace("\"", "\"\"") + '"';
        }
        return field;
    }

    public static List<String> decode(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Unterminated quote in line: " + line);
        }
        fields.add(current.toString());
        return fields;
    }
}
