package com.community.eldercare.dto;

/**
 * 家属接听情况登记（长期照护观察用）
 */
public class FamilyCallRequest {
    private Long contactId;
    private String contactName;
    private String contactPhone;
    private String relationship;
    private String channel;
    private boolean reached;
    private String attitude;
    private String note;

    public Long getContactId() { return contactId; }
    public void setContactId(Long contactId) { this.contactId = contactId; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public boolean isReached() { return reached; }
    public void setReached(boolean reached) { this.reached = reached; }
    public String getAttitude() { return attitude; }
    public void setAttitude(String attitude) { this.attitude = attitude; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
