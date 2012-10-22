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
package com.jd.glowworm;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.jd.glowworm.deserializer.PBDeserializer;
import com.jd.glowworm.util.TypeUtils;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public class PBMapbject implements Map<String, Object>, Cloneable, Serializable, InvocationHandler {

    private static final long         serialVersionUID         = 1L;
    private static final int          DEFAULT_INITIAL_CAPACITY = 16;

    private final Map<String, Object> map;
    
    public PBMapbject(Map<String, Object> map){
        this.map = map;
    }
    
    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Object get(Object key) {
        return map.get(key);
    }
    
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
    }

    public void clear() {
        map.clear();
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Collection<Object> values() {
        return map.values();
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Object clone() {
        return new PBMapbject(new HashMap<String, Object>(map));
    }

    public boolean equals(Object obj) {
        return this.map.equals(obj);
    }

    public int hashCode() {
        return this.map.hashCode();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            Class<?> returnType = method.getReturnType();
            if (returnType != void.class) {
                throw new PBException("illegal setter");
            }

            String name = null;
            
            if (name == null) {
                name = method.getName();
                if (!name.startsWith("set")) {
                    throw new PBException("illegal setter");
                }

                name = name.substring(3);
                if (name.length() == 0) {
                    throw new PBException("illegal setter");
                }
                name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            }

            map.put(name, args[0]);
            return null;
        }

        if (parameterTypes.length == 0) {
            Class<?> returnType = method.getReturnType();
            if (returnType == void.class) {
                throw new PBException("illegal getter");
            }

            String name = null;
            if (name == null) {
                name = method.getName();
                if (name.startsWith("get")) {
                    name = name.substring(3);
                    if (name.length() == 0) {
                        throw new PBException("illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                } else if (name.startsWith("is")) {
                    name = name.substring(2);
                    if (name.length() == 0) {
                        throw new PBException("illegal getter");
                    }
                    name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
                } else {
                    throw new PBException("illegal getter");
                }
            }
            
            Object value = map.get(name);
            return TypeUtils.cast(value, method.getGenericReturnType(), PBDeserializer.getGlobalInstance());
        }

        throw new UnsupportedOperationException(method.toGenericString());
    }
}
