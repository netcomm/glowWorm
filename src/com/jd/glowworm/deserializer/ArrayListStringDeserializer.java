package com.jd.glowworm.deserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ArrayListStringDeserializer implements ObjectDeserializer {

    public final static ArrayListStringDeserializer instance = new ArrayListStringDeserializer();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T deserialze(PBDeserializer parser, Type type, Object fieldName) {
    	Collection array = null;
    	try
    	{
    		if (fieldName != null)
    		{
    			byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
    			if (tmpIsNull == 1)
    			{
    				return null;
    			}
    		}
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
        
        if (type == Set.class || type == HashSet.class) {
            array = new HashSet();
        } else {
            if (type instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) type).getRawType();
                if (rawType == Set.class || rawType == HashSet.class) {
                    array = new HashSet();
                }
            }
        }

        if (array == null) {
            array = new ArrayList();
        }

        parseArray(parser, array);

        return (T) array;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void parseArray(PBDeserializer parser, Collection array) {
    	try
    	{
	    	int tmpListItemCnt = parser.getTheCodedInputStream().readInt32();
	        
	        for (int i = 0; i < tmpListItemCnt; i++)
	        {
	        	byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
	        	if (tmpIsNull == 0)
	        	{
	        		byte tmpType = parser.getTheCodedInputStream().readRawByte();
	        		Object val = parser.scanString();
	        		array.add(val);
	        	}
	        	else
	        	{
	        		array.add(null);
	        	}
	        }
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    	}
    }
}
