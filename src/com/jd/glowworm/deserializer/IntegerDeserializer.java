package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class IntegerDeserializer implements ObjectDeserializer {
    public final static IntegerDeserializer instance = new IntegerDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser, fieldName);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(PBDeserializer parser, Object fieldName) {
    	Integer value = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				value = parser.scanInt();
    			}
    		}
    		else
    		{
    			value = parser.scanInt();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

        return (T) value;
    }
}
