package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 长期照护服务执行记录（定期探访 / 心理陪伴 / 家庭医生随访）。
 * 每次社工/志愿者/医生完成一次安排后登记，作为方案落实与效果观察依据。
 */
@Getter
@Setter
@Entity
@Table(name = "plan_service_execution", indexes = {
        @Index(name = "idx_exec_plan", columnList = "plan_id"),
        @Index(name = "idx_exec_elder", columnList = "elder_id")
})
public class PlanServiceExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnore
    private LongTermCarePlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private PlanServiceItem item;

    /** REGULAR_VISIT / PSYCH_COMPANION / FAMILY_DOCTOR */
    @Column(nullable = false)
    private String serviceType;

    @Column(nullable = false)
    private String executorName;
    private String executorPhone;

    /** 本次服务情况 / 老人状态变化 */
    @Column(length = 2000)
    private String content;

    /** 家庭医生随访结论（随访时填写，同步写回老人档案） */
    @Column(length = 1000)
    private String doctorConclusion;

    private LocalDateTime executedAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
}
