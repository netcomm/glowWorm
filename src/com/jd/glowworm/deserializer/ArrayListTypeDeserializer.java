package com.jd.glowworm.deserializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class ArrayListTypeDeserializer implements ObjectDeserializer {

    private Type     itemType;
    private Class<?> rawClass;

    public ArrayListTypeDeserializer(Class<?> rawClass, Type itemType){
        this.rawClass = rawClass;
        this.itemType = itemType;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> T deserialze(PBDeserializer parser, Type type, Object fieldName) {
        Collection list = null;

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
        
        /*if (rawClass.isAssignableFrom(LinkedHashSet.class)) {
        	list = new LinkedHashSet();
        } else if (rawClass.isAssignableFrom(HashSet.class)) {
        	list = new HashSet();
        } else {
        	list = new ArrayList();
        }*/
        
        if (LinkedHashSet.class.isAssignableFrom(rawClass)) {
        	list = new LinkedHashSet();
        } else if (HashSet.class.isAssignableFrom(rawClass)) {
        	list = new HashSet();
        } else {
        	list = new ArrayList();
        }
        
        parser.parseArray(itemType, list, fieldName);

        return (T) list;
    }
}
