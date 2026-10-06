package com.community.eldercare.dto;

import java.time.LocalDateTime;

/**
 * 长期服务执行登记（定期探访 / 心理陪伴 / 家庭医生随访）
 */
public class ServiceExecutionRequest {
    private Long itemId;
    /** 可直接指定类型（无对应服务项时） */
    private String serviceType;
    private String executorName;
    private String executorPhone;
    private String content;
    /** 家庭医生随访结论 */
    private String doctorConclusion;
    private LocalDateTime executedAt;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getExecutorName() { return executorName; }
    public void setExecutorName(String executorName) { this.executorName = executorName; }
    public String getExecutorPhone() { return executorPhone; }
    public void setExecutorPhone(String executorPhone) { this.executorPhone = executorPhone; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDoctorConclusion() { return doctorConclusion; }
    public void setDoctorConclusion(String doctorConclusion) { this.doctorConclusion = doctorConclusion; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}
