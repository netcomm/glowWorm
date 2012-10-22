package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import com.jd.glowworm.util.FieldInfo;

public class ArrayListStringFieldDeserializer extends FieldDeserializer {

    public ArrayListStringFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);
    }
    
    @Override
    public void parseField(PBDeserializer parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        ArrayList<Object> list = null;

        try
    	{
	    	byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
	    	
	    	if (tmpIsNull == 0)
	    	{
	    		list = new ArrayList<Object>();
	            ArrayListStringDeserializer.parseArray(parser, list);
	    	}
	    	
	    	if (object == null)
	        {
	            fieldValues.put(fieldInfo.getName(), list);
	        }
	    	else
	    	{
	            setValue(object, list);
	        }
    	}
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
    }
}
