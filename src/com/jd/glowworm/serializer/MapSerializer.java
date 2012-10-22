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
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public class MapSerializer implements ObjectSerializer {

    public static MapSerializer instance = new MapSerializer();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void write(PBSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
    	SerializeWriter out = serializer.getWriter();
    	
    	if (fieldName == null)
        {
    		if (object instanceof ConcurrentHashMap)
    		{
    			out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.MAP_ConcurrentHashMap);
    		}
    		else if (object instanceof LinkedHashMap)
    		{
    			out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.MAP_LinkedHash);
    		}
    		else if (object instanceof HashMap)
    		{
    			out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.MAP_HASH);
    		}
    		else
    		{
    			out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.Unknown);
    		}
        }
    	
        Map<?, ?> map = (Map<?, ?>) object;
        
        try
        {
        	int tmpMapSize = map.size();
        	out.writeInt(tmpMapSize);
        	
            Class<?> preClazz = null;
            ObjectSerializer preWriter = null;
            
            for (Map.Entry entry : map.entrySet())
            {
            	Object entryKey = entry.getKey();
                Object value = entry.getValue();
                
                //if (entryKey == null || entryKey instanceof String)
                if (entryKey instanceof String)
                {
                    String key = (String) entryKey;
                    StringSerializer.instance.write(serializer, key, null, null);
                }
                else
                {
                    serializer.write(entryKey);
                }
                
                if (value == null) {
                    out.writeNull();
                    continue;
                }
                
                Class<?> clazz = value.getClass();

                if (clazz == preClazz) {
                    preWriter.write(serializer, value, null, null);
                } else {
                    preClazz = clazz;
                    preWriter = serializer.getObjectWriter(clazz);
                    String tmpClassName = preWriter.getClass().getName();
                    if (tmpClassName.startsWith(ASMSerializerFactory.GenClassName_prefix))
                    {
                    	out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.OBJECT);
                		out.writeString(clazz.getName());
                    }

                    preWriter.write(serializer, value, null, clazz);
                }
            }
        }
        catch (Exception ex)
        {
        	ex.printStackTrace();
        }
    }
}
