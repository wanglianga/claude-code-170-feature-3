package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 药盒提醒记录。针对药物管理混乱老人，平台/智能药盒按点提醒服药，
 * 老人确认（或家属/社工代确认）后闭环；未确认计为漏服，纳入方案效果观察。
 */
@Getter
@Setter
@Entity
@Table(name = "medication_reminder", indexes = {
        @Index(name = "idx_med_elder", columnList = "elder_id"),
        @Index(name = "idx_med_plan", columnList = "plan_id"),
        @Index(name = "idx_med_remind", columnList = "remind_at")
})
public class MedicationReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private LongTermCarePlan plan;

    /** 药品名称，如 降压药、二甲双胍 */
    @Column(nullable = false)
    private String medicineName;

    /** 药次：早/中/晚/睡前 */
    private String dosageSlot = "早";

    /** 计划提醒时间 */
    @Column(nullable = false)
    private LocalDateTime remindAt = LocalDateTime.now();

    /** PENDING 待确认 / CONFIRMED 已确认服药 / MISSED 漏服 */
    @Column(nullable = false)
    private String status = "PENDING";

    private LocalDateTime confirmedAt;
    /** 确认方式：老人按键确认 / 家属确认 / 社工上门确认 */
    private String confirmSource;

    @Column(length = 1000)
    private String note;

    private LocalDateTime createdAt = LocalDateTime.now();
}
