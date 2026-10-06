package com.community.eldercare.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

/**
 * 个案会商请求（新建/结论回填）。
 */
public class ConferenceRequest {

    private Long elderId;
    private Long planId;

    @NotBlank
    private String subject;
    private String location;
    private String situation;
    private String conclusion;
    private boolean planAdjusted;
    private String adjustmentNote;

    private List<ParticipantInput> participants = new ArrayList<>();

    public static class ParticipantInput {
        /** SOCIAL_WORKER / ELDER_ADVISOR / COMMUNITY_DOCTOR / FAMILY / GRID_WORKER / OTHER */
        private String role;
        private String name;
        private String phone;
        private boolean attended = true;
        private String opinion;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public boolean isAttended() { return attended; }
        public void setAttended(boolean attended) { this.attended = attended; }
        public String getOpinion() { return opinion; }
        public void setOpinion(String opinion) { this.opinion = opinion; }
    }

    public Long getElderId() { return elderId; }
    public void setElderId(Long elderId) { this.elderId = elderId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getSituation() { return situation; }
    public void setSituation(String situation) { this.situation = situation; }
    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public boolean isPlanAdjusted() { return planAdjusted; }
    public void setPlanAdjusted(boolean planAdjusted) { this.planAdjusted = planAdjusted; }
    public String getAdjustmentNote() { return adjustmentNote; }
    public void setAdjustmentNote(String adjustmentNote) { this.adjustmentNote = adjustmentNote; }
    public List<ParticipantInput> getParticipants() { return participants; }
    public void setParticipants(List<ParticipantInput> participants) { this.participants = participants; }
}
