package com.mengwangbao.xiu2.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * SignatureParamBuilder 单元测试
 */
public class SignatureParamBuilderTest {

    @Test
    public void testBuildSignString_withSampleData() throws JsonProcessingException {
        // 构造测试数据
        Map<String, Object> params = new HashMap<String, Object>();
//        params.put("schema_version", "1.0");
        params.put("mac", "00:11:22:33:44:55");
        params.put("imei", "");
        params.put("iccid", null);
        params.put("hardware_version", "1.2.3");
        params.put("software_version", "2.1.0");
        params.put("longitude", "116.4074");
        params.put("latitude", "39.9042");
        params.put("device_sn", "xxxx10000000000b001");
        params.put("timestamp", 1763631389011L);
        params.put("nonce", "b5df8e7a6b9c54e4d9e8a6b9c54e4d9e");
        params.put("optional_field", null);

        String signString = SignatureParamBuilder.buildSignString(params);

        String expected = "device_sn=xxxx10000000000b001&hardware_version=1.2.3&latitude=39.9042&longitude=116.4074&mac=00:11:22:33:44:55&nonce=b5df8e7a6b9c54e4d9e8a6b9c54e4d9e&schema_version=1.0&software_version=2.1.0&timestamp=1763631389011";

        assertEquals(expected, signString);
        System.out.println("生成的签名字符串：");
        System.out.println(signString);
    }

    @Test
    public void testBuildSignString_withBooleanAndZero() throws JsonProcessingException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("enabled", false);
        params.put("count", 0);
        params.put("name", "test");

        String signString = SignatureParamBuilder.buildSignString(params);

        assertTrue(signString.contains("enabled=false"));
        assertTrue(signString.contains("count=0"));
        assertTrue(signString.contains("name=test"));
    }

    @Test
    public void testBuildSignString_withArrayAndMap() throws JsonProcessingException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("tags", Arrays.asList("tag1", "tag2"));

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");
        params.put("metadata", metadata);

        params.put("empty_list", new ArrayList<Object>());
        params.put("empty_map", new HashMap<String, Object>());

        String signString = SignatureParamBuilder.buildSignString(params);

        assertTrue(signString.contains("tags="));
        assertTrue(signString.contains("metadata="));
        assertFalse(signString.contains("empty_list"));
        assertFalse(signString.contains("empty_map"));

        System.out.println("包含数组和对象的签名字符串：");
        System.out.println(signString);
    }

    @Test
    public void testBuildSignString_emptyParams() throws JsonProcessingException {
        String signString = SignatureParamBuilder.buildSignString(new HashMap<String, Object>());
        assertEquals("", signString);
    }

    @Test
    public void testBuildSignString_nullParams() throws JsonProcessingException {
        String signString = SignatureParamBuilder.buildSignString(null);
        assertEquals("", signString);
    }
}
