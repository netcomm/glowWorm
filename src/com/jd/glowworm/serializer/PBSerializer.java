package com.jd.glowworm.serializer;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.jd.glowworm.PBException;
import com.jd.glowworm.util.ASMClassLoader;
import com.jd.glowworm.util.IdentityHashMap;

public class PBSerializer {
	private SerializeWriter out;
	private ASMSerializerFactory theASMSerializerFactory = new ASMSerializerFactory();
	private static IdentityHashMap theSerializerHMap = new IdentityHashMap();
	
	public PBSerializer(SerializeWriter outParm)
	{
		out = outParm;
		theSerializerHMap.put(Boolean.class, BooleanSerializer.instance);
		theSerializerHMap.put(Byte.class, ByteSerializer.instance);
		theSerializerHMap.put(Short.class, ShortSerializer.instance);
		theSerializerHMap.put(Character.class, CharacterSerializer.instance);
		theSerializerHMap.put(Integer.class, IntegerSerializer.instance);
		theSerializerHMap.put(Long.class, LongSerializer.instance);
		theSerializerHMap.put(Float.class, FloatSerializer.instance);
		theSerializerHMap.put(Double.class, DoubleSerializer.instance);
		theSerializerHMap.put(BigDecimal.class, BigDecimalSerializer.instance);
		theSerializerHMap.put(String.class, StringSerializer.instance);
		theSerializerHMap.put(Class.class, ClassSerializer.instance);
		theSerializerHMap.put(byte[].class, ByteArraySerializer.instance);
		theSerializerHMap.put(short[].class, ShortArraySerializer.instance);
		theSerializerHMap.put(int[].class, IntArraySerializer.instance);
		theSerializerHMap.put(long[].class, LongArraySerializer.instance);
		theSerializerHMap.put(float[].class, FloatArraySerializer.instance);
		theSerializerHMap.put(double[].class, DoubleArraySerializer.instance);
		theSerializerHMap.put(boolean[].class, BooleanArraySerializer.instance);
		theSerializerHMap.put(char[].class, CharArraySerializer.instance);
		theSerializerHMap.put(Object[].class, ObjectArraySerializer.instance);
	}
	
	public SerializeWriter getWriter()
	{
		return out;
	}
	
	public void write(Object object) {
        Class<?> clazz = object.getClass();
        ObjectSerializer writer = getObjectWriter(clazz);

        try {
            writer.write(this, object, null, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public ObjectSerializer getObjectWriter(Class<?> clazz) {
        ObjectSerializer writer = (ObjectSerializer)theSerializerHMap.get(clazz);

        if (writer == null) {
        	if (Map.class.isAssignableFrom(clazz)) {
            	theSerializerHMap.put(clazz, MapSerializer.instance);
            } else if (List.class.isAssignableFrom(clazz)) {
            	theSerializerHMap.put(clazz, ListSerializer.instance);
            } else if (Collection.class.isAssignableFrom(clazz)) {
            	theSerializerHMap.put(clazz, CollectionSerializer.instance);
            } else if (clazz.isEnum() || (clazz.getSuperclass() != null && clazz.getSuperclass().isEnum())) {
            	theSerializerHMap.put(clazz, EnumSerializer.instance);
            } else if (clazz.isArray()) {
                Class<?> componentType = clazz.getComponentType();
                ObjectSerializer compObjectSerializer = getObjectWriter(componentType);
                theSerializerHMap.put(clazz, new ArraySerializer(compObjectSerializer));
            } else {
            	try
            	{
            		theSerializerHMap.put(clazz, createJavaBeanSerializer(clazz));
            	}
            	catch(Exception ex)
            	{
            		ex.printStackTrace();
            	}
            }

            writer = (ObjectSerializer)theSerializerHMap.get(clazz);
        }
        return writer;
    }
	
	private ObjectSerializer createJavaBeanSerializer(Class<?> clazz) {
        if (!Modifier.isPublic(clazz.getModifiers())) {
            return new JavaBeanSerializer(clazz);
        }

        boolean asm = true;

        if (asm && ASMClassLoader.isExternalClass(clazz) || clazz == Serializable.class || clazz == Object.class) {
            asm = false;
        }
        
        if (asm) {
            try {
                return theASMSerializerFactory.createJavaBeanSerializer(clazz);
            } catch (Throwable e) {
                throw new PBException("create asm serializer error, class " + clazz, e);
            }
        }

        return new JavaBeanSerializer(clazz);
    }
	
	public final void writeWithFieldName(Object object, Object fieldName, Type fieldType)
	{
		try {
			if (object == null)
			{
				out.getCodedOutputStream().writeRawByte(1);
			}
			else
			{
				out.getCodedOutputStream().writeRawByte(0);
	            Class<?> clazz = object.getClass();
	            ObjectSerializer writer = getObjectWriter(clazz);
	            writer.write(this, object, fieldName, fieldType);
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public final void writeArrayItem(Object object, Object fieldName, Type fieldType)
	{
		try {
			if (object == null)
			{
				out.getCodedOutputStream().writeRawByte(1);
			}
			else
			{
	            Class<?> clazz = object.getClass();
	            out.getCodedOutputStream().writeRawByte(0);
	            
	            ObjectSerializer writer = getObjectWriter(clazz);
	            String tmpClassName = writer.getClass().getName();
	            //System.out.println("tmpClassName "+tmpClassName);
                if (tmpClassName.startsWith(ASMSerializerFactory.GenClassName_prefix))
                {
                	out.getCodedOutputStream().writeRawByte(com.jd.glowworm.asm.Type.OBJECT);
                	out.getCodedOutputStream().writeString(clazz.getName());
                }
	            
	            //writer.write(this, object, null, fieldType);
                writer.write(this, object, null, clazz);
			}
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}
