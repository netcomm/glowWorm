package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class StringDeserializer implements ObjectDeserializer {

    public final static StringDeserializer instance = new StringDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser, fieldName);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(PBDeserializer parser, Object fieldName) {
    	String value = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				value = parser.scanString();
    			}
    		}
    		else
    		{
    			value = parser.scanString();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	
    	return (T) value;
    }
}
