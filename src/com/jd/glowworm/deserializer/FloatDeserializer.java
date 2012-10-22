package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class FloatDeserializer implements ObjectDeserializer {

    public final static FloatDeserializer instance = new FloatDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
    	Float value = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				value = parser.scanFloat();
    			}
    		}
    		else
    		{
    			value = parser.scanFloat();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

        return (T) value;
    }
}
