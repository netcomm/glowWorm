package com.jd.glowworm.deserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.jd.glowworm.util.TypeUtils;

public class ArrayDeserializer implements ObjectDeserializer {

    public final static ArrayDeserializer instance = new ArrayDeserializer();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T deserialze(PBDeserializer parser, Type type, Object fieldName) {
        /*if (lexer.token() == JSONToken.LITERAL_STRING) {
            byte[] bytes = lexer.bytesValue();
            lexer.nextToken(JSONToken.COMMA);
            return (T) bytes;
        }*/
        
    	if (fieldName != null)
		{
    		try
    		{
				byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
				if (tmpIsNull == 0)
				{
					return getTheArray(parser, type, fieldName);
				}
    		}
    		catch(Exception ex)
    		{
    			ex.printStackTrace();
    		}
    		
			return null;
		}
		else
		{
	        return getTheArray(parser, type, fieldName);
		}
    }

    private <T> T getTheArray(PBDeserializer parser, Type type, Object fieldName)
    {
    	Class clazz = (Class) type;
        Class componentType = clazz.getComponentType();
        
        // 解析byte[]
        if (byte.class == componentType)
        {
        	try
        	{
        		return (T) parser.parseByteArray();
        	}
        	catch(Exception ex)
        	{
        		ex.printStackTrace();
        	}
        }
        else if (char.class == componentType) {
        	String value = StringDeserializer.deserialze(parser);
            return (T) value.toCharArray();
        }
        else if (boolean.class == componentType)
        {
        	try
        	{
        		byte[] tmpBytes = parser.parseByteArray();
        		boolean[] tmpRetBooleans = new boolean[tmpBytes.length];
        		for (int i= 0; i < tmpBytes.length; i++)
        		{
        			if (tmpBytes[i] == 1)
        			{
        				tmpRetBooleans[i] = true;
        			}
        			else
        			{
        				tmpRetBooleans[i] = false;
        			}
        		}
        		
        		return (T) tmpRetBooleans;
        	}
        	catch (Exception ex)
        	{
        		ex.printStackTrace();
        	}
        }
        
        ArrayList tmpList = new ArrayList();
        parser.parseArray(componentType, tmpList, fieldName);

        return (T) toObjectArray(parser, clazz, tmpList);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T toObjectArray(PBDeserializer parser, Class<T> clazz, List array) {
        if (array == null) {
            return null;
        }
        
        int size = array.size();

        Class<?> componentType = clazz.getComponentType();
        Object objArray = Array.newInstance(componentType, size);
        for (int i = 0; i < size; ++i) {
            Object value = array.get(i);

            if (componentType.isArray()) {
                Object element;
                if (componentType.isInstance(value)) {
                    element = value;
                } else {
                    element = toObjectArray(parser, componentType, (List) value);
                }
                
                Array.set(objArray, i, element);
            } else {
                Object element = TypeUtils.cast(value, componentType, parser);
                Array.set(objArray, i, element);
            }
        }
        
        return (T) objArray; // TODO
    }
}
