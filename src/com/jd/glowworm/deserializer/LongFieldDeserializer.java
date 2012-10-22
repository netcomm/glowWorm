package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;
import java.util.Map;

import com.jd.glowworm.util.FieldInfo;

public class LongFieldDeserializer extends FieldDeserializer {

    private final ObjectDeserializer fieldValueDeserilizer;

    public LongFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);

        fieldValueDeserilizer = mapping.getDeserializer(fieldInfo);
    }

    @Override
    public void parseField(PBDeserializer parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        Long value = null;
        
        try
        {
        	value = parser.scanFieldLong();
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
        
        if (value == null && getFieldClass() == long.class) {
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
