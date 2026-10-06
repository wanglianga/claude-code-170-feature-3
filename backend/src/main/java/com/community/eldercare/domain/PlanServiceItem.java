package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 长期照护方案下的定期服务安排与执行记录。
 * 类型：REGULAR_VISIT 定期探访 / PSYCH_COMPANION 心理陪伴 /
 *      MEAL_SERVICE 助餐 / FAMILY_DOCTOR 家庭医生随访。
 */
@Getter
@Setter
@Entity
@Table(name = "plan_service_item", indexes = {
        @Index(name = "idx_item_plan", columnList = "plan_id")
})
public class PlanServiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnore
    private LongTermCarePlan plan;

    /** REGULAR_VISIT / PSYCH_COMPANION / MEAL_SERVICE / FAMILY_DOCTOR */
    @Column(nullable = false)
    private String serviceType;

    /** 负责人/提供方，如 赵网格员、康泰助餐点、钱医生 */
    @Column(nullable = false)
    private String providerName;
    private String providerPhone;

    /** 频次说明：每周2次 / 每日午餐 / 每两周随访 */
    @Column(nullable = false)
    private String frequency;

    /** 计划开始日期（用 LocalDateTime 存首次安排时间） */
    private LocalDateTime scheduledStartAt = LocalDateTime.now();

    /** 安排说明 */
    @Column(length = 1000)
    private String content;

    /** ACTIVE 执行中 / PAUSED 暂停 / DONE 已结束 */
    @Column(nullable = false)
    private String status = "ACTIVE";

    /** 累计执行次数（执行登记时累加；助餐签到另有签到明细） */
    private Integer executedCount = 0;

    private LocalDateTime lastExecutedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
