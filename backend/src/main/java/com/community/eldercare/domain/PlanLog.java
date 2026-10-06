package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 长期照护方案时间线：评估发起、方案生效、服务执行、助餐签到、药盒提醒、
 * 家属联系、阶段观察结论、个案会商等全过程留痕。
 */
@Getter
@Setter
@Entity
@Table(name = "plan_log", indexes = {
        @Index(name = "idx_planlog_plan", columnList = "plan_id")
})
public class PlanLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JsonIgnore
    private LongTermCarePlan plan;

    /** ASSESS 评估 / PLAN 方案生效 / SERVICE 服务执行 / MEAL 助餐 /
     *  MEDICATION 药盒提醒 / FAMILY 家属联系 / OBSERVE 阶段观察 / CONFERENCE 个案会商 / NOTE 备注 */
    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1000)
    private String content;

    private String operatorName;
    private LocalDateTime createdAt = LocalDateTime.now();
}
