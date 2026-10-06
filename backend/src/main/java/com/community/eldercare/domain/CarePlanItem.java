package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 长期照护方案服务项。
 * REGULAR_VISIT 定期探访 / PSYCH_COMPANION 心理陪伴 / MEAL_SERVICE 助餐 /
 * FAMILY_DOCTOR 家庭医生随访 / MED_REMINDER 药盒提醒 / OTHER 其他。
 */
@Getter
@Setter
@Entity
@Table(name = "care_plan_item")
public class CarePlanItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CarePlan plan;

    @Column(nullable = false)
    private String itemType;

    /** 服务频次说明：如 每周探访2次 / 每日助餐 / 每周心理陪伴1次 / 每月家庭医生随访1次 / 每日早晚药盒提醒 */
    @Column(length = 200)
    private String frequency;

    /** 责任方（社工/网格员/志愿者/助餐点/社区医生/养老顾问/家属） */
    private String providerName;
    private String providerPhone;

    /** 服务目标或备注 */
    @Column(length = 1000)
    private String targetNote;

    /** 计划服务总次数（用于签到率统计，可为空） */
    private Integer plannedTimes;

    /** 是否已完成签到次数 */
    private Integer checkInCount = 0;
}
