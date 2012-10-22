package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class BooleanDeserializer implements ObjectDeserializer {
    public final static BooleanDeserializer instance = new BooleanDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser, fieldName);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(PBDeserializer parser, Object fieldName) {
    	Boolean value = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				value = parser.scanBool();
    			}
    		}
    		else
    		{
    			value = parser.scanBool();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

        return (T) value;
    }
}
