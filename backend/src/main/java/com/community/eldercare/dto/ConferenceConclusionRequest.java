package com.community.eldercare.dto;

/**
 * 个案会商结论（召开后填写，结论与行动安排写回老人档案）
 */
public class ConferenceConclusionRequest {
    private String conclusion;
    private String actionItems;
    /** 会商结论是否同时调整长期方案为 ADJUSTED */
    private Boolean adjustPlan;

    public String getConclusion() { return conclusion; }
    public void setConclusion(String conclusion) { this.conclusion = conclusion; }
    public String getActionItems() { return actionItems; }
    public void setActionItems(String actionItems) { this.actionItems = actionItems; }
    public Boolean getAdjustPlan() { return adjustPlan; }
    public void setAdjustPlan(Boolean adjustPlan) { this.adjustPlan = adjustPlan; }
}
