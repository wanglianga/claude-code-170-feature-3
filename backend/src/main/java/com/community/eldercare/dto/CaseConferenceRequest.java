package com.community.eldercare.dto;

import java.time.LocalDateTime;

/**
 * 发起个案会商
 */
public class CaseConferenceRequest {
    private Long elderId;
    /** 可关联正在观察的长期照护方案 */
    private Long planId;
    private String reason;
    /** 参与方，逗号分隔，如 养老顾问-刘顾问,社区医生-钱医生,家属-周建国,网格员-赵网格员 */
    private String participants;
    private LocalDateTime scheduledAt;

    public Long getElderId() { return elderId; }
    public void setElderId(Long elderId) { this.elderId = elderId; }
    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getParticipants() { return participants; }
    public void setParticipants(String participants) { this.participants = participants; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
}
