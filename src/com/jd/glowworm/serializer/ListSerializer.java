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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public final class ListSerializer implements ObjectSerializer {

    public static final ListSerializer instance = new ListSerializer();

    public final void write(PBSerializer serializer, Object object, Object fieldName, Type fieldType)
        throws IOException {
        SerializeWriter out = serializer.getWriter();
        
        Type elementType = null;
        
        /*if (object == null) {
            if (out.isEnabled(SerializerFeature.WriteNullListAsEmpty)) {
                out.write("[]");
            } else {
                out.writeNull();
            }
            return;
        }*/

        List<?> list = (List<?>) object;
        
        if (fieldName == null)
        {
        	if (fieldType == ArrayList.class)
        	{
        		out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.LIST_ARRAYLIST);
        	}
        	else if (fieldType == LinkedList.class)
        	{
        		out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.LIST_LINKEDLIST);
        	}
        	else
        	{
        		out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.Unknown);
        	}
        }
        
        final int size = list.size();
        out.writeInt(size);
                
        ObjectSerializer itemSerializer = null;
        try {
            for (int i = 0; i < size; i++) {
                Object item = list.get(i);

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
