package com.jd.glowworm;

import org.xerial.snappy.Snappy;
import com.jd.glowworm.deserializer.ASMJavaBeanDeserializer;
import com.jd.glowworm.deserializer.JavaBeanDeserializer;
import com.jd.glowworm.deserializer.ObjectDeserializer;
import com.jd.glowworm.deserializer.PBDeserializer;
import com.jd.glowworm.serializer.PBSerializer;
import com.jd.glowworm.serializer.SerializeWriter;
import com.jd.glowworm.util.TypeUtils;

public class PB {
	public static byte[] toPBBytes(Object objectParm)
	{
		SerializeWriter tmpSerializeWriter = new SerializeWriter();
		PBSerializer tmpPBSerializer = new PBSerializer(tmpSerializeWriter);
		
		// 对象类型字符串
		Class<?> clazz = objectParm.getClass();
		tmpSerializeWriter.getCodedOutputStream().writeString(clazz.getName());
		
		tmpPBSerializer.write(objectParm);
		return tmpSerializeWriter.getCodedOutputStream().getBytes();
	}
	
	public static byte[] toPBBytes_Compress(Object objectParm)
	{
		byte[] tmpRowBytes = toPBBytes(objectParm);
		byte[] tmpCompressBytes = null;
		
		try
		{
			tmpCompressBytes = Snappy.compress(tmpRowBytes);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return tmpCompressBytes;
	}
	
	public static Object parsePBBytes_Compress(byte[] pbCompressBytesParm, Class<?> fieldClass)
	{
		Object retObj = null;
		
		try
		{
			byte[] tmpRowBytes = Snappy.uncompress(pbCompressBytesParm);
			retObj = parsePBBytes(tmpRowBytes, fieldClass);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return retObj;
	}
	
	public static Object parsePBBytes(byte[] pbBytesParm, Class<?> fieldClass)
	{
		PBDeserializer tmpPBDeserializer = new PBDeserializer(pbBytesParm);
		
		try
		{
			String tmpClassName = tmpPBDeserializer.scanString();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return deserializeObj(tmpPBDeserializer, fieldClass);
	}
	
	public static Object parsePBBytes_Compress(byte[] pbCompressBytesParm)
	{
		Object retObj = null;
		
		try
		{
			byte[] tmpRowBytes = Snappy.uncompress(pbCompressBytesParm);
			retObj = parsePBBytes(tmpRowBytes);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return retObj;
	}
	
	public static Object parsePBBytes(byte[] pbBytesParm)
	{
		PBDeserializer tmpPBDeserializer = new PBDeserializer(pbBytesParm);
		
		Class fieldClass = null;
		try
		{
			String tmpClassName = tmpPBDeserializer.scanString();
			fieldClass = TypeUtils.loadClass(tmpClassName);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return deserializeObj(tmpPBDeserializer, fieldClass);
	}
	
	private static Object deserializeObj(PBDeserializer pBDeserializerParm,
			Class<?> fieldClass)
	{
		ObjectDeserializer tmpObjectDeserializer = pBDeserializerParm.getDeserializer(fieldClass);
		
		Class tmpClass = tmpObjectDeserializer.getClass();
		if (ASMJavaBeanDeserializer.class.isAssignableFrom(tmpClass) ||
				JavaBeanDeserializer.class.isAssignableFrom(tmpClass))
		{
			
		}
		else
		{
			try
			{
				byte tmpType = pBDeserializerParm.scanByte();
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		Object retObj = tmpObjectDeserializer.deserialze(pBDeserializerParm, fieldClass, null);
		
		return retObj;
	}
}
