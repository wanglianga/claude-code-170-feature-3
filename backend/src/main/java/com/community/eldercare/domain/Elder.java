package com.community.eldercare.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 独居老人档案与平时照护画像。
 * 画像信息在每次求助归档后联动更新（风险等级、误触记录、家属响应表现、
 * 邻里协助可信度、下一次主动关怀计划等）。
 */
@Getter
@Setter
@Entity
@Table(name = "elder")
public class Elder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String gender;
    private LocalDate birthDate;
    private Integer age;
    private String phone;

    @Column(nullable = false)
    private String address;

    /** 住址经纬度，用于判断求助定位是否偏移 */
    private Double homeLongitude;
    private Double homeLatitude;

    /** 居住状态：长期独居 / 与子女同住 / 日间独居 等 */
    private String livingStatus;

    /** 基础病，如 高血压、糖尿病、冠心病 */
    private String chronicDiseases;

    /** 既往病史与用药备注 */
    private String medicalNote;

    /** 门禁信息：楼栋门禁密码、钥匙存放点、智能锁授权等 */
    @Column(length = 1000)
    private String doorAccessInfo;

    /* ---------------- 照护画像 ---------------- */

    /** 综合风险等级 LOW / MEDIUM / HIGH / CRITICAL */
    private String riskLevel = "MEDIUM";

    private boolean livingAloneLongTerm = true;

    /** 是否安装燃气报警器 */
    private boolean gasAlarmInstalled;
    /** 是否安装独立烟感 */
    private boolean smokeAlarmInstalled;
    /** 是否配置一键呼叫器/跌倒检测设备 */
    private boolean sosDeviceInstalled;

    /** 最近一次家属探访时间 */
    private LocalDate lastFamilyVisitDate;

    /** 最近一次社区医生随访时间 */
    private LocalDate lastDoctorFollowUpDate;
    /** 社区医生随访结论 */
    @Column(length = 1000)
    private String doctorFollowUpConclusion;

    /** 过往误触次数 */
    private Integer falseTriggerCount = 0;
    @Column(length = 1000)
    private String falseTriggerNote;

    /** 家属响应表现评分 0-100 */
    private Integer familyResponsivenessScore = 80;
    /** 家属响应备注（如长期不接电话） */
    @Column(length = 1000)
    private String familyResponsivenessNote;

    /** 邻里协助可信度评分 0-100 */
    private Integer neighborReliabilityScore = 80;

    /* ---- 关怀计划与服务建议（事件归档后更新） ---- */

    /** 下一次主动关怀时间 */
    private LocalDateTime nextProactiveCareAt;
    /** 主动关怀类型：日常巡访 / 助餐 / 适老化改造 / 心理陪伴 */
    private String proactiveCareType;
    @Column(length = 1000)
    private String proactiveCareNote;

    /** 建议增加日常巡访 */
    private boolean suggestDailyVisit;
    /** 建议助餐服务 */
    private boolean suggestMealService;
    /** 建议适老化改造（针对跌倒/安全隐患） */
    private boolean suggestAgingRetrofit;
    /** 建议心理陪伴 */
    private boolean suggestPsychCompanion;

    /* ---- 长期照护方案与个案会商写回 ---- */

    /** 最近一次个案会商时间 */
    private LocalDate lastCaseConferenceDate;
    /** 个案会商结论与后续行动（养老顾问、社区医生、家属协商结论写回档案） */
    @Column(length = 3000)
    private String caseConferenceNote;
    /** 长期照护跟踪备注（方案/会商形成的持续观察结论） */
    @Column(length = 3000)
    private String longTermCareNote;

    @Column(length = 2000)
    private String remark;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
