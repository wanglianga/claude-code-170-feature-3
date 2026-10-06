package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 长期照护评估。
 * 触发场景：同一老人近一周多次夜间求助（FREQUENT_NIGHT_HELP），
 * 社工从事件中发起，综合夜间求助原因、到场结果、孤独焦虑、药物管理混乱与家属参与情况，
 * 给出长期照护结论与服务建议（定期探访 / 心理陪伴 / 助餐 / 家庭医生随访等）。
 */
@Getter
@Setter
@Entity
@Table(name = "care_assessment")
public class CareAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    /** 触发评估的求助事件（可为空，支持社工主动发起） */
    @ManyToOne(fetch = FetchType.LAZY)
    private HelpEvent triggerEvent;

    /** 评估人（社工） */
    private String assessorName;
    private LocalDateTime assessedAt = LocalDateTime.now();

    /** 统计窗口（天），默认近 7 天 */
    private Integer periodDays = 7;
    /** 窗口内夜间求助次数（发起时快照） */
    private Integer nightHelpCount = 0;

    /** 夜间求助主要原因汇总 */
    @Column(length = 1000)
    private String nightReasonSummary;
    /** 到场处置结果汇总 */
    @Column(length = 1000)
    private String onsiteResultSummary;

    /** 评估结论：是否孤独焦虑 */
    private boolean lonelyAnxious;
    @Column(length = 1000)
    private String emotionNote;

    /** 评估结论：是否药物管理混乱 */
    private boolean medicationChaotic;
    @Column(length = 1000)
    private String medicationNote;

    /** 家属参与情况：GOOD 参与良好 / AVERAGE 一般 / POOR 参与不足 */
    private String familyInvolvement;
    @Column(length = 1000)
    private String familyNote;

    /** 日常生活能力评估说明 */
    @Column(length = 1000)
    private String dailyLivingNote;
    /** 慢病/健康风险评估说明 */
    @Column(length = 1000)
    private String chronicRiskNote;
    /** 居家安全评估说明 */
    @Column(length = 1000)
    private String homeSafetyNote;

    /** 建议照护强度：LIGHT 轻度关怀 / MEDIUM 重点照护 / INTENSIVE 强化照护 */
    private String resultLevel;
    /** 评估总结意见 */
    @Column(length = 2000)
    private String conclusion;
    /** 建议服务（逗号分隔）：REGULAR_VISIT,PSYCH_COMPANION,MEAL_SERVICE,FAMILY_DOCTOR,MED_REMINDER */
    @Column(length = 500)
    private String recommendations;

    private LocalDateTime createdAt = LocalDateTime.now();
}
