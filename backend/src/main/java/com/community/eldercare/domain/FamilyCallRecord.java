package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 家属接听情况记录（长期照护观察用）。
 * 方案生效后社工/平台定期主动联系家属，记录拨打与接通，
 * 用于观察“家属接听率”是否随方案改善。
 */
@Getter
@Setter
@Entity
@Table(name = "family_call_record", indexes = {
        @Index(name = "idx_call_elder", columnList = "elder_id"),
        @Index(name = "idx_call_plan", columnList = "plan_id"),
        @Index(name = "idx_call_at", columnList = "called_at")
})
public class FamilyCallRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private LongTermCarePlan plan;

    @Column(nullable = false)
    private String contactName;
    private String contactPhone;
    private String relationship;

    /** 拨打方式：电话/视频/微信语音 */
    private String channel = "电话";

    /** 是否接通 */
    private boolean reached;
    /** 接通后家属态度/配合情况 */
    private String attitude;

    /** 本次沟通事项（是否知晓方案、是否愿意参与定期探访等） */
    @Column(length = 1000)
    private String note;

    private String operatorName;
    private LocalDateTime calledAt = LocalDateTime.now();
}
