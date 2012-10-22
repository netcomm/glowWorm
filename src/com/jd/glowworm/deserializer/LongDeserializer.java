package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class LongDeserializer implements ObjectDeserializer {

    public final static LongDeserializer instance = new LongDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
    	Long value = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				value = parser.scanLong();
    			}
    		}
    		else
    		{
    			value = parser.scanLong();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

        return (T) value;
    }
}
