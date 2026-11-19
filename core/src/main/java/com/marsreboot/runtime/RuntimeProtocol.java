package com.marsreboot.runtime;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import java.io.*;
import java.nio.charset.*;

/** Small strict envelope around libGDX's existing JSON parser; no new JSON dependency. */
final class RuntimeProtocol {
    private final long generation;
    private final String scenarioId;
    private long incoming, outgoing;
    RuntimeProtocol(long generation) { this(generation, "p1-a"); }
    RuntimeProtocol(long generation, String scenarioId) { this.generation = generation; this.scenarioId = scenarioId; }
    String encode(String type, JsonValue payload) {
        JsonValue frame = object();
        frame.addChild("protocolVersion", new JsonValue(1L));
        frame.addChild("generation", new JsonValue(generation));
        frame.addChild("runId", new JsonValue("run-" + generation));
        frame.addChild("scenarioId", new JsonValue(scenarioId));
        frame.addChild("sequence", new JsonValue(++outgoing));
        frame.addChild("type", new JsonValue(type)); frame.addChild("payload", payload);
        return frame.toJson(OutputType.json);
    }
    JsonValue decode(String line) {
        JsonValue frame = new JsonReader().parse(line);
        if (!frame.isObject() || integer(frame,"protocolVersion") != 1
            || integer(frame,"generation") != generation || !string(frame,"runId").equals("run-" + generation)
            || !string(frame,"scenarioId").equals(scenarioId) || integer(frame,"sequence") != incoming + 1
            || !required(frame,"payload").isObject()) throw new IllegalArgumentException("Identitas/urutan frame tidak sah.");
        string(frame,"type"); incoming++; return frame;
    }
    static JsonValue object() { return new JsonValue(JsonValue.ValueType.object); }
    static JsonValue required(JsonValue object, String key) {
        JsonValue value = object.get(key); if (value == null) throw new IllegalArgumentException("Field hilang: " + key); return value;
    }
    static String string(JsonValue object, String key) {
        JsonValue value = required(object,key); if (!value.isString()) throw new IllegalArgumentException("String diperlukan: " + key); return value.asString();
    }
    static long integer(JsonValue object, String key) {
        JsonValue value = required(object,key); if (!value.isLong()) throw new IllegalArgumentException("Integer diperlukan: " + key); return value.asLong();
    }
    static String readLine(InputStream input, int limit) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        for (int value; (value = input.read()) != -1;) {
            if (value == '\n') return utf8(bytes);
            if (bytes.size() >= limit - 1) throw new IOException("Frame melampaui batas byte.");
            bytes.write(value);
        }
        if (bytes.size() != 0) throw new IOException("Frame protokol terpotong.");
        return null;
    }
    private static String utf8(ByteArrayOutputStream bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(java.nio.ByteBuffer.wrap(bytes.toByteArray())).toString();
    }
}
