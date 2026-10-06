package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 长期照护服务执行/签到记录。
 * 记录方案生效后每次服务的到场与完成情况：
 * 定期探访、心理陪伴、助餐签到、家庭医生随访、药盒提醒（含服药确认）、家属接听情况等。
 * 是平台持续观察“助餐签到、药盒提醒、家属接听率”变化的数据来源。
 */
@Getter
@Setter
@Entity
@Table(name = "care_activity", indexes = {
        @Index(name = "idx_activity_plan", columnList = "plan_id"),
        @Index(name = "idx_activity_time", columnList = "activity_at")
})
public class CareActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CarePlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    private CarePlanItem item;

    /** 活动类型，同 CarePlanItem.itemType；家属接听记录为 FAMILY_REACH */
    @Column(nullable = false)
    private String activityType;

    /** REGISTER 签到/服务完成 / CALL_PHONE 电话关怀 / MISSED 未签到/失约 */
    @Column(nullable = false)
    private String recordType = "REGISTER";

    /** 执行/登记人 */
    private String operatorName;
    /** 服务提供方 */
    private String providerName;

    /** 药盒提醒：是否已按时服药 */
    private Boolean medicationTaken;
    /** 家属联系：是否接通 */
    private Boolean familyReached;
    /** 家属联系人姓名 */
    private String familyContactName;

    /** 老人当次状态/情绪：平稳 / 焦虑 / 情绪低落 等 */
    private String elderMood;
    /** 当次夜间求助关联（如记录与某次求助相关） */
    @Column(length = 1000)
    private String content;
    @Column(length = 1000)
    private String note;

    private LocalDateTime activityAt = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
}
