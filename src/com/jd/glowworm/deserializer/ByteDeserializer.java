package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

public class ByteDeserializer implements ObjectDeserializer {
    public final static ByteDeserializer instance = new ByteDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser, fieldName);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T deserialze(PBDeserializer parser, Object fieldName) {
    	Byte value = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				value = parser.scanByte();
    			}
    		}
    		else
    		{
    			value = parser.scanByte();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}

        return (T) value;
    }
}
