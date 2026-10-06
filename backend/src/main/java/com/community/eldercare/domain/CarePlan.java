package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 长期照护方案。
 * 基于长期照护评估制定，包含若干照护服务项（定期探访、心理陪伴、助餐、家庭医生随访、药盒提醒等）。
 * 生效后平台持续观察：夜间求助次数、助餐签到、药盒提醒、家属接听率变化。
 * 观察期内若无改善，社工可发起个案会商，协商结论写回老人档案。
 */
@Getter
@Setter
@Entity
@Table(name = "care_plan")
public class CarePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    @OneToOne(fetch = FetchType.LAZY)
    private CareAssessment assessment;

    /** 方案编号，如 CP20261006001 */
    @Column(nullable = false, unique = true)
    private String planNo;

    /** DRAFT 草案 / ACTIVE 生效观察中 / COMPLETED 观察期结束 / TERMINATED 提前终止 */
    @Column(nullable = false)
    private String status = "DRAFT";

    private String planName;

    /** 方案目标 */
    @Column(length = 1000)
    private String goal;

    /** 制定社工 */
    private String createdByName;
    private LocalDateTime createdAt = LocalDateTime.now();

    /** 生效时间 */
    private LocalDateTime effectiveAt;
    /** 观察周期（天），默认 28 天（4 周） */
    private Integer observeDays = 28;
    /** 观察周期结束时间 */
    private LocalDateTime observeEndAt;

    /** 最近一次效果复评时间 */
    private LocalDateTime lastReviewAt;
    /** 最近一次判定结果 IMPROVING 改善中 / NO_CHANGE 无明显变化 / WORSENING 未见改善且恶化 */
    private String lastReviewResult;
    @Column(length = 1000)
    private String lastReviewNote;

    /** 观察期起始快照：夜间求助/家属接听率，用于计算变化 */
    private Integer baselineNightCount;
    private Integer baselineFamilyReachRate;

    /** 个案会商后是否已调整 */
    private boolean reviewedAfterConference;
}
