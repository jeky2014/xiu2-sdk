package com.mengwangbao.xiu2.register;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * 设备注册请求模型类
 * 用于构建设备注册API的请求参数
 */
public class DeviceRegisterRequest {

    /**
     * JSON结构版本，当前固定为 "1.0"
     */
    @JsonProperty("schema_version")
    private String schemaVersion = "1.0";

    /**
     * 设备MAC地址，格式为 XX:XX:XX:XX:XX:XX
     */
    private String mac;

    /**
     * 设备IMEI号
     */
    private String imei;

    /**
     * SIM卡ICCID号
     */
    private String iccid;

    /**
     * 硬件版本号，格式为 X.X.X
     */
    @JsonProperty("hardware_version")
    private String hardwareVersion;

    /**
     * 软件/固件版本号，格式为 X.X.X
     */
    @JsonProperty("software_version")
    private String softwareVersion;

    /**
     * 设备所在经度，GCJ-02坐标系
     */
    private String longitude;

    /**
     * 设备所在纬度，GCJ-02坐标系
     */
    private String latitude;

    /**
     * 设备唯一序列号，用于身份标识
     */
    @JsonProperty("device_sn")
    private String deviceSn;

    /**
     * 毫秒级Unix时间戳，服务端会校验时间有效性
     */
    private long timestamp;

    /**
     * 防重放随机码，固定32位长度
     */
    private String nonce;
    
    /**
     * 构造函数，自动初始化timestamp和nonce
     */
    public DeviceRegisterRequest() {
        // 默认设置为当前毫秒级时间戳
        this.timestamp = System.currentTimeMillis();
        // 默认生成32位随机字符串作为nonce
        this.nonce = generateNonce();
    }

    // Getters and Setters
    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getHardwareVersion() {
        return hardwareVersion;
    }

    public void setHardwareVersion(String hardwareVersion) {
        this.hardwareVersion = hardwareVersion;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getDeviceSn() {
        return deviceSn;
    }

    public void setDeviceSn(String deviceSn) {
        this.deviceSn = deviceSn;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * 将请求对象转换为Map，用于生成签名
     * @return 包含所有有效字段的Map
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("schema_version", schemaVersion);
        map.put("mac", mac);
        map.put("imei", imei);
        map.put("iccid", iccid);
        map.put("hardware_version", hardwareVersion);
        map.put("software_version", softwareVersion);
        map.put("longitude", longitude);
        map.put("latitude", latitude);
        map.put("device_sn", deviceSn);
        map.put("timestamp", timestamp);
        map.put("nonce", nonce);
        return map;
    }

    /**
     * 生成32位随机字符串作为nonce
     * @return 32位随机字符串
     */
    /**
     * 生成32位随机字符串作为nonce
     * @return 32位随机字符串
     */
    public String generateNonce() {
        String characters = "ABCDEFabcdef0123456789";
        StringBuilder result = new StringBuilder(32);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < 32; i++) {
            result.append(characters.charAt(random.nextInt(characters.length())));
        }

        return result.toString();
    }

}
