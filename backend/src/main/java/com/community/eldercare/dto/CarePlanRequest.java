package com.community.eldercare.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 发起长期照护评估并制定方案（草稿）。
 * 一次提交评估维度、目标与安排的服务项，方案随后生效进入观察期。
 */
public class CarePlanRequest {

    @NotNull
    private Long elderId;

    private String triggerReason;
    private Integer lonelinessScore;
    private Integer medicationScore;
    private Integer familySupportScore;
    private Integer selfCareScore;
    private String assessmentConclusion;
    private String goals;
    private Integer observeDays = 30;

    private List<ItemDraft> items;

    public static class ItemDraft {
        /** REGULAR_VISIT 定期探访 / PSYCH_COMPANION 心理陪伴 / MEAL_SERVICE 助餐 / FAMILY_DOCTOR 家庭医生随访 */
        private String serviceType;
        private String providerName;
        private String providerPhone;
        /** 频次，如 每周2次 / 每日午餐 / 每两周随访 */
        private String frequency;
        private String content;
        private LocalDateTime scheduledStartAt;

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
        public LocalDateTime getScheduledStartAt() { return scheduledStartAt; }
        public void setScheduledStartAt(LocalDateTime scheduledStartAt) { this.scheduledStartAt = scheduledStartAt; }
    }

    public Long getElderId() { return elderId; }
    public void setElderId(Long elderId) { this.elderId = elderId; }
    public String getTriggerReason() { return triggerReason; }
    public void setTriggerReason(String triggerReason) { this.triggerReason = triggerReason; }
    public Integer getLonelinessScore() { return lonelinessScore; }
    public void setLonelinessScore(Integer lonelinessScore) { this.lonelinessScore = lonelinessScore; }
    public Integer getMedicationScore() { return medicationScore; }
    public void setMedicationScore(Integer medicationScore) { this.medicationScore = medicationScore; }
    public Integer getFamilySupportScore() { return familySupportScore; }
    public void setFamilySupportScore(Integer familySupportScore) { this.familySupportScore = familySupportScore; }
    public Integer getSelfCareScore() { return selfCareScore; }
    public void setSelfCareScore(Integer selfCareScore) { this.selfCareScore = selfCareScore; }
    public String getAssessmentConclusion() { return assessmentConclusion; }
    public void setAssessmentConclusion(String assessmentConclusion) { this.assessmentConclusion = assessmentConclusion; }
    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }
    public Integer getObserveDays() { return observeDays; }
    public void setObserveDays(Integer observeDays) { this.observeDays = observeDays; }
    public List<ItemDraft> getItems() { return items; }
    public void setItems(List<ItemDraft> items) { this.items = items; }
}
