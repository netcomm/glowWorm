package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

@SuppressWarnings("rawtypes")
public class EnumDeserializer implements ObjectDeserializer {

    private final Class<?>           enumClass;
    
    public EnumDeserializer(Class<?> enumClass){
        this.enumClass = enumClass;
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type type, Object fieldName)
    {
    	String tmpValue = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				tmpValue = parser.scanString();
    			}
    		}
    		else
    		{
    			tmpValue = parser.scanString();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	
    	if (tmpValue != null)
    	{
    		return (T) Enum.valueOf((Class<Enum>) enumClass, tmpValue);
    	}
    	else
    	{
    		return null;
    	}
    }
}
