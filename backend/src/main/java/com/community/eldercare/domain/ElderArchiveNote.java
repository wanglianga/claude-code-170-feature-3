package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 老人档案留痕（档案化的重要照护记录）。
 * 长期照护评估结论、长期照护方案生效/调整、个案会商协商结论等
 * 以时间线形式持续写回老人档案，形成可追溯的照护记录。
 */
@Getter
@Setter
@Entity
@Table(name = "elder_archive_note")
public class ElderArchiveNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Elder elder;

    /** ASSESSMENT 长期照护评估 / CARE_PLAN 照护方案 / CONFERENCE 个案会商 /
     *  REVIEW 效果观察 / MANUAL 社工记录 */
    @Column(nullable = false)
    private String type;

    /** 关联业务编号（评估/方案/会商 id），便于跳转 */
    private Long refId;
    private String refNo;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    private String operatorName;
    private LocalDateTime createdAt = LocalDateTime.now();
}
