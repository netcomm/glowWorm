package com.jd.glowworm.deserializer;

import static com.jd.glowworm.util.ASMUtils.getDesc;
import static com.jd.glowworm.util.ASMUtils.getType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import com.jd.glowworm.PBException;
import com.jd.glowworm.asm.ASMException;
import com.jd.glowworm.asm.ClassWriter;
import com.jd.glowworm.asm.FieldVisitor;
import com.jd.glowworm.asm.Label;
import com.jd.glowworm.asm.MethodVisitor;
import com.jd.glowworm.asm.Opcodes;
import com.jd.glowworm.deserializer.ASMJavaBeanDeserializer.InnerJavaBeanDeserializer;
import com.jd.glowworm.util.ASMClassLoader;
import com.jd.glowworm.util.ASMUtils;
import com.jd.glowworm.util.DeserializeBeanInfo;
import com.jd.glowworm.util.FieldInfo;

public class ASMDeserializerFactory implements Opcodes {
	public static String DeserializerClassName_prefix = "Glowworm_ASM_";
	
    private static final ASMDeserializerFactory instance    = new ASMDeserializerFactory();

    private ASMClassLoader                      classLoader = new ASMClassLoader();

    private final AtomicLong                    seed        = new AtomicLong();

    public String getGenClassName(Class<?> clazz) {
        return DeserializerClassName_prefix + clazz.getSimpleName() + "_" + seed.incrementAndGet();
    }

    public String getGenFieldDeserializer(Class<?> clazz, FieldInfo fieldInfo) {
        String name = DeserializerClassName_prefix+"Field_" + clazz.getSimpleName();
        name += "_" + fieldInfo.getName() + "_" + seed.incrementAndGet();

        return name;
    }

    public ASMDeserializerFactory(){

    }

    public final static ASMDeserializerFactory getInstance() {
        return instance;
    }

    public ObjectDeserializer createJavaBeanDeserializer(PBDeserializer config, Class<?> clazz, Type type) throws Exception {
        if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("not support type :" + clazz.getName());
        }

        String className = getGenClassName(clazz);

        ClassWriter cw = new ClassWriter();
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className, getType(ASMJavaBeanDeserializer.class), null);

        DeserializeBeanInfo beanInfo = DeserializeBeanInfo.computeSetters(clazz, type);

        _init(cw, new Context(className, config, beanInfo, 3));
        _createInstance(cw, new Context(className, config, beanInfo, 3));
        _deserialze(cw, new Context(className, config, beanInfo, 4));

        byte[] code = cw.toByteArray();

        /*org.apache.commons.io.IOUtils.write(code, new java.io.FileOutputStream(
        	"d:/"+ className + ".class"));*/

        Class<?> exampleClass = classLoader.defineClassPublic(className, code, 0, code.length);

        Constructor<?> constructor = exampleClass.getConstructor(PBDeserializer.class, Class.class);
        Object instance = constructor.newInstance(config, clazz);

        return (ObjectDeserializer) instance;
    }

    void _deserialze(ClassWriter cw, Context context) {
        if (context.getFieldInfoList().size() == 0) {
            return;
        }

        for (FieldInfo fieldInfo : context.getFieldInfoList()) {
            Class<?> fieldClass = fieldInfo.getFieldClass();
            Type fieldType = fieldInfo.getFieldType();

            if (fieldClass == char.class) {
                return;
            }

            if (Collection.class.isAssignableFrom(fieldClass)) {
                if (fieldType instanceof ParameterizedType) {
                    Type itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                    if (itemType instanceof Class) {
                        continue;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        Collections.sort(context.getFieldInfoList());

        MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "deserialze", "(" + getDesc(PBDeserializer.class)
                                                                    + getDesc(Type.class)
                                                                    + "Ljava/lang/Object;)Ljava/lang/Object;", null,
                                          null);
        
        //Label super_ = new Label();
        Label return_ = new Label();
        Label end_ = new Label();
        
        Constructor<?> defaultConstructor = context.getBeanInfo().getDefaultConstructor();

        // create instance
        if (context.getClazz().isInterface()) {
            mw.visitVarInsn(ALOAD, 0);
            mw.visitVarInsn(ALOAD, 1);
            mw.visitMethodInsn(INVOKESPECIAL, getType(ASMJavaBeanDeserializer.class), "createInstance",
                               "(" + getDesc(PBDeserializer.class) + ")Ljava/lang/Object;");
            mw.visitTypeInsn(CHECKCAST, getType(context.getClazz())); // cast
            mw.visitVarInsn(ASTORE, context.var("instance"));
        } else {
            if (defaultConstructor != null) {
                if (Modifier.isPublic(defaultConstructor.getModifiers())) {
                    mw.visitTypeInsn(NEW, getType(context.getClazz()));
                    mw.visitInsn(DUP);
                    mw.visitMethodInsn(INVOKESPECIAL, getType(context.getClazz()), "<init>", "()V");

                    mw.visitVarInsn(ASTORE, context.var("instance"));
                } else {
                    mw.visitVarInsn(ALOAD, 0);
                    mw.visitVarInsn(ALOAD, 1);
                    mw.visitMethodInsn(INVOKESPECIAL, getType(ASMJavaBeanDeserializer.class), "createInstance",
                                       "(" + getDesc(PBDeserializer.class) + ")Ljava/lang/Object;");
                    mw.visitTypeInsn(CHECKCAST, getType(context.getClazz())); // cast
                    mw.visitVarInsn(ASTORE, context.var("instance"));
                }
            } else {
                mw.visitInsn(ACONST_NULL);
                mw.visitTypeInsn(CHECKCAST, getType(context.getClazz())); // cast
                mw.visitVarInsn(ASTORE, context.var("instance"));
            }
        }
        
        {
            mw.visitVarInsn(ALOAD, 1); // parser
            mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "getContext",
                               "()Lcom/jd/glowworm/deserializer/ParseContext;");
            mw.visitVarInsn(ASTORE, context.var("context"));

            mw.visitVarInsn(ALOAD, 1); // parser
            mw.visitVarInsn(ALOAD, context.var("context"));
            mw.visitVarInsn(ALOAD, context.var("instance"));
            mw.visitVarInsn(ALOAD, 3); // fieldName
            mw.visitMethodInsn(INVOKEVIRTUAL,
                               getType(PBDeserializer.class),
                               "setContext",
                               "(Lcom/jd/glowworm/deserializer/ParseContext;Ljava/lang/Object;Ljava/lang/Object;)Lcom/jd/glowworm/deserializer/ParseContext;");
            mw.visitVarInsn(ASTORE, context.var("childContext"));
        }
        
        for (int i = 0, size = context.getFieldInfoList().size(); i < size; ++i) {
            FieldInfo fieldInfo = context.getFieldInfoList().get(i);
            Class<?> fieldClass = fieldInfo.getFieldClass();
            Type fieldType = fieldInfo.getFieldType();
            
            mw.visitVarInsn(ALOAD, 1);
            if (fieldClass == boolean.class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldBoolean", "()Z");
                mw.visitVarInsn(ISTORE, context.var(fieldInfo.getName() + "_asm"));

            } else if (fieldClass == byte.class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldInt", "()I");
                mw.visitVarInsn(ISTORE, context.var(fieldInfo.getName() + "_asm"));

            } else if (fieldClass == short.class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldInt", "()I");
                mw.visitVarInsn(ISTORE, context.var(fieldInfo.getName() + "_asm"));

            } else if (fieldClass == int.class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldInt", "()I");
                mw.visitVarInsn(ISTORE, context.var(fieldInfo.getName() + "_asm"));

            } else if (fieldClass == long.class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldlong", "()J");
                mw.visitVarInsn(LSTORE, context.var(fieldInfo.getName() + "_asm", 2));

            } else if (fieldClass == float.class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldFloat", "()F");
                mw.visitVarInsn(FSTORE, context.var(fieldInfo.getName() + "_asm"));

            } else if (fieldClass == double.class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldDouble", "()D");
                mw.visitVarInsn(DSTORE, context.var(fieldInfo.getName() + "_asm", 2));

            } else if (fieldClass == String.class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldString",
                                   "()Ljava/lang/String;");
                mw.visitVarInsn(ASTORE, context.var(fieldInfo.getName() + "_asm"));

            } else if (fieldClass == byte[].class) {
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldByteArray", "()[B");
                mw.visitVarInsn(ASTORE, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass.isEnum()) {
                mw.visitInsn(ACONST_NULL);
                mw.visitTypeInsn(CHECKCAST, getType(fieldClass)); // cast
                mw.visitVarInsn(ASTORE, context.var(fieldInfo.getName() + "_asm"));
                                
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldString",
                        "()Ljava/lang/String;");
                mw.visitMethodInsn(INVOKESTATIC, getType(fieldClass), "valueOf", "(Ljava/lang/String;)"
                                                                                 + getDesc(fieldClass));
                mw.visitVarInsn(ASTORE, context.var(fieldInfo.getName() + "_asm"));

            } else if (Collection.class.isAssignableFrom(fieldClass)) {

                Type actualTypeArgument = ((ParameterizedType) fieldType).getActualTypeArguments()[0];

                if (actualTypeArgument instanceof Class) {
                    Class<?> itemClass = (Class<?>) actualTypeArgument;

                    if (!Modifier.isPublic(itemClass.getModifiers())) {
                        throw new ASMException("can not create ASMParser");
                    }

                    /*if (itemClass == String.class) {
                        mw.visitLdcInsn(com.jd.glowworm.asm.Type.getType(getDesc(fieldClass))); // cast
                        mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanFieldStringList",
                                           "([Ljava/lang/Class;)" + getDesc(Collection.class));
                        mw.visitVarInsn(ASTORE, context.var(fieldInfo.getName() + "_asm"));
                    } else {*/
                        _deserialze_list_obj(context, mw, fieldInfo, fieldClass, itemClass);

                        /*if (i == size - 1) {
                            _deserialize_endCheck(context, mw, reset_);
                        }*/
                        continue;
                    //}
                } else {
                    throw new ASMException("can not create ASMParser");
                }

            } else {
                _deserialze_obj(context, mw, fieldInfo, fieldClass);

                /*if (i == size - 1) {
                    _deserialize_endCheck(context, mw, reset_);
                }*/

                continue;
            }
        }

        mw.visitLabel(end_);

        if (!context.getClazz().isInterface() && !Modifier.isAbstract(context.getClazz().getModifiers())) {
            if (defaultConstructor != null) {
                _batchSet(context, mw);
            } else {
                Constructor<?> creatorConstructor = context.getBeanInfo().getCreatorConstructor();
                if (creatorConstructor != null) {
                    mw.visitTypeInsn(NEW, getType(context.getClazz()));
                    mw.visitInsn(DUP);

                    _loadCreatorParameters(context, mw);

                    mw.visitMethodInsn(INVOKESPECIAL, getType(context.getClazz()), "<init>",
                                       getDesc(creatorConstructor));
                    mw.visitVarInsn(ASTORE, context.var("instance"));
                } else {
                    Method factoryMethod = context.getBeanInfo().getFactoryMethod();
                    if (factoryMethod != null) {
                        _loadCreatorParameters(context, mw);
                        mw.visitMethodInsn(INVOKESTATIC, getType(factoryMethod.getDeclaringClass()),
                                           factoryMethod.getName(), getDesc(factoryMethod));
                        mw.visitVarInsn(ASTORE, context.var("instance"));
                    } else {
                        throw new PBException("TODO");
                    }
                }
            }
        }

        mw.visitLabel(return_);
        
        _setContext(context, mw, true);
        mw.visitVarInsn(ALOAD, context.var("instance"));
        mw.visitInsn(ARETURN);
        
        /*mw.visitLabel(super_);
        mw.visitVarInsn(ALOAD, 0);
        mw.visitVarInsn(ALOAD, 1);
        mw.visitVarInsn(ALOAD, 2);
        mw.visitVarInsn(ALOAD, 3);
        mw.visitMethodInsn(INVOKESPECIAL, getType(ASMJavaBeanDeserializer.class), "deserialze",
                           "(" + getDesc(PBDeserializer.class) + getDesc(Type.class)
                                   + "Ljava/lang/Object;)Ljava/lang/Object;");
        mw.visitInsn(ARETURN);*/

        int maxStack = context.getVariantCount();
        Constructor<?> creatorConstructor = context.getBeanInfo().getCreatorConstructor();
        if (creatorConstructor != null) {
            int constructorTypeStack = 2;
            for (Class<?> type : creatorConstructor.getParameterTypes()) {
                if (type == long.class || type == double.class) {
                    constructorTypeStack += 2;
                } else {
                    constructorTypeStack++;
                }
            }
            if (maxStack < constructorTypeStack) {
                maxStack = constructorTypeStack;
            }
        } else {
            Method factoryMethod = context.getBeanInfo().getFactoryMethod();
            if (factoryMethod != null) {
                int paramStacks = 2;
                for (Class<?> type : factoryMethod.getParameterTypes()) {
                    if (type == long.class || type == double.class) {
                        paramStacks += 2;
                    } else {
                        paramStacks++;
                    }
                }
                if (maxStack < paramStacks) {
                    maxStack = paramStacks;
                }
            }
        }

        //System.out.println(maxStack+"#"+context.getVariantCount());
        mw.visitMaxs(maxStack, context.getVariantCount());
        mw.visitEnd();
    }

    private void _setContext(Context context, MethodVisitor mw, boolean setObject) {
        mw.visitVarInsn(ALOAD, 1); // parser
        mw.visitVarInsn(ALOAD, context.var("context"));
        mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "setContext",
                           "(Lcom/jd/glowworm/deserializer/ParseContext;)V");

        // TODO childContext is null
        if (setObject) {
            Label endIf_ = new Label();
            mw.visitVarInsn(ALOAD, context.var("childContext"));
            mw.visitJumpInsn(IFNULL, endIf_);

            mw.visitVarInsn(ALOAD, context.var("childContext"));
            mw.visitVarInsn(ALOAD, context.var("instance"));
            mw.visitMethodInsn(INVOKEVIRTUAL, getType(ParseContext.class), "setObject", "(Ljava/lang/Object;)V");

            mw.visitLabel(endIf_);
        }
    }
    
    private void _loadCreatorParameters(Context context, MethodVisitor mw) {
        List<FieldInfo> fieldInfoList = context.getBeanInfo().getFieldList();

        for (int i = 0, size = fieldInfoList.size(); i < size; ++i) {
            FieldInfo fieldInfo = fieldInfoList.get(i);
            Class<?> fieldClass = fieldInfo.getFieldClass();
            Type fieldType = fieldInfo.getFieldType();

            if (fieldClass == boolean.class) {
                mw.visitVarInsn(ILOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == byte.class) {
                mw.visitVarInsn(ILOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == short.class) {
                mw.visitVarInsn(ILOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == int.class) {
                mw.visitVarInsn(ILOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == long.class) {
                mw.visitVarInsn(LLOAD, context.var(fieldInfo.getName() + "_asm", 2));
            } else if (fieldClass == float.class) {
                mw.visitVarInsn(FLOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == double.class) {
                mw.visitVarInsn(DLOAD, context.var(fieldInfo.getName() + "_asm", 2));
            } else if (fieldClass == String.class) {
                mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass.isEnum()) {
                mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                Type itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                if (itemType == String.class) {
                    mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
                    mw.visitTypeInsn(CHECKCAST, getType(fieldClass)); // cast
                } else {
                    mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
                }
            } else {
                mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
            }
        }
    }

    private void _batchSet(Context context, MethodVisitor mw) {
        for (int i = 0, size = context.getFieldInfoList().size(); i < size; ++i) {
            FieldInfo fieldInfo = context.getFieldInfoList().get(i);
            Class<?> fieldClass = fieldInfo.getFieldClass();
            Type fieldType = fieldInfo.getFieldType();

            mw.visitVarInsn(ALOAD, context.var("instance"));
            if (fieldClass == boolean.class) {
                mw.visitVarInsn(ILOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == byte.class) {
                mw.visitVarInsn(ILOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == short.class) {
                mw.visitVarInsn(ILOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == int.class) {
                mw.visitVarInsn(ILOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == long.class) {
                mw.visitVarInsn(LLOAD, context.var(fieldInfo.getName() + "_asm", 2));
                mw.visitMethodInsn(INVOKEVIRTUAL, getType(context.getClazz()), fieldInfo.getMethod().getName(), "(J)V");
                continue;
            } else if (fieldClass == float.class) {
                mw.visitVarInsn(FLOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass == double.class) {
                mw.visitVarInsn(DLOAD, context.var(fieldInfo.getName() + "_asm", 2));
            } else if (fieldClass == String.class) {
                mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (fieldClass.isEnum()) {
                mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                Type itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
                if (itemType == String.class) {
                    mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
                    mw.visitTypeInsn(CHECKCAST, getType(fieldClass)); // cast
                } else {
                    mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
                    mw.visitTypeInsn(CHECKCAST, getType(fieldClass)); // cast
                }
            } else {
                mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
            }

            int INVAKE_TYPE;
            if (context.getClazz().isInterface()) {
                INVAKE_TYPE = INVOKEINTERFACE;
            } else {
                INVAKE_TYPE = INVOKEVIRTUAL;
            }
            if (fieldInfo.getMethod() != null) {
                mw.visitMethodInsn(INVAKE_TYPE, getType(fieldInfo.getDeclaringClass()),
                                   fieldInfo.getMethod().getName(), getDesc(fieldInfo.getMethod()));
                
                if (!fieldInfo.getMethod().getReturnType().equals(Void.TYPE)) {
                	mw.visitInsn(POP);
                }
            } else {
                mw.visitFieldInsn(PUTFIELD, getType(fieldInfo.getDeclaringClass()), fieldInfo.getField().getName(),
                                  getDesc(fieldInfo.getFieldClass()));
            }
        }
    }

    private void _deserialze_list_obj(Context context, MethodVisitor mw, FieldInfo fieldInfo,
                                      Class<?> fieldClass, Class<?> itemType) {        
        Label notNull_ = new Label();
        mw.visitVarInsn(ALOAD, 0);
        mw.visitFieldInsn(GETFIELD, context.getClassName(), fieldInfo.getName() + "_asm_list_item_deser__",
                          getDesc(ObjectDeserializer.class));
        mw.visitJumpInsn(IFNONNULL, notNull_);
        
        mw.visitVarInsn(ALOAD, 0);
        mw.visitVarInsn(ALOAD, 1);
        mw.visitLdcInsn(com.jd.glowworm.asm.Type.getType(getDesc(itemType)));
        mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "getDeserializer",
                           "(" + getDesc(Type.class) + ")" + getDesc(ObjectDeserializer.class));
        
        mw.visitFieldInsn(PUTFIELD, context.getClassName(), fieldInfo.getName() + "_asm_list_item_deser__",
                          getDesc(ObjectDeserializer.class));
        
        mw.visitLabel(notNull_);
    	
        if (fieldClass.isAssignableFrom(ArrayList.class)) {
            mw.visitTypeInsn(NEW, getType(ArrayList.class));
            mw.visitInsn(DUP);
            mw.visitMethodInsn(INVOKESPECIAL, getType(ArrayList.class), "<init>", "()V");
        } else if (fieldClass.isAssignableFrom(LinkedList.class)) {
            mw.visitTypeInsn(NEW, getType(LinkedList.class));
            mw.visitInsn(DUP);
            mw.visitMethodInsn(INVOKESPECIAL, getType(LinkedList.class), "<init>", "()V");
        } else if (fieldClass.isAssignableFrom(HashSet.class)) {
            mw.visitTypeInsn(NEW, getType(HashSet.class));
            mw.visitInsn(DUP);
            mw.visitMethodInsn(INVOKESPECIAL, getType(HashSet.class), "<init>", "()V");
        } else if (fieldClass.isAssignableFrom(TreeSet.class)) {
            mw.visitTypeInsn(NEW, getType(TreeSet.class));
            mw.visitInsn(DUP);
            mw.visitMethodInsn(INVOKESPECIAL, getType(TreeSet.class), "<init>", "()V");
        } else {
            mw.visitTypeInsn(NEW, getType(fieldClass));
            mw.visitInsn(DUP);
            mw.visitMethodInsn(INVOKESPECIAL, getType(fieldClass), "<init>", "()V");
        }
        
        //mw.visitInsn(ACONST_NULL);
        mw.visitTypeInsn(CHECKCAST, getType(fieldClass)); // cast
        mw.visitVarInsn(ASTORE, context.var(fieldInfo.getName() + "_asm"));
        
        mw.visitVarInsn(ALOAD, 1);
        mw.visitVarInsn(ALOAD, context.var(fieldInfo.getName() + "_asm"));
        mw.visitVarInsn(ALOAD, 0);
        mw.visitFieldInsn(GETFIELD, context.getClassName(), fieldInfo.getName() + "_asm_list_item_deser__",
                          getDesc(ObjectDeserializer.class));
        mw.visitLdcInsn(com.jd.glowworm.asm.Type.getType(getDesc(itemType)));
        mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "scanList",
                           "(Ljava/util/Collection;Lcom/jd/glowworm/deserializer/ObjectDeserializer;Ljava/lang/reflect/Type;)Ljava/util/Collection;");
        mw.visitVarInsn(ASTORE, context.var(fieldInfo.getName() + "_asm"));
    }

    private void _deserialze_obj(Context context, MethodVisitor mw, FieldInfo fieldInfo,
                                 Class<?> fieldClass) {
        Label notNull_ = new Label();
        mw.visitVarInsn(ALOAD, 0);
        mw.visitFieldInsn(GETFIELD, context.getClassName(), fieldInfo.getName() + "_asm_deser__",
                          getDesc(ObjectDeserializer.class));
        mw.visitJumpInsn(IFNONNULL, notNull_);
        
        mw.visitVarInsn(ALOAD, 0);
        mw.visitVarInsn(ALOAD, 1);
        mw.visitLdcInsn(com.jd.glowworm.asm.Type.getType(getDesc(fieldInfo.getFieldClass())));
        mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "getDeserializer",
                           "(" + getDesc(Type.class) + ")" + getDesc(ObjectDeserializer.class));

        mw.visitFieldInsn(PUTFIELD, context.getClassName(), fieldInfo.getName() + "_asm_deser__",
                          getDesc(ObjectDeserializer.class));

        mw.visitLabel(notNull_);
        
        mw.visitVarInsn(ALOAD, 1);
        mw.visitVarInsn(ALOAD, 0);
        mw.visitFieldInsn(GETFIELD, context.getClassName(), fieldInfo.getName() + "_asm_deser__",
                          getDesc(ObjectDeserializer.class));
        if (fieldInfo.getFieldType() instanceof Class) {
            mw.visitLdcInsn(com.jd.glowworm.asm.Type.getType(getDesc(fieldInfo.getFieldClass())));
        } else {
            mw.visitVarInsn(ALOAD, 0);
            mw.visitLdcInsn(fieldInfo.getName());
            mw.visitMethodInsn(INVOKEVIRTUAL, getType(ASMJavaBeanDeserializer.class), "getFieldType",
                               "(Ljava/lang/String;)Ljava/lang/reflect/Type;");
        }
        mw.visitLdcInsn(fieldInfo.getName());
        /*mw.visitMethodInsn(INVOKEINTERFACE, getType(ObjectDeserializer.class), "deserialze",
                           "(" + getDesc(PBDeserializer.class) + getDesc(Type.class)
                                   + "Ljava/lang/Object;)Ljava/lang/Object;");*/
        
        mw.visitMethodInsn(INVOKEVIRTUAL, getType(PBDeserializer.class), "doASMDeserializer",
                "(Lcom/jd/glowworm/deserializer/ObjectDeserializer;"
                +getDesc(Type.class)+"Ljava/lang/Object;)Ljava/lang/Object;");
        
        mw.visitTypeInsn(CHECKCAST, getType(fieldClass)); // cast
        mw.visitVarInsn(ASTORE, context.var(fieldInfo.getName() + "_asm"));
    }

    public FieldDeserializer createFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo)
                                                                                                               throws Exception {
        Class<?> fieldClass = fieldInfo.getFieldClass();

        if (fieldClass == int.class || fieldClass == long.class || fieldClass == String.class) {
            return createStringFieldDeserializer(mapping, clazz, fieldInfo);
        }

        FieldDeserializer fieldDeserializer = mapping.createFieldDeserializerWithoutASM(mapping, clazz, fieldInfo);
        return fieldDeserializer;
    }

    public FieldDeserializer createStringFieldDeserializer(PBDeserializer mapping, Class<?> clazz, FieldInfo fieldInfo)
                                                                                                                     throws Exception {
        Class<?> fieldClass = fieldInfo.getFieldClass();
        Method method = fieldInfo.getMethod();

        String className = getGenFieldDeserializer(clazz, fieldInfo);

        ClassWriter cw = new ClassWriter();
        Class<?> superClass;
        if (fieldClass == int.class) {
            superClass = IntegerFieldDeserializer.class;
        } else if (fieldClass == long.class) {
            superClass = LongFieldDeserializer.class;
        } else {
            superClass = StringFieldDeserializer.class;
        }

        int INVAKE_TYPE;
        if (clazz.isInterface()) {
            INVAKE_TYPE = INVOKEINTERFACE;
        } else {
            INVAKE_TYPE = INVOKEVIRTUAL;
        }

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className, getType(superClass), null);

        {
            MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + getDesc(PBDeserializer.class)
                                                                    + getDesc(Class.class) + getDesc(FieldInfo.class)
                                                                    + ")V", null, null);
            mw.visitVarInsn(ALOAD, 0);
            mw.visitVarInsn(ALOAD, 1);
            mw.visitVarInsn(ALOAD, 2);
            mw.visitVarInsn(ALOAD, 3);
            mw.visitMethodInsn(INVOKESPECIAL, getType(superClass), "<init>", "(" + getDesc(PBDeserializer.class)
                                                                             + getDesc(Class.class)
                                                                             + getDesc(FieldInfo.class) + ")V");

            mw.visitInsn(RETURN);
            mw.visitMaxs(4, 6);
            mw.visitEnd();
        }

        if (method != null) {
            if (fieldClass == int.class) {
                MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "setValue", "(" + getDesc(Object.class) + "I)V", null,
                                                  null);
                mw.visitVarInsn(ALOAD, 1);
                mw.visitTypeInsn(CHECKCAST, getType(method.getDeclaringClass())); // cast
                mw.visitVarInsn(ILOAD, 2);
                mw.visitMethodInsn(INVAKE_TYPE, getType(method.getDeclaringClass()), method.getName(), ASMUtils.getDesc(method));

                mw.visitInsn(RETURN);
                mw.visitMaxs(3, 3);
                mw.visitEnd();
            } else if (fieldClass == long.class) {
                MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "setValue", "(" + getDesc(Object.class) + "J)V", null,
                                                  null);
                mw.visitVarInsn(ALOAD, 1);
                mw.visitTypeInsn(CHECKCAST, getType(method.getDeclaringClass())); // cast
                mw.visitVarInsn(LLOAD, 2);
                mw.visitMethodInsn(INVAKE_TYPE, getType(method.getDeclaringClass()), method.getName(), ASMUtils.getDesc(method));

                mw.visitInsn(RETURN);
                mw.visitMaxs(3, 4);
                mw.visitEnd();
            } else {
                // public void setValue(Object object, Object value)
                MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "setValue", "(" + getDesc(Object.class)
                                                                          + getDesc(Object.class) + ")V", null, null);
                mw.visitVarInsn(ALOAD, 1);
                mw.visitTypeInsn(CHECKCAST, getType(method.getDeclaringClass())); // cast
                mw.visitVarInsn(ALOAD, 2);
                mw.visitTypeInsn(CHECKCAST, getType(fieldClass)); // cast
                mw.visitMethodInsn(INVAKE_TYPE, getType(method.getDeclaringClass()), method.getName(),
                		ASMUtils.getDesc(method));

                mw.visitInsn(RETURN);
                mw.visitMaxs(3, 3);
                mw.visitEnd();
            }
        }

        byte[] code = cw.toByteArray();

        Class<?> exampleClass = classLoader.defineClassPublic(className, code, 0, code.length);

        Constructor<?> constructor = exampleClass.getConstructor(PBDeserializer.class, Class.class, FieldInfo.class);
        Object instance = constructor.newInstance(mapping, clazz, fieldInfo);

        return (FieldDeserializer) instance;
    }

    static class Context {

        private int                       variantIndex = 5;

        private Map<String, Integer>      variants     = new HashMap<String, Integer>();

        private Class<?>                  clazz;
        private final DeserializeBeanInfo beanInfo;
        private String                    className;
        private List<FieldInfo>           fieldInfoList;

        public Context(String className, PBDeserializer config, DeserializeBeanInfo beanInfo, int initVariantIndex){
            this.className = className;
            this.clazz = beanInfo.getClazz();
            this.variantIndex = initVariantIndex;
            this.beanInfo = beanInfo;
            fieldInfoList = new ArrayList<FieldInfo>(beanInfo.getFieldList());
        }

        public String getClassName() {
            return className;
        }

        public List<FieldInfo> getFieldInfoList() {
            return fieldInfoList;
        }

        public DeserializeBeanInfo getBeanInfo() {
            return beanInfo;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public int getVariantCount() {
            return variantIndex;
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

        public int var(String name) {
            Integer i = variants.get(name);
            if (i == null) {
                variants.put(name, variantIndex++);
            }
            i = variants.get(name);
            return i.intValue();
        }
    }

    private void _init(ClassWriter cw, Context context) {
        for (int i = 0, size = context.getFieldInfoList().size(); i < size; ++i) {
            FieldInfo fieldInfo = context.getFieldInfoList().get(i);
            Class<?> fieldClass = fieldInfo.getFieldClass();

            if (fieldClass.isPrimitive()) {
                continue;
            }

            if (fieldClass.isEnum()) {

            } else if (Collection.class.isAssignableFrom(fieldClass)) {
                FieldVisitor fw = cw.visitField(ACC_PUBLIC, fieldInfo.getName() + "_asm_list_item_deser__",
                                                getDesc(ObjectDeserializer.class));
                fw.visitEnd();
            } else {
                FieldVisitor fw = cw.visitField(ACC_PUBLIC, fieldInfo.getName() + "_asm_deser__",
                                                getDesc(ObjectDeserializer.class));
                fw.visitEnd();
            }
        }

        MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + getDesc(PBDeserializer.class)
                                                                + getDesc(Class.class) + ")V", null, null);
        mw.visitVarInsn(ALOAD, 0);
        mw.visitVarInsn(ALOAD, 1);
        mw.visitVarInsn(ALOAD, 2);
        mw.visitMethodInsn(INVOKESPECIAL, getType(ASMJavaBeanDeserializer.class), "<init>",
                           "(" + getDesc(PBDeserializer.class) + getDesc(Class.class) + ")V");

        mw.visitVarInsn(ALOAD, 0);
        mw.visitFieldInsn(GETFIELD, getType(ASMJavaBeanDeserializer.class), "serializer",
                          getDesc(InnerJavaBeanDeserializer.class));
        mw.visitMethodInsn(INVOKEVIRTUAL, getType(JavaBeanDeserializer.class), "getFieldDeserializerMap",
                           "()" + getDesc(Map.class));
        mw.visitInsn(POP);

        mw.visitInsn(RETURN);
        mw.visitMaxs(4, 4);
        mw.visitEnd();
    }

    private void _createInstance(ClassWriter cw, Context context) {
        MethodVisitor mw = cw.visitMethod(ACC_PUBLIC, "createInstance", "(" + getDesc(PBDeserializer.class)
                                                                        + getDesc(Type.class) + ")Ljava/lang/Object;",
                                          null, null);

        mw.visitTypeInsn(NEW, getType(context.getClazz()));
        mw.visitInsn(DUP);
        mw.visitMethodInsn(INVOKESPECIAL, getType(context.getClazz()), "<init>", "()V");

        mw.visitInsn(ARETURN);
        mw.visitMaxs(3, 3);
        mw.visitEnd();
    }

}
