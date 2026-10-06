package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 个案会商。
 * 长期照护方案观察期内夜间求助等指标未见改善时，由社工发起，
 * 召集养老顾问、社区医生、家属等共同协商，结论写回老人档案并可据此调整照护方案。
 */
@Getter
@Setter
@Entity
@Table(name = "case_conference")
public class CaseConference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    private CarePlan plan;

    /** 会商主题 */
    @Column(nullable = false, length = 500)
    private String subject;

    /** 发起社工 */
    private String convenerName;
    private LocalDateTime heldAt = LocalDateTime.now();
    private String location;

    /** 会商前情况说明（夜间求助频次、方案执行情况等） */
    @Column(length = 2000)
    private String situation;

    /** 会商结论（协商一致意见） */
    @Column(length = 2000)
    private String conclusion;

    /** 是否调整照护方案 */
    private boolean planAdjusted;
    @Column(length = 1000)
    private String adjustmentNote;

    /** 协商结论是否已写回老人档案 */
    private boolean writtenBackToArchive;

    private LocalDateTime createdAt = LocalDateTime.now();
}
