package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class StringDeserializer implements ObjectDeserializer {

    public final static StringDeserializer instance = new StringDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(PBDeserializer parser) {
    	String value = null;
    	try
    	{
    		value = parser.scanString();
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	
    	return (T) value;
    }
}
