package com.jd.glowworm.serializer;

import java.io.IOException;
import java.lang.reflect.Type;


public class CharArraySerializer implements ObjectSerializer {

    public static CharArraySerializer instance = new CharArraySerializer();

    public final void write(PBSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
        SerializeWriter out = serializer.getWriter();
        
        if (fieldName == null)
        {
        	out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.ARRAY_CHAR);
        }
        char[] chars = (char[]) object;
        out.writeString(new String(chars));
    }

}
