package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 上门核实记录。
 */
@Getter
@Setter
@Entity
@Table(name = "event_onsite_visit")
public class OnsiteVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HelpEvent event;

    /** 入户方式：钥匙开门/门禁密码/智能锁授权/邻居协助开门/家属到场开门/破锁入户/未能入户 */
    @Column(nullable = false)
    private String entryMethod;

    /** 是否成功入户（无法开门时为 false） */
    private boolean entered;

    /** 现场照片地址，多个逗号分隔 */
    @Column(length = 2000)
    private String photoUrls;

    /** 老人状态：清醒正常/身体不适/已跌倒/意识不清/情绪激动/未见到老人 等 */
    @Column(nullable = false)
    private String elderStatus;

    /** 是否联系家属 */
    private boolean familyContacted;
    /** 是否需要送医 */
    private boolean hospitalNeeded;
    /** 老人是否拒绝就医 */
    private boolean refuseMedical;
    /** 是否需要临时照护 */
    private boolean temporaryCareNeeded;
    /** 后续服务建议：日常巡访、助餐、适老化改造、心理陪伴 等（逗号分隔） */
    @Column(length = 500)
    private String followServiceSuggestions;

    /** 家中安全隐患描述（地滑、电线老化、燃气、照明不足等） */
    @Column(length = 2000)
    private String safetyHazards;

    /** 上门时老人是否表现出孤独、焦虑等情绪问题（长期照护评估依据） */
    private boolean lonelyAnxious;
    @Column(length = 1000)
    private String emotionNote;

    /** 是否发现药物管理混乱（漏服/重复服/错服/药盒无人整理等，长期照护评估依据） */
    private boolean medicationChaotic;
    @Column(length = 1000)
    private String medicationNote;

    private String visitorName;
    private LocalDateTime arrivedAt = LocalDateTime.now();

    @Column(length = 2000)
    private String note;
}
