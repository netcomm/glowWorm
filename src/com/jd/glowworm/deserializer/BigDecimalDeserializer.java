package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;
import com.jd.glowworm.util.TypeUtils;

public class BigDecimalDeserializer implements ObjectDeserializer {

    public final static BigDecimalDeserializer instance = new BigDecimalDeserializer();

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser, fieldName);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialze(PBDeserializer parser, Object fieldName)
    {
        String tmpValueStr = null;
        Object value = null;
        
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 0)
    			{
    				tmpValueStr = parser.scanString();
    			}
    		}
    		else
    		{
    			tmpValueStr = parser.scanString();
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    	
    	if (tmpValueStr != null)
    	{
    		value = TypeUtils.castToBigDecimal(tmpValueStr);
    	}
    	
        return (T) value;
    }
}
