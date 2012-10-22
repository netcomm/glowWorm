package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;
import java.util.Map;

import com.jd.glowworm.util.FieldInfo;

public class DefaultFieldDeserializer extends FieldDeserializer {

    private ObjectDeserializer fieldValueDeserilizer;

    public DefaultFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);
    }

    @Override
    public void parseField(PBDeserializer parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        if (fieldValueDeserilizer == null) {
            fieldValueDeserilizer = parser.getDeserializer(fieldInfo);
        }

        try
        {
	        byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
	        if (0 == tmpIsNull)
	        {
		        Object value = fieldValueDeserilizer.deserialze(parser, getFieldType(), null);
		        if (object == null) {
		            fieldValues.put(fieldInfo.getName(), value);
		        } else {
		            setValue(object, value);
		        }
	        }
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
        
    }
}
