package com.example.ajohearresp.schema;

import android.util.Log;
/*
Author: Akhil Jose
UID: ajose12
 */
import java.util.Map;
import java.lang.reflect.Field;

public class MapToPojoMapper{

    public static <T> T mapToPojo(Map<String, Object> map, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        T pojo = clazz.newInstance();
        Field[] fields = clazz.getDeclaredFields();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String cleansed_key = key.toLowerCase().replace(' ', '_');
            Object value = entry.getValue();
            map.put(cleansed_key, value);
        }
        for (Field field : fields) {
            String fieldName = field.getName();
            Log.d("KEY", fieldName);
            if (map.containsKey(fieldName)) {
                Object value = map.get(fieldName);
                field.setAccessible(true);
                field.set(pojo, value);
            }
        }

        return pojo;
    }
}
