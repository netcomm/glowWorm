package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class ShortDeserializer implements ObjectDeserializer {
    public final static ShortDeserializer instance = new ShortDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser, fieldName);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(PBDeserializer parser, Object fieldName) {
    	Short value = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				value = (short)parser.scanInt();
    			}
    		}
    		else
    		{
    			value = (short)parser.scanInt();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

        return (T) value;
    }
}
