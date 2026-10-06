package com.community.eldercare.config;

import com.community.eldercare.domain.*;
import com.community.eldercare.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 初始化测试账号与示例老人画像（仅在库为空时执行）。
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepo;
    private final ElderRepository elderRepo;
    private final EmergencyContactRepository contactRepo;
    private final NeighborResponderRepository neighborRepo;
    private final HelpEventRepository eventRepo;
    private final OnsiteVisitRepository onsiteRepo;
    private final FamilyCommunicationRepository commRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataSeeder(UserRepository userRepo, ElderRepository elderRepo,
                      EmergencyContactRepository contactRepo, NeighborResponderRepository neighborRepo,
                      HelpEventRepository eventRepo, OnsiteVisitRepository onsiteRepo,
                      FamilyCommunicationRepository commRepo) {
        this.userRepo = userRepo;
        this.elderRepo = elderRepo;
        this.contactRepo = contactRepo;
        this.neighborRepo = neighborRepo;
        this.eventRepo = eventRepo;
        this.onsiteRepo = onsiteRepo;
        this.commRepo = commRepo;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() == 0) {
            createUser("worker", "123456", "李社工", "DUTY_WORKER", "13800000001");
            createUser("admin", "123456", "王主任", "ADMIN", "13800000002");
            createUser("grid", "123456", "赵网格员", "GRID_WORKER", "13800000003");
            createUser("doctor", "123456", "钱医生", "FAMILY_DOCTOR", "13800000004");
            createUser("volunteer", "123456", "孙阿姨", "NEIGHBOR_VOLUNTEER", "13800000005");
            log.info("种子用户已创建（密码均为 123456）");
        }

        if (elderRepo.count() == 0) {
            Elder e1 = new Elder();
            e1.setName("周桂兰");
            e1.setGender("女");
            e1.setAge(82);
            e1.setBirthDate(LocalDate.of(1944, 3, 12));
            e1.setPhone("13900000011");
            e1.setAddress("幸福社区 3 栋 2 单元 501");
            e1.setHomeLongitude(118.7969);
            e1.setHomeLatitude(32.0603);
            e1.setLivingStatus("长期独居");
            e1.setLivingAloneLongTerm(true);
            e1.setChronicDiseases("高血压、糖尿病、轻度冠心病");
            e1.setMedicalNote("长期服用降压药、二甲双胍；对青霉素过敏");
            e1.setDoorAccessInfo("楼栋门禁密码 3#501；备用钥匙存放于对门孙阿姨家；智能锁已授权网格员赵网格员");
            e1.setRiskLevel("HIGH");
            e1.setGasAlarmInstalled(false);
            e1.setSmokeAlarmInstalled(true);
            e1.setSosDeviceInstalled(true);
            e1.setLastFamilyVisitDate(LocalDate.now().minusDays(75));
            e1.setLastDoctorFollowUpDate(LocalDate.now().minusDays(20));
            e1.setDoctorFollowUpConclusion("血压控制一般，建议加强随访，注意防跌倒");
            e1.setFalseTriggerCount(1);
            e1.setFalseTriggerNote("2026-09 误触一次（打扫碰到按钮）");
            e1.setFamilyResponsivenessScore(45);
            e1.setFamilyResponsivenessNote("儿子长期在外地，电话经常不接");
            e1.setNeighborReliabilityScore(85);
            e1.setNextProactiveCareAt(LocalDateTime.now().plusDays(2));
            e1.setProactiveCareType("日常巡访");
            e1.setProactiveCareNote("高风险老人，加密巡访");
            e1.setSuggestDailyVisit(true);
            e1.setRemark("行动迟缓，夜间起夜频繁");
            elderRepo.save(e1);

            addContact(e1, "周建国", "儿子", "13700000021", true, true);
            addContact(e1, "周建梅", "女儿", "13700000022", false, false);
            addNeighbor(e1, "孙阿姨", "13800000005", "对门 502", 90);

            Elder e2 = new Elder();
            e2.setName("吴德海");
            e2.setGender("男");
            e2.setAge(76);
            e2.setBirthDate(LocalDate.of(1950, 7, 8));
            e2.setPhone("13900000012");
            e2.setAddress("幸福社区 7 栋 1 单元 302");
            e2.setHomeLongitude(118.7982);
            e2.setHomeLatitude(32.0611);
            e2.setLivingStatus("长期独居");
            e2.setLivingAloneLongTerm(true);
            e2.setChronicDiseases("慢阻肺、膝关节炎");
            e2.setMedicalNote("冬季易咳喘，活动能力差");
            e2.setDoorAccessInfo("门禁卡挂于玄关；女儿持有备用钥匙");
            e2.setRiskLevel("MEDIUM");
            e2.setGasAlarmInstalled(true);
            e2.setSmokeAlarmInstalled(true);
            e2.setSosDeviceInstalled(true);
            e2.setLastFamilyVisitDate(LocalDate.now().minusDays(12));
            e2.setLastDoctorFollowUpDate(LocalDate.now().minusDays(45));
            e2.setDoctorFollowUpConclusion("慢阻肺稳定期，建议入冬前复查");
            e2.setFamilyResponsivenessScore(88);
            e2.setNeighborReliabilityScore(80);
            e2.setNextProactiveCareAt(LocalDateTime.now().plusDays(7));
            e2.setProactiveCareType("日常巡访");
            e2.setRemark("家中卫生间无扶手，存在跌倒隐患");
            e2.setSuggestAgingRetrofit(true);
            elderRepo.save(e2);

            addContact(e2, "吴小燕", "女儿", "13700000031", true, false);
            addNeighbor(e2, "李师傅", "13600000032", "同单元 202", 75);

            Elder e3 = new Elder();
            e3.setName("郑秀珍");
            e3.setGender("女");
            e3.setAge(88);
            e3.setBirthDate(LocalDate.of(1938, 1, 25));
            e3.setPhone("13900000013");
            e3.setAddress("幸福社区 1 栋 3 单元 101");
            e3.setHomeLongitude(118.7955);
            e3.setHomeLatitude(32.0592);
            e3.setLivingStatus("长期独居");
            e3.setLivingAloneLongTerm(true);
            e3.setChronicDiseases("脑梗后遗症、听力下降");
            e3.setMedicalNote("左侧肢体活动不便，使用助行器");
            e3.setDoorAccessInfo("智能锁密码 123888；网格员、社区居委会各留一把备用钥匙");
            e3.setRiskLevel("CRITICAL");
            e3.setGasAlarmInstalled(false);
            e3.setSmokeAlarmInstalled(false);
            e3.setSosDeviceInstalled(true);
            e3.setLastFamilyVisitDate(LocalDate.now().minusDays(120));
            e3.setLastDoctorFollowUpDate(LocalDate.now().minusDays(60));
            e3.setDoctorFollowUpConclusion("脑梗后遗症康复中，需持续康复训练与防跌倒评估");
            e3.setFalseTriggerCount(0);
            e3.setFamilyResponsivenessScore(30);
            e3.setFamilyResponsivenessNote("无本地亲属，侄子偶有电话，长期无人探访");
            e3.setNeighborReliabilityScore(70);
            e3.setNextProactiveCareAt(LocalDateTime.now().plusDays(1));
            e3.setProactiveCareType("助餐");
            e3.setSuggestDailyVisit(true);
            e3.setSuggestMealService(true);
            e3.setSuggestAgingRetrofit(true);
            e3.setSuggestPsychCompanion(true);
            e3.setRemark("近一个月多次夜间求助，情绪低落");
            elderRepo.save(e3);

            addContact(e3, "郑伟", "侄子", "13700000041", true, true);
            addNeighbor(e3, "陈姐", "13600000042", "同栋 201", 70);

            // 郑秀珍近一周已有 3 次夜间求助：孤独焦虑为主，伴药物管理混乱、家属参与不足
            seedNightEvent(e3, 6, 23, 12, "EMOTIONAL_CRISIS", "HIGH",
                    "睡不着、一个人害怕，想有人来说说话",
                    new String[]{"孤独焦虑", "夜间恐惧"}, "CALMED",
                    true, "反复按键求陪伴，情绪低落", false, null,
                    false, "侄子电话未接通", false, false);
            seedNightEvent(e3, 4, 2, 36, "LIFE_ASSIST", "MEDIUM",
                    "起夜后找不到降压药，不知道吃没吃",
                    new String[]{"孤独焦虑", "用药问题"}, "SCENE_RESOLVED",
                    true, "夜间恐惧、表达孤独", true, "早晚药混放，重复服药一次",
                    false, "两次拨打侄子均未接", false, false);
            seedNightEvent(e3, 1, 22, 58, "EMOTIONAL_CRISIS", "HIGH",
                    "胸口闷、害怕，按了按钮后好一些",
                    new String[]{"孤独焦虑", "身体不适"}, "CALMED",
                    true, "见到社工后情绪平复，承认主要是孤单", true, "漏服晚间降压药",
                    true, "侄子接通后电话共同安抚，未到场", true, true);

            log.info("种子老人档案已创建：周桂兰、吴德海、郑秀珍（含郑秀珍近一周3次夜间求助）");
        }
    }

    private void createUser(String username, String pwd, String name, String role, String phone) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(encoder.encode(pwd));
        u.setName(name);
        u.setRole(role);
        u.setPhone(phone);
        u.setCommunity("幸福社区");
        u.setEnabled(true);
        userRepo.save(u);
    }

    private void addContact(Elder elder, String name, String rel, String phone,
                            boolean primary, boolean oftenUnreachable) {
        EmergencyContact c = new EmergencyContact();
        c.setElder(elder);
        c.setName(name);
        c.setRelationship(rel);
        c.setPhone(phone);
        c.setPrimaryContact(primary);
        c.setOftenUnreachable(oftenUnreachable);
        contactRepo.save(c);
    }

    private void addNeighbor(Elder elder, String name, String phone, String proximity, int score) {
        NeighborResponder n = new NeighborResponder();
        n.setElder(elder);
        n.setName(name);
        n.setPhone(phone);
        n.setProximity(proximity);
        n.setReliabilityScore(score);
        neighborRepo.save(n);
    }

    /**
     * 构造一条已归档的夜间求助种子事件（含上门核实新维度与家属沟通），
     * 让“一周内多次夜间求助”在全新启动时即可演示。
     */
    private void seedNightEvent(Elder elder, int daysAgo, int hour, int minute,
                                String triageType, String severity, String voice,
                                String[] reasons, String arriveResult,
                                boolean lonely, String anxietyNote,
                                boolean medChaos, String medNote,
                                boolean familyInvolved, String familyNote,
                                boolean familyReached, boolean familyContacted) {
        java.time.LocalDateTime at = java.time.LocalDate.now().minusDays(daysAgo).atTime(hour, minute);
        HelpEvent e = new HelpEvent();
        String datePart = String.format("%tY%<tm%<td", at).substring(2);
        e.setEventNo(String.format("E%s-S%02d", datePart, (int) (eventRepo.count() + 1)));
        e.setElder(elder);
        e.setSource("SMART_BUTTON");
        e.setSourceDetail("SOS-DEVICE-003");
        e.setVoiceContent(voice);
        e.setNightHelp(true);
        e.setStatus("CLOSED");
        e.setTriageType(triageType);
        e.setSeverity(severity);
        e.setTriageNote("种子事件：值班研判");
        e.setTriageWorkerName("李社工");
        e.setTriagedAt(at.plusMinutes(3));
        e.setArrivedAt(at.plusMinutes(12));
        e.setResponseMinutes(12);
        e.setFollowUpResult("STABLE");
        e.setFollowedUpAt(at.plusHours(2));
        e.setClosedAt(at.plusHours(2));
        e.setProfileUpdated(false);
        StringBuilder flags = new StringBuilder("FAMILY_UNREACHABLE");
        if (eventRepo.findNightHelpsSince(elder.getId(), at.minusDays(30)).size() + 1 >= 3) {
            flags.append(",FREQUENT_NIGHT_HELP");
        }
        e.setRiskFlags(flags.toString());
        e.setCreatedAt(at);
        eventRepo.save(e);

        OnsiteVisit v = new OnsiteVisit();
        v.setEvent(e);
        v.setEntryMethod("智能锁授权");
        v.setEntered(true);
        v.setElderStatus(lonely ? "情绪激动" : "清醒正常");
        v.setHelpReason(String.join(",", reasons));
        v.setArriveResult(arriveResult);
        v.setLonelyAnxious(lonely);
        v.setAnxietyNote(anxietyNote);
        v.setMedicationChaos(medChaos);
        v.setMedicationNote(medNote);
        v.setFamilyInvolved(familyInvolved);
        v.setFamilyInvolvementNote(familyNote);
        v.setFamilyContacted(familyContacted);
        v.setFollowServiceSuggestions("日常巡访,心理陪伴,助餐");
        v.setVisitorName("赵网格员");
        v.setArrivedAt(at.plusMinutes(12));
        onsiteRepo.save(v);

        FamilyCommunication c = new FamilyCommunication();
        c.setEvent(e);
        c.setContactName("郑伟");
        c.setContactPhone("13700000041");
        c.setChannel("电话");
        c.setReached(familyReached);
        c.setAttitude(familyReached ? "一般" : null);
        c.setNote(familyNote);
        c.setOperatorName("李社工");
        c.setCommunicatedAt(at.plusMinutes(8));
        commRepo.save(c);
    }
}
