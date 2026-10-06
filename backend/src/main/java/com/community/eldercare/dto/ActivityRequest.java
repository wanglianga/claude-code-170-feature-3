package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 照护服务执行/签到登记请求。
 */
public class ActivityRequest {

    /** REGULAR_VISIT / PSYCH_COMPANION / MEAL_SERVICE / FAMILY_DOCTOR / MED_REMINDER / FAMILY_REACH / OTHER */
    @NotBlank
    private String activityType;

    /** REGISTER 签到/服务完成 / CALL_PHONE 电话关怀 / MISSED 未签到失约，默认 REGISTER */
    private String recordType;

    private Long itemId;
    private String providerName;

    /** 药盒提醒：是否按时服药 */
    private Boolean medicationTaken;
    /** 家属联系：是否接通、联系人 */
    private Boolean familyReached;
    private String familyContactName;

    /** 老人状态/情绪 */
    private String elderMood;
    private String content;
    private String note;

    /** 服务时间，留空为当前时间（yyyy-MM-ddTHH:mm:ss） */
    private String activityAt;

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    public String getProviderName() { return providerName; }
    public void setProviderName(String providerName) { this.providerName = providerName; }
    public Boolean getMedicationTaken() { return medicationTaken; }
    public void setMedicationTaken(Boolean medicationTaken) { this.medicationTaken = medicationTaken; }
    public Boolean getFamilyReached() { return familyReached; }
    public void setFamilyReached(Boolean familyReached) { this.familyReached = familyReached; }
    public String getFamilyContactName() { return familyContactName; }
    public void setFamilyContactName(String familyContactName) { this.familyContactName = familyContactName; }
    public String getElderMood() { return elderMood; }
    public void setElderMood(String elderMood) { this.elderMood = elderMood; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getActivityAt() { return activityAt; }
    public void setActivityAt(String activityAt) { this.activityAt = activityAt; }
}
