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

    /** 本次求助原因（现场核实），逗号分隔 */
    private String helpReason;
    /** 到场结果 SCENE_RESOLVED / CALMED / TO_HOSPITAL / TEMP_CARE / FORWARD_SERVICE / NO_ENTRY */
    private String arriveResult;
    /** 是否孤独焦虑 */
    private boolean lonelyAnxious;
    private String anxietyNote;
    /** 是否药物管理混乱 */
    private boolean medicationChaos;
    private String medicationNote;
    /** 家属是否参与本次处置 */
    private boolean familyInvolved;
    private String familyInvolvementNote;

    private boolean familyContacted;
    private boolean hospitalNeeded;
    private boolean refuseMedical;
    private boolean temporaryCareNeeded;
    private String followServiceSuggestions;
    private String safetyHazards;
    private String note;

    public String getEntryMethod() { return entryMethod; }
    public void setEntryMethod(String entryMethod) { this.entryMethod = entryMethod; }
    public boolean isEntered() { return entered; }
    public void setEntered(boolean entered) { this.entered = entered; }
    public String getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(String photoUrls) { this.photoUrls = photoUrls; }
    public String getElderStatus() { return elderStatus; }
    public void setElderStatus(String elderStatus) { this.elderStatus = elderStatus; }
    public String getHelpReason() { return helpReason; }
    public void setHelpReason(String helpReason) { this.helpReason = helpReason; }
    public String getArriveResult() { return arriveResult; }
    public void setArriveResult(String arriveResult) { this.arriveResult = arriveResult; }
    public boolean isLonelyAnxious() { return lonelyAnxious; }
    public void setLonelyAnxious(boolean lonelyAnxious) { this.lonelyAnxious = lonelyAnxious; }
    public String getAnxietyNote() { return anxietyNote; }
    public void setAnxietyNote(String anxietyNote) { this.anxietyNote = anxietyNote; }
    public boolean isMedicationChaos() { return medicationChaos; }
    public void setMedicationChaos(boolean medicationChaos) { this.medicationChaos = medicationChaos; }
    public String getMedicationNote() { return medicationNote; }
    public void setMedicationNote(String medicationNote) { this.medicationNote = medicationNote; }
    public boolean isFamilyInvolved() { return familyInvolved; }
    public void setFamilyInvolved(boolean familyInvolved) { this.familyInvolved = familyInvolved; }
    public String getFamilyInvolvementNote() { return familyInvolvementNote; }
    public void setFamilyInvolvementNote(String familyInvolvementNote) { this.familyInvolvementNote = familyInvolvementNote; }
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
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
