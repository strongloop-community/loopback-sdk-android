package com.strongloop.android.remoting;

import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanUtil {
    public static void setProperties(Object object, Map<String, ? extends Object> properties, boolean includeSuperClasses) {
        if (object == null || properties == null) {
            return;
        }

        Class<?> objectClass = object.getClass();

        for (Map.Entry<String, ? extends Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null) continue;
            if (key.length() == 0) continue;

            String setterName = "set" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
            Method setter = null;

            // Try to use the exact setter
            if (value != null) {
                try {
                    if (includeSuperClasses) {
                        setter = objectClass.getMethod(setterName, value.getClass());
                    } else {
                        setter = objectClass.getDeclaredMethod(setterName, value.getClass());
                    }
                } catch (Exception ex) {
                }
            }

            // Find a more generic setter
            if (setter == null) {
                Method[] methods = includeSuperClasses ? objectClass.getMethods() : objectClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(setterName)) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length == 1 && isAssignableFrom(parameterTypes[0], value)) {
                            setter = method;
                            break;
                        }
                    }
                }
            }

            // Invoke
            if (setter != null) {
                if (setter.getAnnotation(Transient.class) != null) continue;

                try {
                    setter.invoke(object, value);
                } catch (Exception e) {
                    Log.e("BeanUtil", setterName + "() failed", e);
                }
            }
        }
    }

    public static Map<String, Object> getProperties(Object object, boolean includeSuperClasses, boolean deepCopy) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        if (object == null) {
            return map;
        }

        Class<?> objectClass = object.getClass();
        Method[] methods = includeSuperClasses ? objectClass.getMethods() : objectClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getDeclaringClass() == java.lang.Object.class) continue;
            if (method.getParameterTypes().length > 0) continue;
            if (method.getReturnType().equals(Void.TYPE)) continue;
            if (method.getAnnotation(Transient.class) != null) continue;

            String methodName = method.getName();
            String propertyName = "";
            if (methodName.startsWith("get")) {
                propertyName = methodName.substring(3);
            } else if (methodName.startsWith("is")) {
                propertyName = methodName.substring(2);
            }
            if (propertyName.length() > 0 && Character.isUpperCase(propertyName.charAt(0))) {
                propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);

                Object value = null;
                try {
                    value = method.invoke(object);
                } catch (Exception e) {
                    Log.e("BeanUtil", method.getName() + "() failed", e);
                }

                if (!deepCopy) {
                    map.put(propertyName, value);
                } else {
                    if (isSimpleObject(value)) {
                        map.put(propertyName, value);
                    } else if (value instanceof Map) {
                        Map<String, Object> submap = new HashMap<String, Object>();
                        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                            submap.put(String.valueOf(entry.getKey()), convertObject(entry.getValue(), includeSuperClasses));
                        }
                        map.put(propertyName, submap);
                    } else if (value instanceof Iterable) {
                        List<Object> sublist = new ArrayList<Object>();
                        for (Object v : (Iterable<?>) object) {
                            sublist.add(convertObject(v, includeSuperClasses));
                        }
                        map.put(propertyName, sublist);
                    } else if (value.getClass().isArray()) {
                        List<Object> sublist = new ArrayList<Object>();
                        int length = Array.getLength(value);
                        for (int i = 0; i < length; i++) {
                            sublist.add(convertObject(Array.get(value, i), includeSuperClasses));
                        }
                        map.put(propertyName, sublist);
                    } else {
                        map.put(propertyName, getProperties(value, includeSuperClasses, deepCopy));
                    }
                }
            }
        }

        return map;
    }

    private static boolean isAssignableFrom(Class<?> parameterType, Object value) {
        if (parameterType.isPrimitive()) {
            if (value == null) {
                return false;
            }
            Class<?> valueClass = value.getClass();

            if (parameterType == Boolean.TYPE) {
                return valueClass == Boolean.class;
            }
            else if (parameterType == Byte.TYPE) {
                return valueClass == Byte.class;
            }
            else if (parameterType == Character.TYPE) {
                return valueClass == Character.class;
            }
            else if (parameterType == Short.TYPE) {
                return valueClass == Short.class || valueClass == Byte.class;
            }
            else if (parameterType == Integer.TYPE) {
                return valueClass == Integer.class || valueClass == Character.class || valueClass == Short.class || valueClass == Byte.class;
            }
            else if (parameterType == Long.TYPE) {
                return valueClass == Long.class || valueClass == Integer.class || valueClass == Character.class || valueClass == Short.class || valueClass == Byte.class;
            }
            else if (parameterType == Float.TYPE) {
                return valueClass == Float.class || valueClass == Long.class || valueClass == Integer.class || valueClass == Character.class || valueClass == Short.class || valueClass == Byte.class;
            }
            else if (parameterType == Double.TYPE) {
                return valueClass == Double.class || valueClass == Float.class || valueClass == Long.class || valueClass == Integer.class || valueClass == Character.class || valueClass == Short.class || valueClass == Byte.class;
            }
            else {
                return false;
            }
        }
        else {
            return value == null || parameterType.isAssignableFrom(value.getClass());
        }
    }

    private static boolean isSimpleObject(Object object) {
        return object == null || isSimpleObjectClass(object.getClass());
    }

    private static boolean isSimpleObjectClass(Class<?> objectClass) {
        if (objectClass.isArray()) {
            return isSimpleObjectClass(objectClass.getComponentType());
        }
        else {
            return objectClass.isPrimitive() ||
                    CharSequence.class.isAssignableFrom(objectClass) ||
                    Character.class.isAssignableFrom(objectClass) ||
                    Boolean.class.isAssignableFrom(objectClass) ||
                    Number.class.isAssignableFrom(objectClass);
        }
    }

    private static Object convertObject(Object object, boolean includeSuperClasses) {
        if (isSimpleObject(object)) {
            return object;
        }
        else {
            return getProperties(object, includeSuperClasses, true);
        }
    }
}
