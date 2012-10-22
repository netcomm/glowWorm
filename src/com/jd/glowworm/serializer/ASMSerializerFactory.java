package com.jd.glowworm.serializer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.jd.glowworm.PBException;
import com.jd.glowworm.asm.ClassWriter;
import com.jd.glowworm.asm.FieldVisitor;
import com.jd.glowworm.asm.Label;
import com.jd.glowworm.asm.MethodVisitor;
import com.jd.glowworm.asm.Opcodes;
import com.jd.glowworm.util.ASMClassLoader;
import com.jd.glowworm.util.ASMUtils;
import com.jd.glowworm.util.FieldInfo;
import com.jd.glowworm.util.TypeUtils;

public class ASMSerializerFactory implements Opcodes {
	public static String GenClassName_prefix = "Glowworm_Serializer_";
    private ASMClassLoader classLoader = new ASMClassLoader();

    public ObjectSerializer createJavaBeanSerializer(Class<?> clazz) throws Exception {
        return createJavaBeanSerializer(clazz, (Map<String, String>) null);
    }

    private final AtomicLong seed = new AtomicLong();

    public String getGenClassName(Class<?> clazz) {
        return GenClassName_prefix + seed.incrementAndGet();
    }

    static class Context {

        private final String className;

        public Context(String className){
            this.className = className;
        }

        private int                  variantIndex = 8;

        private Map<String, Integer> variants     = new HashMap<String, Integer>();

        public int serializer() {
            return 1;
        }

        public String getClassName() {
            return className;
        }

        public int obj() {
            return 2;
        }

        public int paramFieldName() {
            return 3;
        }

        public int paramFieldType() {
            return 4;
        }

        public int fieldName() {
            return 5;
        }

        public int original() {
            return 6;
        }

        public int processValue() {
            return 7;
        }

        public int getVariantCount() {
            return variantIndex;
        }

        public int var(String name) {
            Integer i = variants.get(name);
            if (i == null) {
                variants.put(name, variantIndex++);
            }
            i = variants.get(name);
            return i.intValue();
        }

        public int var(String name, int increment) {
            Integer i = variants.get(name);
            if (i == null) {
                variants.put(name, variantIndex);
                variantIndex += increment;
            }
            i = variants.get(name);
            return i.intValue();
        }
    }

    public ObjectSerializer createJavaBeanSerializer(Class<?> clazz, Map<String, String> aliasMap) throws Exception {
        if (clazz.isPrimitive()) {
            throw new PBException("unsupportd class " + clazz.getName());
        }

        List<FieldInfo> getters = TypeUtils.computeGetters(clazz, aliasMap, true);

        String className = getGenClassName(clazz);

        ClassWriter cw = new ClassWriter();
        
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className, "java/lang/Object",
                new String[] { "com/jd/glowworm/serializer/ObjectSerializer" });
        
        for (FieldInfo fieldInfo : getters) {
            FieldVisitor fw = cw.visitField(ACC_PUBLIC, fieldInfo.getName() + "_asm_fieldType",
                                            "Ljava/lang/reflect/Type;");
            fw.visitEnd();
        }

        MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mw.visitVarInsn(ALOAD, 0);
        mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");

        // mw.visitFieldInsn(PUTFIELD, context.getClassName(), fieldInfo.getName() + "_asm_prefix__", "[C");
        mw.visitInsn(RETURN);
        mw.visitMaxs(4, 4);
        mw.visitEnd();

        {
            Context context = new Context(className);

            mw = cw.visitMethod(ACC_PUBLIC,
                                "write",
                                "(Lcom/jd/glowworm/serializer/PBSerializer;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;)V",
                                null, new String[] { "java/io/IOException" });

            mw.visitVarInsn(ALOAD, context.serializer()); // serializer
            mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(PBSerializer.class), "getWriter",
                               "()" + ASMUtils.getDesc(SerializeWriter.class));
            mw.visitVarInsn(ASTORE, context.var("out"));
            
            mw.visitVarInsn(ALOAD, context.obj()); // obj
            mw.visitTypeInsn(CHECKCAST, ASMUtils.getType(clazz)); // serializer
            mw.visitVarInsn(ASTORE, context.var("entity")); // obj

            generateWriteMethod(clazz, mw, getters, context);

            mw.visitInsn(RETURN);
            mw.visitMaxs(5, context.getVariantCount() + 1);
            mw.visitEnd();
        }
        
        byte[] code = cw.toByteArray();
        
        /*org.apache.commons.io.IOUtils.write(code, new java.io.FileOutputStream(
        		"D:/"+ className + ".class"));*/

        Class<?> exampleClass = classLoader.defineClassPublic(className, code, 0, code.length);
        Object instance = exampleClass.newInstance();

        return (ObjectSerializer) instance;
    }

    private void generateWriteMethod(Class<?> clazz, MethodVisitor mw, List<FieldInfo> getters, Context context)
                                                                                                                throws Exception {
        Label end = new Label();
        int size = getters.size();
        
        for (int i = 0; i < size; ++i) {
            FieldInfo property = getters.get(i);
            Class<?> propertyClass = property.getFieldClass();

            mw.visitLdcInsn(property.getName());
            mw.visitVarInsn(ASTORE, context.fieldName());

            if (propertyClass == byte.class) {
                _byte(clazz, mw, property, context);
            } else if (propertyClass == short.class) {
                _short(clazz, mw, property, context);
            } else if (propertyClass == int.class) {
                _int(clazz, mw, property, context);
            } else if (propertyClass == long.class) {
                _long(clazz, mw, property, context);
            } else if (propertyClass == float.class) {
                _float(clazz, mw, property, context);
            } else if (propertyClass == double.class) {
                _double(clazz, mw, property, context);
            } else if (propertyClass == boolean.class) {
                _boolean(clazz, mw, property, context);
            } else if (propertyClass == char.class) {
                _char(clazz, mw, property, context);
            } else if (propertyClass == String.class) {
                _string(clazz, mw, property, context, i);
            } else if (propertyClass == BigDecimal.class) {
                _decimal(clazz, mw, property, context);
            } else if (List.class.isAssignableFrom(propertyClass)) {
                _list(clazz, mw, property, context, i);
                // _object(clazz, mw, property, context);
            } else if (propertyClass.isEnum()) {
                _enum(clazz, mw, property, context);
            } else {
                _object(clazz, mw, property, context, i);
            }
        }
    }
    
    private void _object(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context,
    		int fieldInfoIndexParm) {
        Label _end = new Label();

        _get(mw, context, property);
        mw.visitVarInsn(ASTORE, context.var("object"));

        _writeObject(mw, property, context, _end, fieldInfoIndexParm);

        mw.visitLabel(_end);
    }

    private void _enum(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        boolean writeEnumUsingToString = false;
        
        Label _not_null = new Label();
        Label _end_if = new Label();
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitTypeInsn(CHECKCAST, ASMUtils.getType(Enum.class)); // cast
        mw.visitVarInsn(ASTORE, context.var("enum"));
        
        mw.visitVarInsn(ALOAD, context.var("enum"));
        mw.visitJumpInsn(IFNONNULL, _not_null);
        _if_write_null(mw, property, context);
        mw.visitJumpInsn(GOTO, _end_if);

        mw.visitLabel(_not_null);
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ILOAD, context.var("seperator"));
        mw.visitVarInsn(ALOAD, context.fieldName());
        mw.visitVarInsn(ALOAD, context.var("enum"));

        if (writeEnumUsingToString) {
            mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(Object.class), "toString", "()Ljava/lang/String;");
            mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue",
                               "(CLjava/lang/String;Ljava/lang/String;)V");
        } else {
            mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue",
                               "(CLjava/lang/String;L" + ASMUtils.getType(Enum.class) + ";)V");
        }

        _seperator(mw, context);

        mw.visitLabel(_end_if);

        mw.visitLabel(_end);
    }

    private void _long(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(LSTORE, context.var("long", 2));
        
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(LLOAD, context.var("long", 2));
        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue", "(J)V");

        _seperator(mw, context);

        mw.visitLabel(_end);
    }

    private void _float(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(FSTORE, context.var("float"));
        
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(FLOAD, context.var("float"));
        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue", "(F)V");
        
        mw.visitLabel(_end);
    }

    private void _double(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(DSTORE, context.var("double", 2));
        
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(DLOAD, context.var("double", 2));
        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue", "(D)V");
        
        mw.visitLabel(_end);
    }

    private void _char(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(ISTORE, context.var("char"));
        
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ILOAD, context.var("char"));

        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue", "(C)V");
        
        mw.visitLabel(_end);
    }

    private void _boolean(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(ISTORE, context.var("boolean"));
        
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ILOAD, context.var("boolean"));

        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue", "(Z)V");
        
        mw.visitLabel(_end);
    }

    private void _get(MethodVisitor mw, Context context, FieldInfo property) {
        Method method = property.getMethod();
        if (method != null) {
            mw.visitVarInsn(ALOAD, context.var("entity"));
            mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(method.getDeclaringClass()), method.getName(), ASMUtils.getDesc(method));
        } else {
            mw.visitVarInsn(ALOAD, context.var("entity"));
            mw.visitFieldInsn(GETFIELD, ASMUtils.getType(property.getDeclaringClass()), property.getName(),
            		ASMUtils.getDesc(property.getFieldClass()));
        }
    }

    private void _byte(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(ISTORE, context.var("byte"));
        
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ILOAD, context.var("byte"));

        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue_Byte", "(I)V");

        _seperator(mw, context);

        mw.visitLabel(_end);
    }

    private void _short(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(ISTORE, context.var("short"));

        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ILOAD, context.var("short"));

        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue", "(I)V");
        
        mw.visitLabel(_end);
    }

    private void _int(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(ISTORE, context.var("int"));
        
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ILOAD, context.var("int"));

        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue", "(I)V");
        
        mw.visitLabel(_end);
    }

    private void _decimal(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(ASTORE, context.var("decimal"));
        
        Label _if = new Label();
        Label _else = new Label();
        Label _end_if = new Label();

        mw.visitLabel(_if);

        // if (decimalValue == null) {
        mw.visitVarInsn(ALOAD, context.var("decimal"));
        mw.visitJumpInsn(IFNONNULL, _else);
        _if_write_null(mw, property, context);
        mw.visitJumpInsn(GOTO, _end_if);

        mw.visitLabel(_else); // else { out.writeFieldValue(seperator, fieldName, fieldValue)

        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ILOAD, context.var("seperator"));
        mw.visitVarInsn(ALOAD, context.fieldName());
        mw.visitVarInsn(ALOAD, context.var("decimal"));
        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue",
                           "(CLjava/lang/String;Ljava/math/BigDecimal;)V");
        
        mw.visitJumpInsn(GOTO, _end_if);

        mw.visitLabel(_end_if);

        mw.visitLabel(_end);
    }

    private void _string(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context,
    		int fieldInfoIndexParm) {
        Label _end = new Label();
        
        _get(mw, context, property);
        mw.visitVarInsn(ASTORE, context.var("string"));
        
        Label _else = new Label();
        Label _end_if = new Label();

        // if (value == null) {
        mw.visitVarInsn(ALOAD, context.var("string"));
        mw.visitJumpInsn(IFNONNULL, _else);

        _if_write_null(mw, property, context);

        mw.visitJumpInsn(GOTO, _end_if);

        mw.visitLabel(_else); // else { out.writeFieldValue(seperator, fieldName, fieldValue)
        mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ALOAD, context.fieldName());
        mw.visitVarInsn(ALOAD, context.var("string"));
        mw.visitIntInsn(BIPUSH, fieldInfoIndexParm);
        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldValue",
                           "(Ljava/lang/String;Ljava/lang/String;I)V");

        mw.visitLabel(_end_if);

        mw.visitLabel(_end);
    }

    private void _list(Class<?> clazz, MethodVisitor mw, FieldInfo property, Context context,
    		int fieldInfoIndexParm) {
        Type propertyType = property.getFieldType();

        Type elementType;
        if (propertyType instanceof Class) {
            elementType = Object.class;
        } else {
            elementType = ((ParameterizedType) propertyType).getActualTypeArguments()[0];
        }

        Class<?> elementClass = null;
        if (elementType instanceof Class<?>) {
            elementClass = (Class<?>) elementType;
        }
        
        Label _end = new Label();
        
        Label _if = new Label();
        Label _else = new Label();
        Label _end_if = new Label();

        mw.visitLabel(_if);
        
        _get(mw, context, property);
        mw.visitTypeInsn(CHECKCAST, ASMUtils.getType(List.class)); // cast
        mw.visitVarInsn(ASTORE, context.var("list"));
        
        mw.visitVarInsn(ALOAD, context.var("list"));
        mw.visitJumpInsn(IFNONNULL, _else);
        _if_write_null(mw, property, context);
        mw.visitJumpInsn(GOTO, _end_if);

        mw.visitLabel(_else); // else {
        
        mw.visitVarInsn(ALOAD, context.var("list"));
        mw.visitMethodInsn(INVOKEINTERFACE, ASMUtils.getType(List.class), "size", "()I");
        mw.visitVarInsn(ISTORE, context.var("int"));
        
        mw.visitVarInsn(ALOAD, context.var("out"));
        //mw.visitVarInsn(ALOAD, context.fieldName());
        //mw.visitIntInsn(BIPUSH, fieldInfoIndexParm);
        mw.visitIntInsn(BIPUSH, fieldInfoIndexParm);
        mw.visitVarInsn(ILOAD, context.var("int"));
        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeGroup", "(II)V");
        
        Label _if_3 = new Label();
        Label _else_3 = new Label();
        Label _end_if_3 = new Label();

        mw.visitLabel(_if_3);

        mw.visitVarInsn(ILOAD, context.var("int"));
        mw.visitInsn(ICONST_0);
        mw.visitJumpInsn(IF_ICMPNE, _else_3);
        // if list.size == 0
        mw.visitJumpInsn(GOTO, _end_if_3);

        mw.visitLabel(_else_3);
        {
            Label _for = new Label();
            Label _end_for = new Label();

            mw.visitInsn(ICONST_0);
            mw.visitVarInsn(ISTORE, context.var("i"));

            // for (; i < list.size() -1; ++i) {
            mw.visitLabel(_for);
            mw.visitVarInsn(ILOAD, context.var("i"));

            mw.visitVarInsn(ILOAD, context.var("int"));
            
            mw.visitJumpInsn(IF_ICMPGE, _end_for); // j < list.size

            /*if (elementType == String.class) {
                // out.write((String)list.get(i));
                mw.visitVarInsn(ALOAD, context.var("out"));
                mw.visitVarInsn(ALOAD, context.var("list"));
                mw.visitVarInsn(ILOAD, context.var("i"));
                mw.visitMethodInsn(INVOKEINTERFACE, ASMUtils.getType(List.class), "get", "(I)Ljava/lang/Object;");
                mw.visitTypeInsn(CHECKCAST, ASMUtils.getType(String.class)); // cast to string
                mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeString",
                                   "(Ljava/lang/String;)V");
            } else {*/
                mw.visitVarInsn(ALOAD, context.serializer());
                mw.visitVarInsn(ALOAD, context.var("list"));
                mw.visitVarInsn(ILOAD, context.var("i"));
                mw.visitMethodInsn(INVOKEINTERFACE, ASMUtils.getType(List.class), "get", "(I)Ljava/lang/Object;");
                mw.visitVarInsn(ILOAD, context.var("i"));
                mw.visitMethodInsn(INVOKESTATIC, ASMUtils.getType(Integer.class), "valueOf", "(I)Ljava/lang/Integer;");

                if (elementClass != null && Modifier.isPublic(elementClass.getModifiers())) {
                    mw.visitLdcInsn(com.jd.glowworm.asm.Type.getType(ASMUtils.getDesc((Class<?>) elementType)));
                    mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(PBSerializer.class), "writeArrayItem",
                                       "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;)V");
                } else {
                    mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(PBSerializer.class), "writeWithFieldName",
                                       "(Ljava/lang/Object;Ljava/lang/Object;)V");
                }
            //}

            mw.visitIincInsn(context.var("i"), 1);
            mw.visitJumpInsn(GOTO, _for);

            mw.visitLabel(_end_for);
        }
        
        mw.visitLabel(_end_if_3);

        _seperator(mw, context);

        mw.visitLabel(_end_if);

        mw.visitLabel(_end);
    }
    
    private void _writeObject(MethodVisitor mw, FieldInfo fieldInfo, Context context, Label _end,
    		int fieldInfoIndexParm) {
        Label _not_null = new Label();

        /*mw.visitVarInsn(ALOAD, context.processValue());
        mw.visitJumpInsn(IFNONNULL, _not_null); // if (obj == null)
        _if_write_null(mw, fieldInfo, context);
        mw.visitJumpInsn(GOTO, _end);*/

        mw.visitLabel(_not_null);
        // out.writeFieldName("fieldName")
        /*mw.visitVarInsn(ALOAD, context.var("out"));
        mw.visitVarInsn(ALOAD, context.fieldName());
        mw.visitIntInsn(BIPUSH, fieldInfoIndexParm);
        mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeFieldName", "(Ljava/lang/String;I)V");*/

        // serializer.write(obj)
        mw.visitVarInsn(ALOAD, context.serializer()); // serializer
        mw.visitVarInsn(ALOAD, context.var("object"));
        
        mw.visitVarInsn(ALOAD, context.fieldName());
            if (fieldInfo.getFieldType() instanceof Class<?> && ((Class<?>) fieldInfo.getFieldType()).isPrimitive()) {
                mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(PBSerializer.class), "writeWithFieldName",
                                   "(Ljava/lang/Object;Ljava/lang/Object;)V");
            } else {
                // fieldInfo.getName() + "_asm_fieldType"

                mw.visitVarInsn(ALOAD, 0);
                mw.visitFieldInsn(GETFIELD, context.getClassName(), fieldInfo.getName() + "_asm_fieldType",
                                  "Ljava/lang/reflect/Type;");

                mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(PBSerializer.class), "writeWithFieldName",
                                   "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/reflect/Type;)V");
            }
    }
    
    private void _if_write_null(MethodVisitor mw, FieldInfo fieldInfo, Context context) {
    	mw.visitVarInsn(ALOAD, context.var("out"));
    	mw.visitMethodInsn(INVOKEVIRTUAL, ASMUtils.getType(SerializeWriter.class), "writeNull",
                "()V");
    }

    private void _seperator(MethodVisitor mw, Context context) {
        mw.visitVarInsn(BIPUSH, ',');
        mw.visitVarInsn(ISTORE, context.var("seperator"));
    }

}
