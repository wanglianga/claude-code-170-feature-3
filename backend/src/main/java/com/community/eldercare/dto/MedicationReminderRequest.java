package com.community.eldercare.dto;

import java.time.LocalDateTime;

/**
 * 药盒提醒登记
 */
public class MedicationReminderRequest {
    private String medicineName;
    /** 早/中/晚/睡前 */
    private String dosageSlot;
    private LocalDateTime remindAt;
    /** PENDING 待确认 / CONFIRMED 已确认服药 / MISSED 漏服 */
    private String status;
    private String confirmSource;
    private String note;

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public String getDosageSlot() { return dosageSlot; }
    public void setDosageSlot(String dosageSlot) { this.dosageSlot = dosageSlot; }
    public LocalDateTime getRemindAt() { return remindAt; }
    public void setRemindAt(LocalDateTime remindAt) { this.remindAt = remindAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getConfirmSource() { return confirmSource; }
    public void setConfirmSource(String confirmSource) { this.confirmSource = confirmSource; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
