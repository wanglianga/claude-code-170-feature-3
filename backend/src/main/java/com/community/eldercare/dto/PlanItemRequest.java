package com.community.eldercare.dto;

/**
 * 方案追加/调整服务项请求
 */
public class PlanItemRequest {
    /** REGULAR_VISIT / PSYCH_COMPANION / MEAL_SERVICE / FAMILY_DOCTOR */
    private String serviceType;
    private String providerName;
    private String providerPhone;
    private String frequency;
    private String content;

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public String getProviderPhone() { return providerPhone; }
    public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
