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
}
