package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class JavaObjectDeserializer implements ObjectDeserializer {

    public final static JavaObjectDeserializer instance = new JavaObjectDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        return (T) parser.parse(fieldName);
    }
}
