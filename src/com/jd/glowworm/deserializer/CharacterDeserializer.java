package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;

import com.jd.glowworm.util.TypeUtils;

public class CharacterDeserializer implements ObjectDeserializer {
    public final static CharacterDeserializer instance = new CharacterDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        Object value = null;
        
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
        
        return (T) TypeUtils.castToChar(value);
    }
}
