package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 长期照护方案制定请求。
 */
public class PlanRequest {

    @NotNull
    private Long elderId;
    private Long assessmentId;

    @NotBlank
    private String planName;
    private String goal;
    /** 观察周期天数，默认 28 */
    private Integer observeDays;
    /** true 则保存后立即生效 */
    private boolean activate;

    private List<ItemInput> items = new ArrayList<>();

    public static class ItemInput {
        /** REGULAR_VISIT / PSYCH_COMPANION / MEAL_SERVICE / FAMILY_DOCTOR / MED_REMINDER / OTHER */
        private String itemType;
        private String frequency;
        private String providerName;
        private String providerPhone;
        private String targetNote;
        private Integer plannedTimes;

        public String getItemType() { return itemType; }
        public void setItemType(String itemType) { this.itemType = itemType; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public String getProviderName() { return providerName; }
        public void setProviderName(String providerName) { this.providerName = providerName; }
        public String getProviderPhone() { return providerPhone; }
        public void setProviderPhone(String providerPhone) { this.providerPhone = providerPhone; }
        public String getTargetNote() { return targetNote; }
        public void setTargetNote(String targetNote) { this.targetNote = targetNote; }
        public Integer getPlannedTimes() { return plannedTimes; }
        public void setPlannedTimes(Integer plannedTimes) { this.plannedTimes = plannedTimes; }
    }

    public Long getElderId() { return elderId; }
    public void setElderId(Long elderId) { this.elderId = elderId; }
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public Integer getObserveDays() { return observeDays; }
    public void setObserveDays(Integer observeDays) { this.observeDays = observeDays; }
    public boolean isActivate() { return activate; }
    public void setActivate(boolean activate) { this.activate = activate; }
    public List<ItemInput> getItems() { return items; }
    public void setItems(List<ItemInput> items) { this.items = items; }
}
