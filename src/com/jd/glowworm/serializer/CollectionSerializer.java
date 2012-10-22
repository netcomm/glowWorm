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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public class CollectionSerializer implements ObjectSerializer {

    public final static CollectionSerializer instance = new CollectionSerializer();

    public void write(PBSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
        SerializeWriter out = serializer.getWriter();
        Collection<?> collection = (Collection<?>) object;
        
        if (fieldName == null)
        {
        	if (fieldType == HashSet.class)
        	{
        		out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.COLLECTION_HASHSET);
        	}
        	else if (fieldType == TreeSet.class)
        	{
        		out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.COLLECTION_TREESET);
        	}
        	else
        	{
        		out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.Unknown);
        	}
        }
        
        final int size = collection.size();
        out.writeInt(size);
                
        ObjectSerializer itemSerializer = null;
        try
        {
        	for (Object item : collection)
        	{
                if (item == null)
                {
                	serializer.getWriter().getCodedOutputStream().writeRawByte(1);
                }
                else
                {
	                serializer.getWriter().getCodedOutputStream().writeRawByte(0);
	                Class tmpClass = item.getClass();
	                itemSerializer = serializer.getObjectWriter(tmpClass);
	                
	                if (itemSerializer.getClass().getName().startsWith(ASMSerializerFactory.GenClassName_prefix))
	                {
	                	out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.OBJECT);
	                	out.getCodedOutputStream().writeString(tmpClass.getName());
	                }
	                
	                itemSerializer.write(serializer, item, null, tmpClass);
                }
            }
        } finally {
            
        }
    }

}
