package com.jd.glowworm.deserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.jd.glowworm.PBException;

public class MapDeserializer implements ObjectDeserializer {

	public final static MapDeserializer instance = new MapDeserializer();

	@SuppressWarnings("unchecked")
	public <T> T deserialze(PBDeserializer parser, Type type, Object fieldName)
	{
		ParseContext context = parser.getContext();
		Map<Object, Object> map = null;
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
			map = createMap(type);
			
			int tmpMapItemSize = parser.scanInt();
			if (tmpMapItemSize != 0)
			{
				parser.setContext(context, map, fieldName);
				return (T) deserialze(parser, type, fieldName, map, tmpMapItemSize);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			parser.setContext(context);
		}
		
		return (T) map;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object deserialze(PBDeserializer parser, Type type,
			Object fieldName, Map map, int mapItemSizeParm) {
		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type keyType = parameterizedType.getActualTypeArguments()[0];
			Type valueType = parameterizedType.getActualTypeArguments()[1];
			
			return parseMap(parser, map,
						keyType, valueType, fieldName, mapItemSizeParm);
		} else {
			return parser.parseObject(map, fieldName, mapItemSizeParm);
		}
	}

	public Object parseMap(PBDeserializer parser, Map<Object, Object> map,
			Type keyType, Type valueType, Object fieldName, int mapItemSizeParm)
	{
		ObjectDeserializer keyDeserializer = parser.getDeserializer(keyType);
		ObjectDeserializer valueDeserializer = parser.getDeserializer(valueType);
		
		ParseContext context = parser.getContext();
		try
		{
			for (int i = 0; i < mapItemSizeParm; i++)
			{
				Object key = keyDeserializer.deserialze(parser, keyType, null);
				Object value = valueDeserializer.deserialze(parser, valueType,
						null);

				if (map.size() == 0 && context != null
						&& context.getObject() != map) {
					parser.setContext(context, map, fieldName);
				}

				map.put(key, value);
			}
		}
		finally
		{
			parser.setContext(context);
		}

		return map;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<Object, Object> createMap(Type type) {
		if (type == Properties.class) {
			return new Properties();
		}

		if (type == Hashtable.class) {
			return new Hashtable();
		}

		if (type == IdentityHashMap.class) {
			return new IdentityHashMap();
		}

		if (type == SortedMap.class || type == TreeMap.class) {
			return new TreeMap();
		}

		if (type == ConcurrentMap.class || type == ConcurrentHashMap.class) {
			return new ConcurrentHashMap();
		}

		if (type == HashMap.class) { //
			return new HashMap();
		}

		if (type == LinkedHashMap.class) {
			return new LinkedHashMap();
		}

		if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;

			return createMap(parameterizedType.getRawType());
		}

		if (type instanceof Class<?>) {
			Class<?> clazz = (Class<?>) type;
			if (clazz.isInterface()) {
				throw new PBException("unsupport type " + type);
			}

			try {
				return (Map<Object, Object>) clazz.newInstance();
			} catch (Exception e) {
				throw new PBException("unsupport type " + type, e);
			}
		}

		throw new PBException("unsupport type " + type);
	}
}
