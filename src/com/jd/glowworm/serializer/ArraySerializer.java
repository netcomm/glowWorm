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

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public class ArraySerializer implements ObjectSerializer {

    private final ObjectSerializer compObjectSerializer;

    public ArraySerializer(ObjectSerializer compObjectSerializer){
        super();
        this.compObjectSerializer = compObjectSerializer;
    }

    public final void write(PBSerializer paramPBSerializer, Object object,
    		Object fieldName, Type fieldType) throws IOException {
        SerializeWriter out = paramPBSerializer.getWriter();
        
        if (object == null) {
            return;
        }

        Object[] array = (Object[]) object;
        int size = array.length;
        
        try
        {
        	if (fieldName == null)
            {
            	out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.ARRAY);
            }
        	out.writeInt(size);
            for (int i = 0; i < size; i++)
            {
                Object item = array[i];

                if (item == null)
                {
                	out.getCodedOutputStream().writeRawByte(1);
                }
                else
                {
                	out.getCodedOutputStream().writeRawByte(0);
                    compObjectSerializer.write(paramPBSerializer, item, null, null);
                }
            }
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        }
    }
}
