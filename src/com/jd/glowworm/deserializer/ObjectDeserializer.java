package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public interface ObjectDeserializer {
    <T> T deserialze(PBDeserializer parser, Type type, Object fieldName);
}
