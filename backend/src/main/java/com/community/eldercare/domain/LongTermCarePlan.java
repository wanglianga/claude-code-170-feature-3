package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 长期照护方案。
 * 社工对频繁夜间求助老人发起长期照护评估后制定，安排定期探访、心理陪伴、
 * 助餐、家庭医生随访等长期服务；生效后平台持续观察夜间求助、助餐签到、
 * 药盒提醒与家属接听率的变化；若无改善可发起个案会商。
 */
@Getter
@Setter
@Entity
@Table(name = "long_term_care_plan", indexes = {
        @Index(name = "idx_plan_elder", columnList = "elder_id"),
        @Index(name = "idx_plan_status", columnList = "status")
})
public class LongTermCarePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 方案编号 P20261006001 */
    @Column(nullable = false, unique = true)
    private String planNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    /** DRAFT 评估中（方案待生效） / ACTIVE 生效中 / ADJUSTED 已调整 / ENDED 已结束 */
    @Column(nullable = false)
    private String status = "DRAFT";

    /* ---------------- 长期照护评估 ---------------- */

    /** 发起评估的社工 */
    private String initiatorName;
    private LocalDateTime initiatedAt = LocalDateTime.now();

    /** 触发原因，如 近30天夜间求助 5 次 */
    @Column(length = 1000)
    private String triggerReason;

    /** 评估维度：孤独焦虑程度 */
    private Integer lonelinessScore;
    /** 药物管理能力评分（越混乱越低） */
    private Integer medicationScore;
    /** 家属支持评分 */
    private Integer familySupportScore;
    /** 生活自理能力评分 */
    private Integer selfCareScore;

    /** 评估结论 */
    @Column(length = 2000)
    private String assessmentConclusion;

    /* ---------------- 方案目标与观察基线 ---------------- */

    /** 方案目标 */
    @Column(length = 2000)
    private String goals;

    /** 观察期（天），默认 30 天 */
    private Integer observeDays = 30;

    /** 基线：生效前 30 天夜间求助次数（评估发起时快照） */
    private Integer baselineNightHelps;
    /** 基线：家属接听率（事件家属沟通记录） */
    private Integer baselineFamilyAnswerRate;
    /** 基线：助餐签到次数（观察前 30 天） */
    private Integer baselineMealCheckins;
    /** 基线：药盒提醒确认次数（观察前 30 天） */
    private Integer baselineMedReminders;

    /* ---------------- 生效 / 结束 ---------------- */

    private LocalDateTime effectiveAt;
    private LocalDateTime endedAt;
    /** 结束/调整原因 */
    @Column(length = 1000)
    private String endReason;

    /** 关联的个案会商（若无改善发起会商） */
    @ManyToOne(fetch = FetchType.LAZY)
    private CaseConference lastConference;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
