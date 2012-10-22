package com.jd.glowworm.deserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.jd.glowworm.util.FieldInfo;

public class ArrayListTypeFieldDeserializer extends FieldDeserializer {

    private final Type         itemType;
    private int                itemFastMatchToken;
    private ObjectDeserializer deserializer;

    public ArrayListTypeFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);

        Type fieldType = getFieldType();
        if (fieldType instanceof ParameterizedType) {
            this.itemType = ((ParameterizedType) getFieldType()).getActualTypeArguments()[0];
        } else {
            this.itemType = Object.class;
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void parseField(PBDeserializer parser, Object object, Type objectType, Map<String, Object> fieldValues) {
    	try
    	{
	    	byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
	    	
	    	if (tmpIsNull == 1)
	    	{
	            setValue(object, null);
	            return;
        	}
	    	
	        ArrayList list = new ArrayList();
	
	        ParseContext context = parser.getContext();
	
	        parser.setContext(context, object, fieldInfo.getName());
	        parseArray(parser, objectType, list);
	        parser.setContext(context);
	
	        if (object == null) {
	            fieldValues.put(fieldInfo.getName(), list);
	        } else {
	            setValue(object, list);
	        }
    	}
    	catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final void parseArray(PBDeserializer parser, Type objectType, Collection array) {
        Type itemType = this.itemType;;
        
        if (itemType instanceof TypeVariable && objectType instanceof ParameterizedType) {
            TypeVariable typeVar = (TypeVariable) itemType;
            ParameterizedType paramType = (ParameterizedType) objectType;

            Class<?> objectClass = null;
            if (paramType.getRawType() instanceof Class) {
                objectClass = (Class<?>) paramType.getRawType();
            }

            int paramIndex = -1;
            if (objectClass != null) {
                for (int i = 0, size = objectClass.getTypeParameters().length; i < size; ++i) {
                    TypeVariable item = objectClass.getTypeParameters()[i];
                    if (item.getName().equals(typeVar.getName())) {
                        paramIndex = i;
                        break;
                    }
                }
            }

            if (paramIndex != -1) {
                itemType = paramType.getActualTypeArguments()[paramIndex];
            }
        }
        
        if (deserializer == null) {
            deserializer = parser.getDeserializer(itemType);
        }
        
        try
        {
	        //int tmpTag = parser.getTheCodedInputStream().readTag();
			int tmpListItemCnt = parser.getTheCodedInputStream().readInt32();
	        
	        for (int i = 0; i < tmpListItemCnt; i++) {
	        	byte tmpIsNull = parser.getTheCodedInputStream().readRawByte();
	        	if (tmpIsNull == 0)
	        	{
	        		if (deserializer instanceof JavaObjectDeserializer)
	        		{
	        		}
	        		else
	        		{
	        			byte tmpType = parser.getTheCodedInputStream().readRawByte();
	        			if (com.jd.glowworm.asm.Type.OBJECT == tmpType)
	        			{
		        		    if (deserializer.getClass().getName().startsWith(ASMDeserializerFactory.DeserializerClassName_prefix))
		        		    {
		        		    	parser.getTheCodedInputStream().readString();
		        		    }
	        			}
	        		}
	        		Object val = deserializer.deserialze(parser, itemType, null);
	        		array.add(val);
	        	}
	        	else
	        	{
	        		array.add(null);
	        	}
	        }
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
    }

}
