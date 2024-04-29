package com.sharp.common.utils;

import java.util.HashMap;

import org.apache.ibatis.type.Alias;
import org.springframework.jdbc.support.JdbcUtils;

@SuppressWarnings("rawtypes")
@Alias("camelMap")
public class CamelMap extends HashMap {
    private static final long serialVersionUID = -7700790403928325865L;

    @SuppressWarnings("unchecked")
    @Override
    public Object put(Object key, Object value) {
        return super.put(JdbcUtils.convertUnderscoreNameToPropertyName((String) key), value);
    }
}