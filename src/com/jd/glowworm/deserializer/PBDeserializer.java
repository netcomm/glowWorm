package com.jd.glowworm.deserializer;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.jd.glowworm.PBException;
import com.jd.glowworm.asm.ASMException;
import com.jd.glowworm.util.ASMClassLoader;
import com.jd.glowworm.util.BufferInputStream;
import com.jd.glowworm.util.CodedInputStream;
import com.jd.glowworm.util.DeserializeBeanInfo;
import com.jd.glowworm.util.FieldInfo;
import com.jd.glowworm.util.IdentityHashMap;
import com.jd.glowworm.util.TypeUtils;

public class PBDeserializer {
	public static PBDeserializer getGlobalInstance() {
        return global;
    }
	
	private static PBDeserializer global = new PBDeserializer();
	private CodedInputStream theCodedInputStream;
	private ParseContext context;
	private ParseContext[]             contextArray      = new ParseContext[8];
	private int                        contextArrayIndex = 0;
	private static IdentityHashMap<Type, ObjectDeserializer> derializers       = new IdentityHashMap<Type, ObjectDeserializer>();
	
	static{
        derializers.put(Map.class, MapDeserializer.instance);
        derializers.put(HashMap.class, MapDeserializer.instance);
        derializers.put(LinkedHashMap.class, MapDeserializer.instance);
        derializers.put(TreeMap.class, MapDeserializer.instance);
        derializers.put(ConcurrentMap.class, MapDeserializer.instance);
        derializers.put(ConcurrentHashMap.class, MapDeserializer.instance);

        derializers.put(Collection.class, CollectionDeserializer.instance);
        derializers.put(List.class, CollectionDeserializer.instance);
        derializers.put(ArrayList.class, CollectionDeserializer.instance);

        derializers.put(Object.class, JavaObjectDeserializer.instance);
        derializers.put(String.class, StringDeserializer.instance);
        
        derializers.put(char.class, CharacterDeserializer.instance);
        derializers.put(Character.class, CharacterDeserializer.instance);
        
        derializers.put(byte.class, ByteDeserializer.instance);
        derializers.put(Byte.class, ByteDeserializer.instance);
        derializers.put(short.class, ShortDeserializer.instance);
        derializers.put(Short.class, ShortDeserializer.instance);
        derializers.put(int.class, IntegerDeserializer.instance);
        derializers.put(Integer.class, IntegerDeserializer.instance);
        derializers.put(float.class, FloatDeserializer.instance);
        derializers.put(Float.class, FloatDeserializer.instance);
        derializers.put(double.class, DoubleDeserializer.instance);
        derializers.put(Double.class, DoubleDeserializer.instance);
        derializers.put(long.class, LongDeserializer.instance);
        derializers.put(Long.class, LongDeserializer.instance);
        derializers.put(boolean.class, BooleanDeserializer.instance);
        derializers.put(Boolean.class, BooleanDeserializer.instance);
        derializers.put(BigDecimal.class, BigDecimalDeserializer.instance);
/*      derializers.put(BigInteger.class, BigIntegerDeserializer.instance);
        derializers.put(Class.class, ClassDerializer.instance);
        derializers.put(char[].class, CharArrayDeserializer.instance);

        derializers.put(UUID.class, UUIDDeserializer.instance);
        derializers.put(TimeZone.class, TimeZoneDeserializer.instance);
        derializers.put(Locale.class, LocaleDeserializer.instance);
        derializers.put(InetAddress.class, InetAddressDeserializer.instance);
        derializers.put(Inet4Address.class, InetAddressDeserializer.instance);
        derializers.put(Inet6Address.class, InetAddressDeserializer.instance);
        derializers.put(InetSocketAddress.class, InetSocketAddressDeserializer.instance);
        derializers.put(File.class, FileDeserializer.instance);
        derializers.put(URI.class, URIDeserializer.instance);
        derializers.put(URL.class, URLDeserializer.instance);
        derializers.put(Pattern.class, PatternDeserializer.instance);
        derializers.put(Charset.class, CharsetDeserializer.instance);
        derializers.put(Number.class, NumberDeserializer.instance);
        derializers.put(AtomicIntegerArray.class, AtomicIntegerArrayDeserializer.instance);
        derializers.put(AtomicLongArray.class, AtomicLongArrayDeserializer.instance);
        derializers.put(StackTraceElement.class, StackTraceElementDeserializer.instance);

        derializers.put(Serializable.class, defaultSerializer);
        derializers.put(Cloneable.class, defaultSerializer);
        derializers.put(Comparable.class, defaultSerializer);
        derializers.put(Closeable.class, defaultSerializer);*/
	}
	
	public PBDeserializer()
	{
		
	}
	
	public PBDeserializer(byte[] thePBBytesParm)
	{
		theCodedInputStream = new CodedInputStream(new BufferInputStream(thePBBytesParm));
	}
	
	public String scanFieldString()
	{
		String retStr = null;
		/*int tmpTag = theCodedInputStream.readTag();
		WireFormat.getTagWireType(tmpTag);*/
		try
		{
			byte tmpIsNull = theCodedInputStream.readRawByte();
			if (tmpIsNull == 0)
			{
				retStr = theCodedInputStream.readString();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return retStr;
	}
	
	public boolean scanFieldBoolean() throws IOException
	{
		boolean ret;
		byte tmpIsNull = theCodedInputStream.readRawByte();
		ret = theCodedInputStream.readBool();
		
		return ret;
	}
	
	public Boolean scanFieldBooleanObj() throws IOException
	{
		Boolean ret = null;
		/*int tmpTag = theCodedInputStream.readTag();
		WireFormat.getTagWireType(tmpTag);*/
		byte tmpIsNull = theCodedInputStream.readRawByte();
		if (0 == tmpIsNull)
		{
			ret = theCodedInputStream.readBool();
		}
		
		return ret;
	}
	
	public double scanFieldDouble() throws IOException
	{
		Double ret = null;
		byte tmpIsNull = theCodedInputStream.readRawByte();
		ret = theCodedInputStream.readDouble_my();
		
		return ret;
	}
	
	public int scanFieldInt() throws IOException
	{
		int ret;
		/*int tmpTag = theCodedInputStream.readTag();
		WireFormat.getTagWireType(tmpTag);*/
		byte tmpIsNull = theCodedInputStream.readRawByte();
		ret = theCodedInputStream.readUInt32();
		
		return ret;
	}
	
	public float scanFieldFloat() throws IOException
	{
		float ret;
		byte tmpIsNull = theCodedInputStream.readRawByte();
		ret = theCodedInputStream.readFloat();
		
		return ret;
	}
	
	public Integer scanFieldInteger() throws IOException
	{
		Integer ret = null;
		/*int tmpTag = theCodedInputStream.readTag();
		WireFormat.getTagWireType(tmpTag);*/
		byte tmpIsNull = theCodedInputStream.readRawByte();
		if (0 == tmpIsNull)
		{
			ret = theCodedInputStream.readUInt32();
		}
		
		return ret;
	}
	
	public Long scanFieldLong() throws IOException
	{
		Long ret = null;
		/*int tmpTag = theCodedInputStream.readTag();
		WireFormat.getTagWireType(tmpTag);*/
		byte tmpIsNull = theCodedInputStream.readRawByte();
		if (0 == tmpIsNull)
		{
			ret = theCodedInputStream.readRawVarint64();
		}
		
		return ret;
	}
	
	public byte[] scanFieldByteArray() throws IOException
	{
		byte[] ret = null;
		
		byte tmpIsNull = theCodedInputStream.readRawByte();
		if (0 == tmpIsNull)
		{
			ret = scanByteArray();
		}
		
		return ret;
	}
	
	public long scanFieldlong() throws IOException
	{
		long ret = 0;
		/*int tmpTag = theCodedInputStream.readTag();
		WireFormat.getTagWireType(tmpTag);*/
		byte tmpIsNull = theCodedInputStream.readRawByte();
		if (0 == tmpIsNull)
		{
			ret = theCodedInputStream.readRawVarint64();
		}
		
		return ret;
	}
	
	public byte scanByte() throws IOException
	{
		return theCodedInputStream.readRawByte();
	}
	
	public String scanString() throws IOException
	{
		String retStr = null;
		retStr = theCodedInputStream.readString();
		return retStr;
	}
	
	public int scanInt() throws IOException
	{
		int retInt;
		retInt = theCodedInputStream.readInt32();
		return retInt;
	}
	
	public double scanDouble() throws IOException
	{
		double ret;
		ret = theCodedInputStream.readDouble_my();
		return ret;
	}
	
	public long scanLong() throws IOException
	{
		long ret;
		ret = theCodedInputStream.readRawVarint64();
		return ret;
	}
	
	public boolean scanBool() throws IOException
	{
		boolean ret;
		ret = theCodedInputStream.readBool();
		return ret;
	}
	
	public BigDecimal scanBigDecimal() throws IOException
	{
		String tmpStrVal = theCodedInputStream.readString();
		return new BigDecimal(tmpStrVal);
	}
	
	public Object scanEnum() throws IOException
	{
		String typeName = scanString();
        Class<?> clazz = TypeUtils.loadClass(typeName);
        String tmpValue = scanString();
        return Enum.valueOf((Class<Enum>)clazz, tmpValue);
	}
	
	public float scanFloat() throws IOException
	{
		float ret;
		ret = theCodedInputStream.readFloat();
		return ret;
	}
	
	public final Object parseObject(final Map object, Object fieldName, int mapItemSizeParm)
	{
        ParseContext context = this.getContext();
        try
        {
            boolean setContextFlag = false;
            for (int i = 0; i < mapItemSizeParm; i++)
            {
                Object key;
                key = parse(null);
                
                if (!setContextFlag) {
                    setContext(object, fieldName);
                    setContextFlag = true;
                }

                Object value = parse(null);
                object.put(key, value);
            }
            
            return object;
        }
        finally
        {
            this.setContext(context);
        }
    }
	
	public Collection scanList(Collection theCollectionParm, ObjectDeserializer listItemDeserParm,
			Type typeParm)
	{
		Collection theCollection = null;
		try
		{
			//int tmpTag = theCodedInputStream.readTag();
			byte tmpIsNull = theCodedInputStream.readRawByte();
			if (tmpIsNull == 0)
			{
				theCollection = theCollectionParm;
				int tmpListItemCnt = theCodedInputStream.readInt32();
				for (int i = 0; i < tmpListItemCnt; i++)
				{
					tmpIsNull = theCodedInputStream.readRawByte();
					if (tmpIsNull == 0)
					{
						byte tmpType = theCodedInputStream.readRawByte();
						// 如果是OBJECT或者ENUM,则需要过滤掉类名
						if (tmpType == com.jd.glowworm.asm.Type.OBJECT ||
								tmpType == com.jd.glowworm.asm.Type.ENUM)
						{
							String tmpClassName = theCodedInputStream.readString();
						}
						theCollection.add(
							listItemDeserParm.deserialze(this, typeParm, null));
					}
					else
					{
						theCollection.add(null);
					}
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return theCollection;
	}
	
	public ObjectDeserializer getDeserializer(FieldInfo fieldInfo) {
        return getDeserializer(fieldInfo.getFieldClass(), fieldInfo.getFieldType());
    }
	
	public ObjectDeserializer getDeserializer(Type type) {
        ObjectDeserializer derializer = this.derializers.get(type);
        if (derializer != null) {
            return derializer;
        }

        if (type instanceof Class<?>) {
            return getDeserializer((Class<?>) type, type);
        }

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class<?>) {
                return getDeserializer((Class<?>) rawType, type);
            } else {
                return getDeserializer(rawType);
            }
        }

        return null;
    }
	
	public ObjectDeserializer getDeserializer(Class<?> clazz, Type type) {
        ObjectDeserializer derializer = derializers.get(type);
        if (derializer != null) {
            return derializer;
        }

        if (type == null) {
            type = clazz;
        }

        derializer = derializers.get(type);
        if (derializer != null) {
            return derializer;
        }

        if (type instanceof WildcardType || type instanceof TypeVariable) {
            derializer = derializers.get(clazz);
        }
        
        if (derializer != null) {
            return derializer;
        }
        
        derializer = derializers.get(type);
        if (derializer != null) {
            return derializer;
        }
        
        if (clazz.isEnum()) {
            derializer = new EnumDeserializer(clazz);
        } else if (clazz.isArray()) {
            return ArrayDeserializer.instance;
        } else if (clazz == Set.class || clazz == HashSet.class || clazz == Collection.class || clazz == List.class
                   || clazz == ArrayList.class) {
            if (type instanceof ParameterizedType) {
                Type itemType = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (itemType == String.class) {
                    derializer = ArrayListStringDeserializer.instance;
                } else {
                    derializer = new ArrayListTypeDeserializer(clazz, itemType);
                }
            } else {
                derializer = CollectionDeserializer.instance;
            }
        } else if (Collection.class.isAssignableFrom(clazz)) {
            derializer = CollectionDeserializer.instance;
        } else if (Map.class.isAssignableFrom(clazz)) {
            derializer = MapDeserializer.instance;
        } else {
            derializer = createJavaBeanDeserializer(clazz, type);
        }

        putDeserializer(type, derializer);

        return derializer;
    }
	
	public ObjectDeserializer createJavaBeanDeserializer(Class<?> clazz, Type type) {
        /*if (clazz == Class.class) {
            return this.defaultSerializer;
        }*/

        boolean asmEnable = true;
        if (asmEnable && !Modifier.isPublic(clazz.getModifiers())) {
            asmEnable = false;
        }

        if (clazz.getTypeParameters().length != 0) {
            asmEnable = false;
        }

        if (ASMClassLoader.isExternalClass(clazz)) {
            asmEnable = false;
        }

        if (asmEnable) {
            DeserializeBeanInfo beanInfo = DeserializeBeanInfo.computeSetters(clazz, type);
            for (FieldInfo fieldInfo : beanInfo.getFieldList()) {
                if (fieldInfo.isGetOnly()) {
                    asmEnable = false;
                    break;
                }
                
                Class<?> fieldClass = fieldInfo.getFieldClass();
                if (!Modifier.isPublic(fieldClass.getModifiers())) {
                    asmEnable = false;
                    break;
                }

                if (fieldClass.isMemberClass() && !Modifier.isStatic(fieldClass.getModifiers())) {
                    asmEnable = false;
                }
            }
        }

        if (asmEnable) {
            if (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
                asmEnable = false;
            }
        }

        if (!asmEnable) {
            return new JavaBeanDeserializer(this, clazz, type);
        }

        try {
            return ASMDeserializerFactory.getInstance().createJavaBeanDeserializer(this, clazz, type);
        } catch (ASMException asmError) {
            return new JavaBeanDeserializer(this, clazz, type);
        } catch (Exception e) {
            throw new PBException("create asm deserializer error, " + clazz.getName(), e);
        }
    }
	
	public void putDeserializer(Type type, ObjectDeserializer deserializer) {
        derializers.put(type, deserializer);
    }
	
	public FieldDeserializer createFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo) {
        boolean asmEnable = true;

        if (!Modifier.isPublic(clazz.getModifiers())) {
            asmEnable = false;
        }

        if (fieldInfo.getFieldClass() == Class.class) {
            asmEnable = false;
        }

        if (ASMClassLoader.isExternalClass(clazz)) {
            asmEnable = false;
        }

        if (!asmEnable) {
            return createFieldDeserializerWithoutASM(mapping, clazz, fieldInfo);
        }

        try {
            return ASMDeserializerFactory.getInstance().createFieldDeserializer(mapping, clazz, fieldInfo);
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return createFieldDeserializerWithoutASM(mapping, clazz, fieldInfo);
    }
	
	public FieldDeserializer createFieldDeserializerWithoutASM(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo) {
        Class<?> fieldClass = fieldInfo.getFieldClass();
        
        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return new BooleanFieldDeserializer(mapping, clazz, fieldInfo);
        }

        if (fieldClass == int.class || fieldClass == Integer.class) {
            return new IntegerFieldDeserializer(mapping, clazz, fieldInfo);
        }

        if (fieldClass == long.class || fieldClass == Long.class) {
            return new LongFieldDeserializer(mapping, clazz, fieldInfo);
        }
        
        if (fieldClass == String.class) {
            return new StringFieldDeserializer(mapping, clazz, fieldInfo);
        }

        if (fieldClass == List.class || fieldClass == ArrayList.class) {
            Type fieldType = fieldInfo.getFieldType();
            if (fieldType instanceof ParameterizedType) {
                Type itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                if (itemType == String.class) {
                    return new ArrayListStringFieldDeserializer(mapping, clazz, fieldInfo);
                }
            }

            return new ArrayListTypeFieldDeserializer(mapping, clazz, fieldInfo);
        }

        return new DefaultFieldDeserializer(mapping, clazz, fieldInfo);
    }
	
	public Object getObject()
	{
		return null;
	}
	
	public Object parse(Object fieldName) {
		try
		{
	        int tmpType = (int)scanByte();
	        switch (tmpType) {
	            case com.jd.glowworm.asm.Type.OBJECT:
	                return parseObject(null);
	            case com.jd.glowworm.asm.Type.BYTE:
	                return scanByte();
	            case com.jd.glowworm.asm.Type.SHORT:
	                return scanInt();
	            case com.jd.glowworm.asm.Type.CHAR:
	                String tmpStr = scanString();
	                return tmpStr.charAt(0);
	            case com.jd.glowworm.asm.Type.INT:
	                Number intValue = scanInt();
	                return intValue;
	            case com.jd.glowworm.asm.Type.DOUBLE:
	                double tmpDoubleValue = scanDouble();
	                return tmpDoubleValue;
	            case com.jd.glowworm.asm.Type.FLOAT:
	                Object value = scanFloat();
	                return value;
	            case com.jd.glowworm.asm.Type.LONG:
	                long tmpLongValue = scanLong();
	                return tmpLongValue;
	            case com.jd.glowworm.asm.Type.STRING:
	                return scanString();
	            case com.jd.glowworm.asm.Type.BOOLEAN:
	                return scanBool();
	            case com.jd.glowworm.asm.Type.BIGDECIMAL:
	                return scanBigDecimal();
	            case com.jd.glowworm.asm.Type.ENUM:
	                return scanEnum();
	            case com.jd.glowworm.asm.Type.ARRAY_BYTE:
	            	return scanByteArray();
	            case com.jd.glowworm.asm.Type.ARRAY_CHAR:
	            	String tmpCharsStr = scanString();
	            	return tmpCharsStr.toCharArray();
	            case com.jd.glowworm.asm.Type.ARRAY_INT:
	            	return scanIntArray();
	            case com.jd.glowworm.asm.Type.ARRAY_DOUBLE:
	            	return scanDoubleArray();
	            case com.jd.glowworm.asm.Type.ARRAY_FLOAT:
	            	return scanFloatArray();
	            case com.jd.glowworm.asm.Type.ARRAY_LONG:
	            	return scanLongArray();
	            case com.jd.glowworm.asm.Type.ARRAY_OBJ:
	            	return scanObjArray();
	            case com.jd.glowworm.asm.Type.ARRAY_SHORT:
	            	return scanIntArray();
	            case com.jd.glowworm.asm.Type.ARRAY_BOOLEAN:
	            	return scanBooleanArray();
	            case com.jd.glowworm.asm.Type.LIST_ARRAYLIST:
	            	ArrayList retList = new ArrayList();
	            	parseArray(Object.class, retList, null);
	            	return retList;
	            case com.jd.glowworm.asm.Type.LIST_LINKEDLIST:
	            	LinkedList retLinkedList = new LinkedList();
	            	parseArray(Object.class, retLinkedList, null);
	            	return retLinkedList;
	            case com.jd.glowworm.asm.Type.COLLECTION_HASHSET:
	            	HashSet retHashSet = new HashSet();
	            	parseArray(Object.class, retHashSet, null);
	            	return retHashSet;
	            case com.jd.glowworm.asm.Type.COLLECTION_TREESET:
	            	TreeSet retTreeSet = new TreeSet();
	            	parseArray(Object.class, retTreeSet, null);
	            	return retTreeSet;
	            case com.jd.glowworm.asm.Type.MAP_HASH:
	            	HashMap retMap = new HashMap();
	            	int tmpMapSz = scanInt();
	            	parseObject(retMap, null, tmpMapSz);
	            	return retMap;
	            case com.jd.glowworm.asm.Type.MAP_LinkedHash:
	            	LinkedHashMap retLinkedHashMap = new LinkedHashMap();
	            	tmpMapSz = scanInt();
	            	parseObject(retLinkedHashMap, null, tmpMapSz);
	            	return retLinkedHashMap;
	            case com.jd.glowworm.asm.Type.MAP_ConcurrentHashMap:
	            	ConcurrentHashMap retConcurrentHashMap = new ConcurrentHashMap();
	            	tmpMapSz = scanInt();
	            	parseObject(retConcurrentHashMap, null, tmpMapSz);
	            	return retConcurrentHashMap;
	            /*case com.jd.glowworm.asm.Type.ARRAY:
	                return scanBool();*/
	            default:
	                throw new PBException("没找到对应的解析类型 " + tmpType);
	        }
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
    }
	
	private int[] scanIntArray()
	{
		int[] ret = null;
		
		try
		{
			int tmpListSz = scanInt();
			ret = new int[tmpListSz];
	        for (int i = 0; i < tmpListSz; i++)
	        {
	        	ret[i] = scanInt();
	        }
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	private long[] scanLongArray()
	{
		long[] ret = null;
		
		try
		{
			int tmpListSz = scanInt();
			ret = new long[tmpListSz];
	        for (int i = 0; i < tmpListSz; i++)
	        {
	        	ret[i] = scanLong();
	        }
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	private double[] scanDoubleArray()
	{
		double[] ret = null;
		
		try
		{
			int tmpListSz = scanInt();
			ret = new double[tmpListSz];
	        for (int i = 0; i < tmpListSz; i++)
	        {
	        	ret[i] = scanDouble();
	        }
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	private float[] scanFloatArray()
	{
		float[] ret = null;
		
		try
		{
			int tmpListSz = scanInt();
			ret = new float[tmpListSz];
	        for (int i = 0; i < tmpListSz; i++)
	        {
	        	ret[i] = scanFloat();
	        }
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	private byte[] scanByteArray()
	{
		byte[] ret = null;
		
		try
		{
			int tmpListSz = scanInt();
			ret = theCodedInputStream.readRawBytes(tmpListSz).toByteArray();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	private boolean[] scanBooleanArray()
	{
		boolean[] ret = null;
		byte[] tmpBytes = null;
		
		try
		{
			int tmpListSz = scanInt();
			ret = new boolean[tmpListSz];
			tmpBytes = theCodedInputStream.readRawBytes(tmpListSz).toByteArray();
			for (int i = 0; i < tmpBytes.length; i++)
			{
				if (tmpBytes[i] == 1)
				{
					ret[i] = true;
				}
				else
				{
					ret[i] = false;
				}
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	private Object[] scanObjArray()
	{
		Object[] ret = null;
		
		try
		{
			int tmpListSz = scanInt();
			ret = new Object[tmpListSz];
	        for (int i = 0; i < tmpListSz; i++)
	        {
	        	byte tmpIsNull = theCodedInputStream.readRawByte();
            	if (0 == tmpIsNull)
            	{
            		ret[i] = parse(null);
            	}
            	else
            	{
            		ret[i] = null;
            	}
	        }
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	public void parseArray(Type type, Collection array, Object fieldName) {
        ObjectDeserializer deserializer = null;
        try
        {
	        if (int.class == type) {
	            deserializer = IntegerDeserializer.instance;
	            parseIntOrdoubleOrlong(array, deserializer);
	            return;
	        } else if (short.class == type) {
	            deserializer = IntegerDeserializer.instance;
	            parseIntOrdoubleOrlong(array, deserializer);
	            return;
	        } else if (long.class == type) {
	            deserializer = LongDeserializer.instance;
	            parseIntOrdoubleOrlong(array, deserializer);
	            return;
	        } else if (double.class == type) {
	            deserializer = DoubleDeserializer.instance;
	            parseIntOrdoubleOrlong(array, deserializer);
	            return;
	        } else if (float.class == type) {
	            deserializer = FloatDeserializer.instance;
	            parseIntOrdoubleOrlong(array, deserializer);
	            return;
	        } else {
	            deserializer = getDeserializer(type);
	        }
	        
	        ParseContext context = this.getContext();
	        this.setContext(array, fieldName);
	        
        	int tmpListSz = scanInt();
            for (int i = 0; i < tmpListSz; i++)
            {
            	byte tmpIsNull = theCodedInputStream.readRawByte();
            	if (0 == tmpIsNull)
            	{
            		Object val;
            		// 如果是ASM生成的反序列化类
	                if (deserializer.getClass().getName().startsWith(ASMDeserializerFactory.DeserializerClassName_prefix))
	        		{
	                	byte tmpType = theCodedInputStream.readRawByte();
	                    String tmpClassName = theCodedInputStream.readString();
	                    System.out.println("tmpClassName "+tmpClassName);
	        		}
	                // 如果不是JavaObjectDeserializer
	                else if ( ! JavaObjectDeserializer.class.isAssignableFrom(deserializer.getClass()))
	                {
	                	byte tmpType = theCodedInputStream.readRawByte();
	                }
	                
	                val = deserializer.deserialze(this, type, null);
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
        finally
        {
            this.setContext(context);
        }
    }
	
	private void parseIntOrdoubleOrlong(Collection array, ObjectDeserializer deserializer) throws Exception
	{
		int tmpListSz = scanInt();
        for (int i = 0; i < tmpListSz; i++)
        {
        	Object val = deserializer.deserialze(this, null, null);
            array.add(val);
        }
	}
	
	public byte[] parseByteArray() throws Exception
	{
		byte[] ret = null;
		int tmpListSz = scanInt();
		ret = theCodedInputStream.readRawBytes(tmpListSz).toByteArray();
        return ret;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public final Object parseObject(Object fieldName) {
		try
		{
           String typeName = scanString();
           Class<?> clazz = TypeUtils.loadClass(typeName);
           ObjectDeserializer deserializer = getDeserializer(clazz);
           return deserializer.deserialze(this, clazz, fieldName);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		return null;
    }

	public ParseContext getContext() {
		return context;
	}

	public void setContext(ParseContext context) {
		this.context = context;
	}
	
	public ParseContext setContext(Object object, Object fieldName) {
        return setContext(this.context, object, fieldName);
    }
	
	public ParseContext setContext(ParseContext parent, Object object, Object fieldName) {
        this.context = new ParseContext(parent, object, fieldName);
        addContext(this.context);        
        return this.context;
    }
	
	public void popContext() {
        this.context = this.context.getParentContext();
        contextArray[contextArrayIndex - 1] = null;
        contextArrayIndex--;
    }
	
	private void addContext(ParseContext context) {
        int i = contextArrayIndex++;
        if (i >= contextArray.length) {
            int newLen = (contextArray.length * 3) / 2;
            ParseContext[] newArray = new ParseContext[newLen];
            System.arraycopy(contextArray, 0, newArray, 0, contextArray.length);
            contextArray = newArray;
        }
        contextArray[i] = context;
    }

	public CodedInputStream getTheCodedInputStream() {
		return theCodedInputStream;
	}
	
	public Map<String, FieldDeserializer> getFieldDeserializers(Class<?> clazz) {
        ObjectDeserializer deserizer = getDeserializer(clazz);

        if (deserizer instanceof JavaBeanDeserializer) {
            return ((JavaBeanDeserializer) deserizer).getFieldDeserializerMap();
        } else if (deserizer instanceof ASMJavaBeanDeserializer) {
            return ((ASMJavaBeanDeserializer) deserizer).getInnterSerializer().getFieldDeserializerMap();
        } else {
            return Collections.emptyMap();
        }
    }
	
	public Object doASMDeserializer(ObjectDeserializer theObjectDeserializerParm,
			Type type, Object fieldName)
	{
		Object ret = null;
		try
		{
			if (ASMJavaBeanDeserializer.class
					.isAssignableFrom(theObjectDeserializerParm.getClass()))
			{
				byte tmpByte = theCodedInputStream.readRawByte();
				if (tmpByte == 1)
				{
					return null;
				}
			}
			
			ret = theObjectDeserializerParm.deserialze(this, type, fieldName);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		
		return ret;
	}
}
