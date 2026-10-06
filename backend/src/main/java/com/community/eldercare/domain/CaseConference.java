package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

/**
 * 个案会商。长期照护方案观察期内夜间求助等指标没有改善时，由社工发起，
 * 召集养老顾问、社区医生、家属等共同协商；会商结论与行动安排写回老人档案。
 */
@Getter
@Setter
@Entity
@Table(name = "case_conference", indexes = {
        @Index(name = "idx_conf_elder", columnList = "elder_id"),
        @Index(name = "idx_conf_plan", columnList = "plan_id")
})
public class CaseConference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 会商编号 C20261006001 */
    @Column(nullable = false, unique = true)
    private String conferenceNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private LongTermCarePlan plan;

    /** 召集社工 */
    private String convenerName;

    /** 发起原因，如 方案生效30天夜间求助未下降 */
    @Column(length = 1000)
    private String reason;

    /** 参与方：养老顾问、社区医生、家属、网格员等（逗号分隔姓名/角色） */
    @Column(length = 1000)
    private String participants;

    /** SCHEDULED 已发起待召开 / HELD 已召开形成结论 */
    @Column(nullable = false)
    private String status = "SCHEDULED";

    private LocalDateTime scheduledAt;
    private LocalDateTime heldAt;

    /** 协商结论 */
    @Column(length = 3000)
    private String conclusion;

    /** 后续行动安排（调整探访频次、家属轮值、家庭医生上门、转介养老顾问等） */
    @Column(length = 3000)
    private String actionItems;

    /** 结论是否已写回老人档案 */
    private boolean writtenBackToProfile;

    private LocalDateTime createdAt = LocalDateTime.now();
}
