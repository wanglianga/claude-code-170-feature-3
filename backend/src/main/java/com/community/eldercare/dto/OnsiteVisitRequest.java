package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 上门核实请求
 */
public class OnsiteVisitRequest {

    @NotBlank
    private String entryMethod;
    private boolean entered;
    private String photoUrls;
    @NotBlank
    private String elderStatus;
    private boolean familyContacted;
    private boolean hospitalNeeded;
    private boolean refuseMedical;
    private boolean temporaryCareNeeded;
    private String followServiceSuggestions;
    private String safetyHazards;
    /** 是否孤独焦虑、情绪表现说明 */
    private boolean lonelyAnxious;
    private String emotionNote;
    /** 是否药物管理混乱、用药情况说明 */
    private boolean medicationChaotic;
    private String medicationNote;
    private String note;

    public String getEntryMethod() { return entryMethod; }
    public void setEntryMethod(String entryMethod) { this.entryMethod = entryMethod; }
    public boolean isEntered() { return entered; }
    public void setEntered(boolean entered) { this.entered = entered; }
    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public String getElderStatus() { return elderStatus; }
    public void setElderStatus(String elderStatus) { this.elderStatus = elderStatus; }
    public boolean isFamilyContacted() { return familyContacted; }
    public void setFamilyContacted(boolean familyContacted) { this.familyContacted = familyContacted; }
    public boolean isHospitalNeeded() { return hospitalNeeded; }
    public void setHospitalNeeded(boolean hospitalNeeded) { this.hospitalNeeded = hospitalNeeded; }
    public boolean isRefuseMedical() { return refuseMedical; }
    public void setRefuseMedical(boolean refuseMedical) { this.refuseMedical = refuseMedical; }
    public boolean isTemporaryCareNeeded() { return temporaryCareNeeded; }
    public void setTemporaryCareNeeded(boolean temporaryCareNeeded) { this.temporaryCareNeeded = temporaryCareNeeded; }
    public String getFollowServiceSuggestions() { return followServiceSuggestions; }
    public void setFollowServiceSuggestions(String followServiceSuggestions) { this.followServiceSuggestions = followServiceSuggestions; }
    public String getSafetyHazards() { return safetyHazards; }
    public void setSafetyHazards(String safetyHazards) { this.safetyHazards = safetyHazards; }
    public boolean isLonelyAnxious() { return lonelyAnxious; }
    public void setLonelyAnxious(boolean lonelyAnxious) { this.lonelyAnxious = lonelyAnxious; }
    public String getEmotionNote() { return emotionNote; }
    public void setEmotionNote(String emotionNote) { this.emotionNote = emotionNote; }
    public boolean isMedicationChaotic() { return medicationChaotic; }
    public void setMedicationChaotic(boolean medicationChaotic) { this.medicationChaotic = medicationChaotic; }
    public String getMedicationNote() { return medicationNote; }
    public void setMedicationNote(String medicationNote) { this.medicationNote = medicationNote; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
