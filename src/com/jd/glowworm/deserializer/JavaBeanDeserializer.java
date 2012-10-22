package com.jd.glowworm.deserializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.jd.glowworm.PBException;
import com.jd.glowworm.util.DeserializeBeanInfo;
import com.jd.glowworm.util.FieldInfo;
import com.jd.glowworm.util.TypeUtils;

public class JavaBeanDeserializer implements ObjectDeserializer {

    private final Map<String, FieldDeserializer> feildDeserializerMap = new IdentityHashMap<String, FieldDeserializer>();

    private final List<FieldDeserializer>        fieldDeserializers   = new ArrayList<FieldDeserializer>();

    private final Class<?>                       clazz;
    private final Type                           type;

    private DeserializeBeanInfo                  beanInfo;

    public JavaBeanDeserializer(DeserializeBeanInfo beanInfo){
        this.beanInfo = beanInfo;
        this.clazz = beanInfo.getClazz();
        this.type = beanInfo.getType();
    }

    public JavaBeanDeserializer(PBDeserializer config, Class<?> clazz){
        this(config, clazz, clazz);
    }

    public JavaBeanDeserializer(PBDeserializer config, Class<?> clazz, Type type){
        this.clazz = clazz;
        this.type = type;

        beanInfo = DeserializeBeanInfo.computeSetters(clazz, type);

        for (FieldInfo fieldInfo : beanInfo.getFieldList()) {
            addFieldDeserializer(config, clazz, fieldInfo);
        }
        
        Collections.sort(beanInfo.getFieldList());
        Collections.sort((ArrayList)fieldDeserializers);
    }

    public Map<String, FieldDeserializer> getFieldDeserializerMap() {
        return feildDeserializerMap;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Type getType() {
        return type;
    }

    private void addFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo) {
        FieldDeserializer fieldDeserializer = createFieldDeserializer(mapping, clazz, fieldInfo);

        feildDeserializerMap.put(fieldInfo.getName().intern(), fieldDeserializer);
        fieldDeserializers.add(fieldDeserializer);
    }

    public FieldDeserializer createFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo) {
        return mapping.createFieldDeserializer(mapping, clazz, fieldInfo);
    }

    public Object createInstance(PBDeserializer parser, Type type) {
        if (beanInfo.getDefaultConstructor() == null) {
            return null;
        }

        Object object;
        try {
            Constructor<?> constructor = beanInfo.getDefaultConstructor();
            if (constructor.getParameterTypes().length == 0) {
                object = constructor.newInstance();
            } else {
                object = constructor.newInstance(parser.getObject());
            }
        } catch (Exception e) {
            throw new PBException("create instance error, class " + clazz.getName(), e);
        }
        
        return object;
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialze(PBDeserializer parser, Type type, Object fieldName) {
        ParseContext context = parser.getContext();
        ParseContext childContext = null;
        Object object = null;
        
        Map<String, Object> fieldValues = null;
        object = createInstance(parser, type);
        if (object == null) {
            fieldValues = new HashMap<String, Object>(this.fieldDeserializers.size());
        }
        childContext = parser.setContext(context, object, fieldName);
        
        try {
        	/*if (fieldName != null)
            {
            	parser.scanByte();
            	parser.scanString();
            }*/
        	
            int tmpListSz = fieldDeserializers.size();
            for (int i = 0; i < tmpListSz; i++) {
    	            /*case SET:
    	                lexer.nextToken();
    	                HashSet<Object> set = new HashSet<Object>();
    	                parseArray(set, fieldName);
    	                return set;
    	            case TREE_SET:
    	                lexer.nextToken();
    	                TreeSet<Object> treeSet = new TreeSet<Object>();
    	                parseArray(treeSet, fieldName);
    	                return treeSet;
    	            case LBRACKET:
    	                JSONArray array = new JSONArray();
    	                parseArray(array, fieldName);
    	                return array;*/
    	            /*if (com.cn.jd.glowworm.asm.Type.OBJECT == tmpType)
    	            {
    	            	String tmpTypeName = parser.scanString();
    	            	Class<?> userType = TypeUtils.loadClass(tmpTypeName);
                        ObjectDeserializer deserizer = parser.getDeserializer(userType);
                        return (T) deserizer.deserialze(parser, userType, fieldName);
    	            }
    	            else
    	            {
    	                if (object == null && fieldValues == null) {
    	                    object = createInstance(parser, type);
    	                    if (object == null) {
    	                        fieldValues = new HashMap<String, Object>(this.fieldDeserializers.size());
    	                    }
    	                    childContext = parser.setContext(context, object, fieldName);
    	                }

    	                boolean match = parseField(parser, i, object, type, fieldValues);
    	            }*/
            	parseField(parser, i, object, type, fieldValues);
            }

            if (object == null) {
                if (fieldValues == null) {
                    object = createInstance(parser, type);
                    return (T) object;
                }

                List<FieldInfo> fieldInfoList = beanInfo.getFieldList();
                int size = fieldInfoList.size();
                Object[] params = new Object[size];
                for (int i = 0; i < size; ++i) {
                    FieldInfo fieldInfo = fieldInfoList.get(i);
                    params[i] = fieldValues.get(fieldInfo.getName());
                }

                if (beanInfo.getCreatorConstructor() != null) {
                    try {
                        object = beanInfo.getCreatorConstructor().newInstance(params);
                    } catch (Exception e) {
                        throw new PBException("create instance error, "
                                                + beanInfo.getCreatorConstructor().toGenericString(), e);
                    }
                } else if (beanInfo.getFactoryMethod() != null) {
                    try {
                        object = beanInfo.getFactoryMethod().invoke(null, params);
                    } catch (Exception e) {
                        throw new PBException("create factory method error, "
                                                + beanInfo.getFactoryMethod().toString(), e);
                    }
                }
            }

            return (T) object;
        }
        catch(Exception ex)
        {
        	ex.printStackTrace();
        	return null;
        }
        finally {
            if (childContext != null) {
                childContext.setObject(object);
            }
            parser.setContext(context);
        }
    }

    public boolean parseField(PBDeserializer parser, int feildIndexParm, Object object, Type objectType,
                              Map<String, Object> fieldValues) {
        FieldDeserializer fieldDeserializer = fieldDeserializers.get(feildIndexParm);
        fieldDeserializer.parseField(parser, object, objectType, fieldValues);

        return true;
    }
}
