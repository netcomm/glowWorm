/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.glowworm.serializer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jd.glowworm.PBException;
import com.jd.glowworm.util.FieldInfo;
import com.jd.glowworm.util.TypeUtils;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public class JavaBeanSerializer implements ObjectSerializer {

    // serializers
    private final FieldSerializer[] sortedGetters;
    
    public JavaBeanSerializer(Class<?> clazz){
        this(clazz, (Map<String, String>) null);
    }

    public JavaBeanSerializer(Class<?> clazz, String... aliasList){
        this(clazz, createAliasMap(aliasList));
    }

    static Map<String, String> createAliasMap(String... aliasList) {
        Map<String, String> aliasMap = new HashMap<String, String>();
        for (String alias : aliasList) {
            aliasMap.put(alias, alias);
        }

        return aliasMap;
    }

    public JavaBeanSerializer(Class<?> clazz, Map<String, String> aliasMap) {
        {
        	List<FieldSerializer> getterList = new ArrayList<FieldSerializer>();
	        List<FieldInfo> fieldInfoList = TypeUtils.computeGetters(clazz, aliasMap, true);
	
	        for (FieldInfo fieldInfo : fieldInfoList) {
	            getterList.add(createFieldSerializer(fieldInfo));
	        }
	
	        sortedGetters = getterList.toArray(new FieldSerializer[getterList.size()]);
        }
    }
    
    public void write(PBSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
        SerializeWriter out = serializer.getWriter();

/*        if (object == null) {
            out.writeNull();
            return;
        }

        if (serializer.containsReference(object)) {
            writeReference(serializer, object);
            return;
        }*/

        final FieldSerializer[] getters = this.sortedGetters;
        
        try {
        	if (fieldName == null)
        	{
        		out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.OBJECT);
        		out.writeString(object.getClass().getName());
        	}
        		/*out.getCodedOutputStream().writeRawByte(com.cn.jd.glowworm.asm.Type.OBJECT);
        		out.writeString(object.getClass().getName());*/
                
	            for (int i = 0; i < getters.length; ++i) {
	                FieldSerializer fieldSerializer = getters[i];
	                Field field = fieldSerializer.getField();
	                if (field != null) {
	                    if (Modifier.isTransient(field.getModifiers())) {
	                        continue;
	                    }
	                }
	
	                Object propertyValue = fieldSerializer.getPropertyValue(object);
	                fieldSerializer.writeProperty(serializer, propertyValue, i);
	            }
        } catch (Exception e) {
            throw new PBException("write javaBean error", e);
        }
    }
    
    public FieldSerializer createFieldSerializer(FieldInfo fieldInfo) {
        Class<?> clazz = fieldInfo.getFieldClass();

        if (clazz == Number.class) {
            return new NumberFieldSerializer(fieldInfo);
        }

        return new ObjectFieldSerializer(fieldInfo);
    }
}
