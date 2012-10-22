package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class DoubleDeserializer implements ObjectDeserializer {

    public final static DoubleDeserializer instance = new DoubleDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
    	Double value = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				value = parser.scanDouble();
    			}
    		}
    		else
    		{
    			value = parser.scanDouble();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

        return (T) value;
    }
}
