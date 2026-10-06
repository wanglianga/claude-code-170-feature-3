package com.community.eldercare.dto;

import java.util.List;

/**
 * 长期照护评估请求。
 * 夜间原因/到场结果汇总可留空，由系统按近期夜间事件自动汇总。
 */
public class AssessmentRequest {

    /** 触发评估的事件 id（可空） */
    private Long triggerEventId;
    /** 统计窗口天数，默认 7 */
    private Integer periodDays;

    private String nightReasonSummary;
    private String onsiteResultSummary;

    private boolean lonelyAnxious;
    private String emotionNote;
    private boolean medicationChaotic;
    private String medicationNote;

    /** GOOD / AVERAGE / POOR */
    private String familyInvolvement;
    private String familyNote;

    private String dailyLivingNote;
    private String chronicRiskNote;
    private String homeSafetyNote;

    /** LIGHT / MEDIUM / INTENSIVE */
    private String resultLevel;
    private String conclusion;
    /** 建议服务编码：REGULAR_VISIT/PSYCH_COMPANION/MEAL_SERVICE/FAMILY_DOCTOR/MED_REMINDER */
    private List<String> recommendations;

    public Long getTriggerEventId() { return triggerEventId; }
    public void setTriggerEventId(Long triggerEventId) { this.triggerEventId = triggerEventId; }
    public Integer getPeriodDays() { return periodDays; }
    public void setPeriodDays(Integer periodDays) { this.periodDays = periodDays; }
    public String getNightReasonSummary() { return nightReasonSummary; }
    public void setNightReasonSummary(String nightReasonSummary) { this.nightReasonSummary = nightReasonSummary; }
    public String getOnsiteResultSummary() { return onsiteResultSummary; }
    public void setOnsiteResultSummary(String onsiteResultSummary) { this.onsiteResultSummary = onsiteResultSummary; }
    public boolean isLonelyAnxious() { return lonelyAnxious; }
    public void setLonelyAnxious(boolean lonelyAnxious) { this.lonelyAnxious = lonelyAnxious; }
    public String getEmotionNote() { return emotionNote; }
    public void setEmotionNote(String emotionNote) { this.emotionNote = emotionNote; }
    public boolean isMedicationChaotic() { return medicationChaotic; }
    public void setMedicationChaotic(boolean medicationChaotic) { this.medicationChaotic = medicationChaotic; }
    public String getMedicationNote() { return medicationNote; }
    public void setMedicationNote(String medicationNote) { this.medicationNote = medicationNote; }
    public String getFamilyInvolvement() { return familyInvolvement; }
    public void setFamilyInvolvement(String familyInvolvement) { this.familyInvolvement = familyInvolvement; }
    public String getFamilyNote() { return familyNote; }
    public void setFamilyNote(String familyNote) { this.familyNote = familyNote; }
    public String getDailyLivingNote() { return dailyLivingNote; }
    public void setDailyLivingNote(String dailyLivingNote) { this.dailyLivingNote = dailyLivingNote; }
    public String getChronicRiskNote() { return chronicRiskNote; }
    public void setChronicRiskNote(String chronicRiskNote) { this.chronicRiskNote = chronicRiskNote; }
    public String getHomeSafetyNote() { return homeSafetyNote; }
    public void setHomeSafetyNote(String homeSafetyNote) { this.homeSafetyNote = homeSafetyNote; }
    public String getResultLevel() { return resultLevel; }
    public void setResultLevel(String resultLevel) { this.resultLevel = resultLevel; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
}
