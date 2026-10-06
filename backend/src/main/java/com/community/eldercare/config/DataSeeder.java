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
    private final EventLogRepository logRepo;
    private final EventFollowUpRepository followUpRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataSeeder(UserRepository userRepo, ElderRepository elderRepo,
                      EmergencyContactRepository contactRepo, NeighborResponderRepository neighborRepo,
                      HelpEventRepository eventRepo, OnsiteVisitRepository onsiteRepo,
                      FamilyCommunicationRepository commRepo, EventLogRepository logRepo,
                      EventFollowUpRepository followUpRepo) {
        this.userRepo = userRepo;
        this.elderRepo = elderRepo;
        this.contactRepo = contactRepo;
        this.neighborRepo = neighborRepo;
        this.eventRepo = eventRepo;
        this.onsiteRepo = onsiteRepo;
        this.commRepo = commRepo;
        this.logRepo = logRepo;
        this.followUpRepo = followUpRepo;
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

            seedFrequentNightCase(e3);

            log.info("种子老人档案已创建：周桂兰、吴德海、郑秀珍");
        }
    }

    /**
     * 为郑秀珍构造近一周 3 次夜间求助的完整处置留痕：
     * 每次的求助原因（语音/研判）、到场结果、孤独焦虑、药物管理混乱、家属参与（多次未接通）。
     * 第 3 次自动等价触发“频繁夜间求助”，供社区端观察并发起长期照护评估。
     */
    private void seedFrequentNightCase(Elder e) {
        EmergencyContact nephew = contactRepo.findByElderId(e.getId()).stream()
                .filter(c -> "郑伟".equals(c.getName())).findFirst().orElse(null);
        LocalDateTime now = LocalDateTime.now();

        seedNightEvent(e, "E260930001", now.minusDays(6).withHour(23).withMinute(12).withSecond(0).withNano(0),
                "PHONE", "夜里睡不着，心里发慌，想找人说说话", "EMOTIONAL_CRISIS", "HIGH", "",
                "钥匙开门", "情绪激动", true, "老伴去世后长期独处，夜间反复主诉心慌、害怕",
                false, null, false, "STABLE", nephew);
        seedNightEvent(e, "E261002002", now.minusDays(4).withHour(0).withMinute(43).withSecond(0).withNano(0),
                "SMART_BUTTON", "头晕起夜，找不到降压药，按钮求助", "BODY_DISCOMFORT", "HIGH", "",
                "智能锁授权", "身体不适", false, null,
                true, "药盒中早/晚药混放，存在漏服、重复服药，无人整理药盒", false, "STABLE", nephew);
        seedNightEvent(e, "E261004003", now.minusDays(2).withHour(2).withMinute(18).withSecond(0).withNano(0),
                "SMART_BUTTON", "胸口闷，晚饭药没吃，一直没人接电话", "BODY_DISCOMFORT", "CRITICAL",
                "FREQUENT_NIGHT_HELP", "智能锁授权", "身体不适", true, "情绪低落、焦虑，反复念叨没人管",
                true, "晚间降压药漏服，药盒提醒缺失", true, "STABLE", nephew);
        log.info("种子夜间频繁求助案例已创建（郑秀珍，近一周 3 次夜间事件）");
    }

    private void seedNightEvent(Elder elder, String no, LocalDateTime at, String source, String voice,
                                String triage, String severity, String flags, String entryMethod,
                                String elderStatus, boolean lonely, String emotionNote,
                                boolean medChaotic, String medNote, boolean thirdEventFamilyReached,
                                String followResult, EmergencyContact primaryContact) {
        HelpEvent ev = new HelpEvent();
        ev.setEventNo(no);
        ev.setElder(elder);
        ev.setStatus("CLOSED");
        ev.setSource(source);
        ev.setVoiceContent(voice);
        ev.setNightHelp(true);
        ev.setRiskFlags(flags);
        ev.setTriageType(triage);
        ev.setSeverity(severity);
        ev.setTriageWorkerName("李社工");
        ev.setTriagedAt(at.plusMinutes(3));
        LocalDateTime arrived = at.plusMinutes(9);
        ev.setDispatchedAt(at.plusMinutes(4));
        ev.setArrivedAt(arrived);
        ev.setResponseMinutes(9);
        ev.setFollowUpResult(followResult);
        ev.setFollowedUpAt(at.plusHours(20));
        ev.setClosedAt(at.plusHours(20));
        ev.setProfileUpdated(true);
        ev.setCreatedAt(at);
        eventRepo.save(ev);

        OnsiteVisit v = new OnsiteVisit();
        v.setEvent(ev);
        v.setEntryMethod(entryMethod);
        v.setEntered(true);
        v.setElderStatus(elderStatus);
        v.setFamilyContacted(true);
        v.setLonelyAnxious(lonely);
        v.setEmotionNote(emotionNote);
        v.setMedicationChaotic(medChaotic);
        v.setMedicationNote(medNote);
        v.setVisitorName("赵网格员");
        v.setArrivedAt(arrived);
        onsiteRepo.save(v);

        // 家属参与：侄子前两次未接通；第三次经多次拨打后接通，态度一般
        addSeedComm(ev, primaryContact, at.plusMinutes(6), false, null);
        addSeedComm(ev, primaryContact, at.plusMinutes(12), thirdEventFamilyReached,
                thirdEventFamilyReached ? "一般" : null);

        EventFollowUp f = new EventFollowUp();
        f.setEvent(ev);
        f.setResult(followResult);
        f.setMethod("电话回访");
        f.setSatisfaction(4);
        f.setNextCareType("DAILY_VISIT");
        f.setNextCareAt(at.plusDays(3));
        f.setOperatorName("李社工");
        f.setFollowedUpAt(at.plusHours(20));
        followUpRepo.save(f);

        seedLog(ev, "HELP", "夜间收到求助（" + at.toLocalDate() + " " + at.toLocalTime() + "），语音内容：" + voice, "系统", at.plusMinutes(1));
        seedLog(ev, "ONSITE", "上门核实：" + elderStatus
                + (lonely ? "，表现孤独焦虑" : "") + (medChaotic ? "，药物管理混乱" : ""), "赵网格员", arrived);
        seedLog(ev, "FAMILY_COMM",
                thirdEventFamilyReached ? "家属经多次拨打后接通，态度一般" : "连续 2 次拨打家属未接通",
                "李社工", at.plusMinutes(15));
        seedLog(ev, "CLOSE", "事件回访归档：" + followResult, "李社工", at.plusHours(20));
    }

    private void addSeedComm(HelpEvent ev, EmergencyContact contact, LocalDateTime at, boolean reached, String attitude) {
        FamilyCommunication c = new FamilyCommunication();
        c.setEvent(ev);
        c.setContact(contact);
        c.setContactName(contact == null ? "家属" : contact.getName());
        c.setContactPhone(contact == null ? null : contact.getPhone());
        c.setChannel("电话");
        c.setReached(reached);
        c.setAttitude(attitude);
        c.setOperatorName("李社工");
        c.setCommunicatedAt(at);
        commRepo.save(c);
        if (contact != null) {
            contact.setContactAttempts(contact.getContactAttempts() + 1);
            if (reached) contact.setContactReached(contact.getContactReached() + 1);
            contact.setReachabilityNote("近一周 6 次联系中仅 1 次接通，多为夜间求助时未接");
            contactRepo.save(contact);
        }
    }

    private void seedLog(HelpEvent ev, String type, String content, String operator, LocalDateTime at) {
        EventLog l = new EventLog();
        l.setEvent(ev);
        l.setType(type);
        l.setContent(content);
        l.setOperatorName(operator);
        l.setCreatedAt(at);
        logRepo.save(l);
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
}
