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

    /** 本次求助原因（现场核实），逗号分隔：孤独焦虑/身体不适/跌倒/用药问题/生活协助/设备误触/其他 */
    @Column(length = 500)
    private String helpReason;

    /** 到场结果：SCENE_RESOLVED 现场解决 / CALMED 安抚后平稳 / TO_HOSPITAL 送医处置 /
     *  TEMP_CARE 安排临时照护 / FORWARD_SERVICE 转长期服务 / NO_ENTRY 未能入户 */
    private String arriveResult;

    /** 是否存在孤独焦虑表现 */
    private boolean lonelyAnxious;
    /** 孤独/焦虑表现描述（反复按键求陪伴、情绪低落、睡眠差等） */
    @Column(length = 1000)
    private String anxietyNote;

    /** 是否存在药物管理混乱（漏服、重复服、药盒混乱、过期药等） */
    private boolean medicationChaos;
    /** 药物管理情况描述 */
    @Column(length = 1000)
    private String medicationNote;

    /** 本次家属是否参与（到场/通话共同处置） */
    private boolean familyInvolved;
    /** 家属参与情况说明 */
    @Column(length = 1000)
    private String familyInvolvementNote;

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

    private String visitorName;
    private LocalDateTime arrivedAt = LocalDateTime.now();

    @Column(length = 2000)
    private String note;
}
