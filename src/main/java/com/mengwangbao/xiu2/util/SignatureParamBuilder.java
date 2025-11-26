package com.mengwangbao.xiu2.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 签名参数拼接工具类
 * 用于将请求参数按照签名规则进行筛选、排序和拼接
 *
 * @author mengwangbao
 * @version 1.0
 */
public class SignatureParamBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 构建待签名的参数字符串
     *
     * @param params 原始参数Map
     * @return 拼接后的签名字符串
     * @throws JsonProcessingException 当JSON序列化失败时抛出
     */
    public static String buildSignString(Map<String, Object> params) throws JsonProcessingException {
        if (params == null || params.isEmpty()) {
            return "";
        }

        // 步骤1: 筛选非空参数并格式化
        Map<String, String> filteredParams = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 跳过空值
            if (isEmptyValue(value)) {
                continue;
            }

            // 格式化值
            String formattedValue = formatValue(value);
            filteredParams.put(key, formattedValue);
        }

        // 步骤2: 按字段名字典序排序
        List<String> sortedKeys = filteredParams.keySet().stream()
                .sorted()
                .collect(Collectors.toList());

        // 步骤3: 拼接键值对
        List<String> paramPairs = new ArrayList<>();
        for (String key : sortedKeys) {
            paramPairs.add(key + "=" + filteredParams.get(key));
        }

        // 步骤4: 用&连接所有键值对
        return String.join("&", paramPairs);
    }

    /**
     * 判断值是否为空
     * 空的定义：null、空字符串、空数组、空Map/Collection
     * 注意：false 和 0 不算空值
     *
     * @param value 待检查的值
     * @return true表示为空，false表示非空
     */
    private static boolean isEmptyValue(Object value) {
        if (value == null) {
            return true;
        }

        if (value instanceof String) {
            return ((String) value).isEmpty();
        }

        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }

        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }

        if (value.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(value) == 0;
        }

        return false;
    }

    /**
     * 格式化参数值为字符串
     *
     * @param value 原始值
     * @return 格式化后的字符串
     * @throws JsonProcessingException 当JSON序列化失败时抛出
     */
    private static String formatValue(Object value) throws JsonProcessingException {
        if (value == null) {
            return "";
        }

        // 布尔值转小写字符串
        if (value instanceof Boolean) {
            return value.toString().toLowerCase();
        }

        // 数字直接转字符串
        if (value instanceof Number) {
            return value.toString();
        }

        // 数组、List、Map 转JSON字符串（紧凑格式，无空格）
        if (value instanceof Collection || value instanceof Map || value.getClass().isArray()) {
            return OBJECT_MAPPER.writeValueAsString(value);
        }

        // 其他类型直接toString
        return value.toString();
    }

    /**
     * 便捷方法：从对象构建签名字符串（使用反射）
     *
     * @param object 请求对象
     * @return 签名字符串
     * @throws JsonProcessingException 当JSON序列化失败时抛出
     */
    public static String buildSignStringFromObject(Object object) throws JsonProcessingException {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = OBJECT_MAPPER.convertValue(object, Map.class);
        return buildSignString(params);
    }
}
