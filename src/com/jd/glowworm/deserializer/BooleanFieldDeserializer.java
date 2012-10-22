package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;
import java.util.Map;

import com.jd.glowworm.util.FieldInfo;

public class BooleanFieldDeserializer extends FieldDeserializer {

    public BooleanFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);
    }

    @Override
    public void parseField(PBDeserializer parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        Boolean value = null;
        
        try
        {
        	value = parser.scanFieldBooleanObj();
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
        
        if (value == null && getFieldClass() == boolean.class) {
            // skip
            return;
        }
        
        if (object == null) {
            fieldValues.put(fieldInfo.getName(), value);
        } else {
            setValue(object, value);
        }
    }
}
