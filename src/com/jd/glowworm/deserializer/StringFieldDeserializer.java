package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;
import java.util.Map;

import com.jd.glowworm.util.FieldInfo;

public class StringFieldDeserializer extends FieldDeserializer {
    public StringFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);
    }

    @Override
    public void parseField(PBDeserializer parser, Object object, Type objectType, Map<String, Object> fieldValues) {
    	String value = null;
    	try
        {
        	value = parser.scanFieldString();
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }

        /*final JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.LITERAL_STRING) {
            value = lexer.stringVal();
            lexer.nextToken(JSONToken.COMMA);
        } else {

            Object obj = parser.parse();

            if (obj == null) {
                value = null;
            } else {
                value = obj.toString();
            }
        }*/

        if (object == null) {
            fieldValues.put(fieldInfo.getName(), value);
        } else {
            setValue(object, value);
        }
    }
}
