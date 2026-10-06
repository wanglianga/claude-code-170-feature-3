package com.community.eldercare.service;

import com.community.eldercare.common.ApiException;
import com.community.eldercare.domain.*;
import com.community.eldercare.dto.*;
import com.community.eldercare.notify.SseService;
import com.community.eldercare.repo.*;
import com.community.eldercare.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 长期照护服务：
 * 频繁夜间求助观察 → 长期照护评估 → 长期照护方案（定期探访/心理陪伴/助餐/家庭医生随访/药盒提醒）
 * → 生效后持续观察（夜间求助次数、助餐签到、药盒提醒、家属接听率）→ 无改善发起个案会商，结论写回老人档案。
 */
@Service
public class CareService {

    private final ElderRepository elderRepo;
    private final HelpEventRepository eventRepo;
    private final OnsiteVisitRepository onsiteRepo;
    private final EventServiceRepository serviceRepo;
    private final FamilyCommunicationRepository commRepo;
    private final CareAssessmentRepository assessmentRepo;
    private final CarePlanRepository planRepo;
    private final CarePlanItemRepository itemRepo;
    private final CareActivityRepository activityRepo;
    private final CaseConferenceRepository conferenceRepo;
    private final ConferenceParticipantRepository participantRepo;
    private final ElderArchiveNoteRepository archiveRepo;
    private final SseService sse;

    public CareService(ElderRepository elderRepo, HelpEventRepository eventRepo,
                       OnsiteVisitRepository onsiteRepo, EventServiceRepository serviceRepo,
                       FamilyCommunicationRepository commRepo, CareAssessmentRepository assessmentRepo,
                       CarePlanRepository planRepo, CarePlanItemRepository itemRepo,
                       CareActivityRepository activityRepo, CaseConferenceRepository conferenceRepo,
                       ConferenceParticipantRepository participantRepo,
                       ElderArchiveNoteRepository archiveRepo, SseService sse) {
        this.elderRepo = elderRepo;
        this.eventRepo = eventRepo;
        this.onsiteRepo = onsiteRepo;
        this.serviceRepo = serviceRepo;
        this.commRepo = commRepo;
        this.assessmentRepo = assessmentRepo;
        this.planRepo = planRepo;
        this.itemRepo = itemRepo;
        this.activityRepo = activityRepo;
        this.conferenceRepo = conferenceRepo;
        this.participantRepo = participantRepo;
        this.archiveRepo = archiveRepo;
        this.sse = sse;
    }

    /* ===================== 夜间求助观察（社区端） ===================== */

    @Transactional(readOnly = true)
    public Map<String, Object> nightReview(Long elderId, Integer days) {
        Elder elder = mustElder(elderId);
        int period = days == null || days <= 0 ? 7 : days;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusDays(period);

        List<HelpEvent> nights = eventRepo.findNightHelpsSince(elderId, since);
        Map<Long, List<OnsiteVisit>> visitsByEvent = new LinkedHashMap<>();
        Map<Long, List<FamilyCommunication>> commsByEvent = new LinkedHashMap<>();
        Map<Long, List<com.community.eldercare.domain.EventService>> servicesByEvent = new LinkedHashMap<>();
        int lonely = 0, med = 0;
        for (HelpEvent e : nights) {
            List<OnsiteVisit> vs = onsiteRepo.findByEventIdOrderByArrivedAtAsc(e.getId());
            visitsByEvent.put(e.getId(), vs);
            commsByEvent.put(e.getId(), commRepo.findByEventIdOrderByCommunicatedAtAsc(e.getId()));
            servicesByEvent.put(e.getId(), serviceRepo.findByEventIdOrderByServiceAtAsc(e.getId()));
            if (vs.stream().anyMatch(OnsiteVisit::isLonelyAnxious)) lonely++;
            if (vs.stream().anyMatch(OnsiteVisit::isMedicationChaotic)) med++;
        }

        List<FamilyCommunication> windowComms = commRepo.findByElderSince(elderId, since);
        long attempts = windowComms.size();
        long reached = windowComms.stream().filter(FamilyCommunication::isReached).count();
        int reachRate = attempts == 0 ? 0 : (int) Math.round(100.0 * reached / attempts);

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("elder", elder);
        m.put("periodDays", period);
        m.put("totalNightCount", nights.size());
        m.put("events", nights);
        m.put("visitsByEvent", visitsByEvent);
        m.put("commsByEvent", commsByEvent);
        m.put("servicesByEvent", servicesByEvent);
        m.put("windowCommunications", windowComms);
        m.put("lonelyAnxiousCount", lonely);
        m.put("medicationChaoticCount", med);
        m.put("familyAttempts", attempts);
        m.put("familyReached", reached);
        m.put("familyReachRate", reachRate);
        m.put("frequentFlag", nights.size() >= 3);
        m.put("suggestAssessment", nights.size() >= 2);
        return m;
    }

    /* ===================== 长期照护评估 ===================== */

    @Transactional
    public CareAssessment createAssessment(Long elderId, AssessmentRequest req) {
        Elder elder = mustElder(elderId);
        int period = req.getPeriodDays() == null || req.getPeriodDays() <= 0 ? 7 : req.getPeriodDays();
        LocalDateTime now = LocalDateTime.now();

        CareAssessment a = new CareAssessment();
        a.setElder(elder);
        if (req.getTriggerEventId() != null) {
            a.setTriggerEvent(eventRepo.findById(req.getTriggerEventId()).orElse(null));
        }
        a.setAssessorName(UserContext.currentName());
        a.setAssessedAt(now);
        a.setPeriodDays(period);
        a.setNightHelpCount(eventRepo.findNightHelpsSince(elderId, now.minusDays(period)).size());

        // 自动汇总：夜间求助原因（语音内容/研判）与到场结果
        List<HelpEvent> nights = eventRepo.findNightHelpsSince(elderId, now.minusDays(period));
        if (isBlank(req.getNightReasonSummary())) {
            List<String> reasons = new ArrayList<>();
            for (HelpEvent e : nights) {
                String r = firstNonBlank(e.getVoiceContent(),
                        e.getTriageType() == null ? null : EventService.triageLabel(e.getTriageType()));
                if (r != null) reasons.add(e.getEventNo() + "：" + r);
            }
            a.setNightReasonSummary(reasons.isEmpty() ? "近" + period + "天无夜间求助记录" : String.join("；", reasons));
        } else {
            a.setNightReasonSummary(req.getNightReasonSummary());
        }
        if (isBlank(req.getOnsiteResultSummary())) {
            List<OnsiteVisit> vs = onsiteRepo.findByElderSince(elderId, now.minusDays(period));
            List<String> results = new ArrayList<>();
            for (OnsiteVisit v : vs) {
                StringBuilder sb = new StringBuilder(v.getElderStatus());
                if (v.isHospitalNeeded()) sb.append("·需送医");
                if (v.isTemporaryCareNeeded()) sb.append("·需临时照护");
                if (v.isLonelyAnxious()) sb.append("·孤独焦虑");
                if (v.isMedicationChaotic()) sb.append("·药物混乱");
                results.add(sb.toString());
            }
            a.setOnsiteResultSummary(results.isEmpty() ? "窗口内暂无上门记录" : String.join("；", results));
        } else {
            a.setOnsiteResultSummary(req.getOnsiteResultSummary());
        }

        a.setLonelyAnxious(req.isLonelyAnxious());
        a.setEmotionNote(req.getEmotionNote());
        a.setMedicationChaotic(req.isMedicationChaotic());
        a.setMedicationNote(req.getMedicationNote());

        // 家属参与度：未指定时按窗口接通率推断
        String inv = req.getFamilyInvolvement();
        if (isBlank(inv)) {
            List<FamilyCommunication> cs = commRepo.findByElderSince(elderId, now.minusDays(period));
            long att = cs.size(), rc = cs.stream().filter(FamilyCommunication::isReached).count();
            int rate = att == 0 ? 0 : (int) Math.round(100.0 * rc / att);
            inv = rate >= 70 ? "GOOD" : rate >= 40 ? "AVERAGE" : "POOR";
        }
        a.setFamilyInvolvement(inv);
        a.setFamilyNote(req.getFamilyNote());
        a.setDailyLivingNote(req.getDailyLivingNote());
        a.setChronicRiskNote(req.getChronicRiskNote());
        a.setHomeSafetyNote(req.getHomeSafetyNote());
        a.setResultLevel(isBlank(req.getResultLevel())
                ? (req.isLonelyAnxious() || req.isMedicationChaotic() ? "INTENSIVE" : "MEDIUM")
                : req.getResultLevel());
        a.setConclusion(req.getConclusion());
        a.setRecommendations(req.getRecommendations() == null ? "" : String.join(",", req.getRecommendations()));
        a = assessmentRepo.save(a);

        writeArchive(elder, "ASSESSMENT", a.getId(), null,
                "完成长期照护评估",
                "近" + a.getPeriodDays() + "天夜间求助 " + a.getNightHelpCount()
                        + " 次；孤独焦虑：" + yn(a.isLonelyAnxious())
                        + "；药物管理混乱：" + yn(a.isMedicationChaotic())
                        + "；家属参与：" + familyLabel(a.getFamilyInvolvement())
                        + "；照护强度：" + levelLabel(a.getResultLevel())
                        + (a.getConclusion() != null && !a.getConclusion().isBlank() ? "。结论：" + a.getConclusion() : "")
                        + "。建议服务：" + itemTypesLabel(a.getRecommendations()));
        sse.broadcast("care-update", Map.of("elderId", elderId, "type", "ASSESSMENT", "id", a.getId()));
        return a;
    }

    /* ===================== 长期照护方案 ===================== */

    @Transactional
    public CarePlan createPlan(PlanRequest req) {
        Elder elder = mustElder(req.getElderId());
        CarePlan plan = new CarePlan();
        plan.setElder(elder);
        plan.setPlanNo(generatePlanNo());
        plan.setPlanName(req.getPlanName());
        plan.setGoal(req.getGoal());
        plan.setObserveDays(req.getObserveDays() == null || req.getObserveDays() <= 0 ? 28 : req.getObserveDays());
        plan.setCreatedByName(UserContext.currentName());
        if (req.getAssessmentId() != null) {
            CareAssessment a = assessmentRepo.findById(req.getAssessmentId())
                    .orElseThrow(() -> new ApiException("评估记录不存在"));
            if (!a.getElder().getId().equals(elder.getId())) {
                throw new ApiException("评估记录与老人不匹配");
            }
            plan.setAssessment(a);
        }
        plan = planRepo.save(plan);

        for (PlanRequest.ItemInput in : req.getItems()) {
            if (isBlank(in.getItemType())) continue;
            CarePlanItem item = new CarePlanItem();
            item.setPlan(plan);
            item.setItemType(in.getItemType());
            item.setFrequency(in.getFrequency());
            item.setProviderName(in.getProviderName());
            item.setProviderPhone(in.getProviderPhone());
            item.setTargetNote(in.getTargetNote());
            item.setPlannedTimes(in.getPlannedTimes());
            itemRepo.save(item);
        }

        if (req.isActivate()) {
            activate(plan);
        }
        return plan;
    }

    @Transactional
    public CarePlan activatePlan(Long planId) {
        CarePlan plan = mustPlan(planId);
        if ("ACTIVE".equals(plan.getStatus())) {
            return plan;
        }
        if ("TERMINATED".equals(plan.getStatus())) {
            throw new ApiException("方案已终止，不能再次生效");
        }
        activate(plan);
        return plan;
    }

    private void activate(CarePlan plan) {
        LocalDateTime now = LocalDateTime.now();
        plan.setStatus("ACTIVE");
        plan.setEffectiveAt(now);
        int days = plan.getObserveDays() == null ? 28 : plan.getObserveDays();
        plan.setObserveEndAt(now.plusDays(days));

        // 基线：方案生效前同等周期夜间求助次数；家属接听率按生效前 30 天
        plan.setBaselineNightCount(eventRepo.findNightHelpsSince(plan.getElder().getId(), now.minusDays(days)).size());
        List<FamilyCommunication> before = commRepo.findByElderSince(plan.getElder().getId(), now.minusDays(30));
        long att = before.size(), rc = before.stream().filter(FamilyCommunication::isReached).count();
        plan.setBaselineFamilyReachRate(att == 0 ? null : (int) Math.round(100.0 * rc / att));
        planRepo.save(plan);

        writeArchive(plan.getElder(), "CARE_PLAN", plan.getId(), plan.getPlanNo(),
                "长期照护方案生效：" + plan.getPlanName(),
                "观察周期 " + days + " 天；基线夜间求助 " + nz(plan.getBaselineNightCount())
                        + " 次/" + days + "天；基线家属接听率 "
                        + (plan.getBaselineFamilyReachRate() == null ? "无数据" : plan.getBaselineFamilyReachRate() + "%")
                        + "。服务项：" + itemListBrief(plan.getId())
                        + (plan.getGoal() != null ? "。目标：" + plan.getGoal() : ""));
        sse.broadcast("care-plan-update", Map.of("planId", plan.getId(), "elderId", plan.getElder().getId()));
    }

    @Transactional
    public CarePlan completePlan(Long planId) {
        CarePlan plan = mustPlan(planId);
        plan.setStatus("COMPLETED");
        planRepo.save(plan);
        writeArchive(plan.getElder(), "CARE_PLAN", plan.getId(), plan.getPlanNo(),
                "长期照护方案观察期结束：" + plan.getPlanName(),
                "最近效果判定：" + verdictLabel(plan.getLastReviewResult())
                        + (plan.getLastReviewNote() != null ? "（" + plan.getLastReviewNote() + "）" : ""));
        return plan;
    }

    /* ===================== 服务执行 / 签到登记 ===================== */

    @Transactional
    public CareActivity addActivity(Long planId, ActivityRequest req) {
        CarePlan plan = mustPlan(planId);
        CareActivity a = new CareActivity();
        a.setPlan(plan);
        a.setActivityType(req.getActivityType());
        a.setRecordType(isBlank(req.getRecordType()) ? "REGISTER" : req.getRecordType());
        if (req.getItemId() != null) {
            CarePlanItem item = itemRepo.findById(req.getItemId()).orElse(null);
            if (item != null && item.getPlan().getId().equals(planId)) {
                a.setItem(item);
                a.setProviderName(item.getProviderName());
            }
        }
        if (!isBlank(req.getProviderName())) a.setProviderName(req.getProviderName());
        a.setOperatorName(UserContext.currentName());
        a.setMedicationTaken(req.getMedicationTaken());
        a.setFamilyReached(req.getFamilyReached());
        a.setFamilyContactName(req.getFamilyContactName());
        a.setElderMood(req.getElderMood());
        a.setContent(req.getContent());
        a.setNote(req.getNote());
        if (!isBlank(req.getActivityAt())) {
            try {
                a.setActivityAt(LocalDateTime.parse(req.getActivityAt().replace(' ', 'T')));
            } catch (Exception ignore) {
                // 格式非法时使用当前时间
            }
        }
        a = activityRepo.save(a);

        // 累加服务项签到次数（失约不计）
        if (a.getItem() != null && "REGISTER".equals(a.getRecordType())) {
            CarePlanItem item = a.getItem();
            item.setCheckInCount((item.getCheckInCount() == null ? 0 : item.getCheckInCount()) + 1);
            itemRepo.save(item);
        }
        sse.broadcast("care-plan-update", Map.of("planId", planId, "elderId", plan.getElder().getId()));
        return a;
    }

    /* ===================== 方案效果观察 ===================== */

    @Transactional(readOnly = true)
    public Map<String, Object> planEffect(Long planId) {
        CarePlan plan = mustPlan(planId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("plan", plan);

        LocalDateTime eff = plan.getEffectiveAt();
        LocalDateTime now = LocalDateTime.now();
        if (eff == null) {
            m.put("active", false);
            return m;
        }
        LocalDateTime windowEnd = now.isAfter(plan.getObserveEndAt()) ? plan.getObserveEndAt() : now;
        long daysElapsed = Math.max(0, ChronoUnit.DAYS.between(eff, now));
        long daysRemain = Math.max(0, ChronoUnit.DAYS.between(now, plan.getObserveEndAt()));
        m.put("active", "ACTIVE".equals(plan.getStatus()));
        m.put("daysElapsed", daysElapsed);
        m.put("daysRemaining", daysRemain);

        int observeDays = plan.getObserveDays();
        int weekCount = Math.max(1, (int) Math.ceil(observeDays / 7.0));

        // 1) 夜间求助
        List<HelpEvent> nightEvents = eventRepo.findNightHelpsSince(plan.getElder().getId(), eff).stream()
                .filter(e -> !e.getCreatedAt().isAfter(windowEnd)).toList();
        List<Long> nightWeekly = weeklyBuckets(eff, weekCount, nightEvents.stream()
                .map(HelpEvent::getCreatedAt).toList());
        int nightTotal = nightEvents.size();
        double baselineWeekly = observeDays == 0 ? 0 : (plan.getBaselineNightCount() == null ? 0
                : plan.getBaselineNightCount() * 7.0 / observeDays);
        long elapsedWeeks = Math.max(1, (long) Math.ceil(Math.min(daysElapsed, observeDays) / 7.0));
        double currentWeekly = nightTotal * 1.0 / elapsedWeeks;
        m.put("night", buildMetric(plan.getBaselineNightCount(), nightTotal, "次/" + observeDays + "天",
                baselineWeekly, currentWeekly));
        m.put("nightEvents", nightEvents);
        m.put("nightWeekly", nightWeekly);

        // 2) 助餐签到
        List<CareActivity> meals = activityRepo.findByPlanIdAndActivityTypeOrderByActivityAtAsc(planId, "MEAL_SERVICE").stream()
                .filter(a -> !a.getActivityAt().isBefore(eff) && !a.getActivityAt().isAfter(windowEnd)).toList();
        long mealDone = meals.stream().filter(a -> "REGISTER".equals(a.getRecordType())).count();
        long mealMissed = meals.stream().filter(a -> "MISSED".equals(a.getRecordType())).count();
        List<Long> mealWeekly = weeklyBuckets(eff, weekCount, meals.stream()
                .filter(a -> "REGISTER".equals(a.getRecordType())).map(CareActivity::getActivityAt).toList());
        Map<String, Object> meal = new LinkedHashMap<>();
        meal.put("checkIns", mealDone);
        meal.put("missed", mealMissed);
        meal.put("attendanceRate", (mealDone + mealMissed) == 0 ? null
                : Math.round(100.0 * mealDone / (mealDone + mealMissed)));
        meal.put("weekly", mealWeekly);
        m.put("meal", meal);

        // 3) 药盒提醒
        List<CareActivity> meds = activityRepo.findByPlanIdAndActivityTypeOrderByActivityAtAsc(planId, "MED_REMINDER").stream()
                .filter(a -> !a.getActivityAt().isBefore(eff) && !a.getActivityAt().isAfter(windowEnd)).toList();
        long medReminders = meds.stream().filter(a -> "REGISTER".equals(a.getRecordType())).count();
        long medTaken = meds.stream().filter(a -> Boolean.TRUE.equals(a.getMedicationTaken())).count();
        long medMissed = meds.stream().filter(a -> "MISSED".equals(a.getRecordType())
                || Boolean.FALSE.equals(a.getMedicationTaken())).count();
        List<Long> medWeekly = weeklyBuckets(eff, weekCount, meds.stream()
                .filter(a -> Boolean.TRUE.equals(a.getMedicationTaken())).map(CareActivity::getActivityAt).toList());
        Map<String, Object> med = new LinkedHashMap<>();
        med.put("reminders", medReminders);
        med.put("taken", medTaken);
        med.put("missed", medMissed);
        med.put("adherenceRate", medReminders == 0 ? null : Math.round(100.0 * medTaken / medReminders));
        med.put("weekly", medWeekly);
        m.put("medication", med);

        // 4) 家属接听率（事件家属沟通 + FAMILY_REACH 活动合并）
        List<FamilyCommunication> comms = commRepo.findByElderSince(plan.getElder().getId(), eff).stream()
                .filter(c -> !c.getCommunicatedAt().isAfter(windowEnd)).toList();
        List<CareActivity> reachActs = activityRepo.findByPlanIdAndActivityTypeOrderByActivityAtAsc(planId, "FAMILY_REACH").stream()
                .filter(a -> !a.getActivityAt().isBefore(eff) && !a.getActivityAt().isAfter(windowEnd)).toList();
        long fAttempts = comms.size() + reachActs.size();
        long fReached = comms.stream().filter(FamilyCommunication::isReached).count()
                + reachActs.stream().filter(a -> Boolean.TRUE.equals(a.getFamilyReached())).count();
        Map<String, Object> family = new LinkedHashMap<>();
        family.put("attempts", fAttempts);
        family.put("reached", fReached);
        family.put("reachRate", fAttempts == 0 ? null : (int) Math.round(100.0 * fReached / fAttempts));
        family.put("baselineReachRate", plan.getBaselineFamilyReachRate());
        family.put("delta", (fAttempts == 0 || plan.getBaselineFamilyReachRate() == null) ? null
                : (int) Math.round(100.0 * fReached / fAttempts) - plan.getBaselineFamilyReachRate());
        m.put("family", family);

        // 5) 综合判定（以夜间求助频次下降为核心）
        String verdict;
        if (daysElapsed < 7) {
            verdict = "OBSERVING";
        } else if (baselineWeekly <= 0.01) {
            verdict = nightTotal == 0 ? "IMPROVING" : "NO_CHANGE";
        } else {
            double ratio = currentWeekly / baselineWeekly;
            if (ratio <= 0.75) verdict = "IMPROVING";
            else if (ratio >= 1.25) verdict = "WORSENING";
            else verdict = "NO_CHANGE";
        }
        m.put("verdict", verdict);
        m.put("verdictLabel", verdictLabel(verdict));
        m.put("suggestConference", "NO_CHANGE".equals(verdict) || "WORSENING".equals(verdict));

        m.put("weekLabels", weekLabels(weekCount));
        return m;
    }

    @Transactional
    public CarePlan saveReview(Long planId, String result, String note) {
        CarePlan plan = mustPlan(planId);
        plan.setLastReviewAt(LocalDateTime.now());
        plan.setLastReviewResult(isBlank(result) ? (String) planEffect(planId).get("verdict") : result);
        plan.setLastReviewNote(note);
        planRepo.save(plan);
        writeArchive(plan.getElder(), "REVIEW", plan.getId(), plan.getPlanNo(),
                "长期照护方案效果复评：" + verdictLabel(plan.getLastReviewResult()),
                isBlank(note) ? "社工对观察指标进行复评" : note);
        return plan;
    }

    /* ===================== 个案会商（结论写回档案） ===================== */

    @Transactional
    public CaseConference createConference(ConferenceRequest req) {
        Elder elder = req.getElderId() == null ? null : elderRepo.findById(req.getElderId()).orElse(null);
        CarePlan plan = req.getPlanId() == null ? null : planRepo.findById(req.getPlanId()).orElse(null);
        if (elder == null && plan != null) elder = plan.getElder();
        if (elder == null) throw new ApiException("请指定老人或照护方案");

        CaseConference c = new CaseConference();
        c.setElder(elder);
        c.setPlan(plan);
        c.setSubject(req.getSubject());
        c.setLocation(req.getLocation());
        c.setSituation(req.getSituation());
        c.setConclusion(req.getConclusion());
        c.setPlanAdjusted(req.isPlanAdjusted());
        c.setAdjustmentNote(req.getAdjustmentNote());
        c.setConvenerName(UserContext.currentName());
        c.setHeldAt(LocalDateTime.now());
        c = conferenceRepo.save(c);

        for (ConferenceRequest.ParticipantInput p : req.getParticipants()) {
            if (isBlank(p.getName())) continue;
            ConferenceParticipant cp = new ConferenceParticipant();
            cp.setConference(c);
            cp.setRole(isBlank(p.getRole()) ? "OTHER" : p.getRole());
            cp.setName(p.getName());
            cp.setPhone(p.getPhone());
            cp.setAttended(p.isAttended());
            cp.setOpinion(p.getOpinion());
            participantRepo.save(cp);
        }

        if (!isBlank(req.getConclusion())) {
            writeBack(c);
        }
        sse.broadcast("care-update", Map.of("elderId", elder.getId(), "type", "CONFERENCE", "id", c.getId()));
        return c;
    }

    @Transactional
    public CaseConference updateConference(Long id, ConferenceRequest req) {
        CaseConference c = conferenceRepo.findById(id).orElseThrow(() -> new ApiException("会商记录不存在"));
        if (!isBlank(req.getSubject())) c.setSubject(req.getSubject());
        c.setLocation(req.getLocation());
        c.setSituation(req.getSituation());
        c.setConclusion(req.getConclusion());
        c.setPlanAdjusted(req.isPlanAdjusted());
        c.setAdjustmentNote(req.getAdjustmentNote());

        // 参与方以提交内容全量替换
        participantRepo.findByConferenceIdOrderByIdAsc(id).forEach(participantRepo::delete);
        for (ConferenceRequest.ParticipantInput p : req.getParticipants()) {
            if (isBlank(p.getName())) continue;
            ConferenceParticipant cp = new ConferenceParticipant();
            cp.setConference(c);
            cp.setRole(isBlank(p.getRole()) ? "OTHER" : p.getRole());
            cp.setName(p.getName());
            cp.setPhone(p.getPhone());
            cp.setAttended(p.isAttended());
            cp.setOpinion(p.getOpinion());
            participantRepo.save(cp);
        }

        if (!isBlank(req.getConclusion()) && !c.isWrittenBackToArchive()) {
            writeBack(c);
        }
        return c;
    }

    /** 会商协商结论写回老人档案，并标记方案已经会商调整 */
    private void writeBack(CaseConference c) {
        Elder elder = c.getElder();
        List<ConferenceParticipant> ps = participantRepo.findByConferenceIdOrderByIdAsc(c.getId());
        List<String> present = new ArrayList<>();
        for (ConferenceParticipant p : ps) {
            present.add(participantRoleLabel(p.getRole()) + " " + p.getName()
                    + (p.isAttended() ? "" : "（缺席）"));
        }
        writeArchive(elder, "CONFERENCE", c.getId(), null,
                "个案会商：" + c.getSubject(),
                "参与方：" + (present.isEmpty() ? "未登记" : String.join("、", present))
                        + "。协商结论：" + c.getConclusion()
                        + (c.isPlanAdjusted() ? "。已调整照护方案：" + nz(c.getAdjustmentNote()) : ""));
        c.setWrittenBackToArchive(true);
        conferenceRepo.save(c);

        if (c.getPlan() != null) {
            CarePlan plan = c.getPlan();
            plan.setReviewedAfterConference(true);
            plan.setLastReviewAt(java.time.LocalDateTime.now());
            plan.setLastReviewResult("NO_CHANGE");
            plan.setLastReviewNote("已发起个案会商并按协商结论调整，结论写回档案");
            planRepo.save(plan);
            elder.setUpdatedAt(java.time.LocalDateTime.now());
            elderRepo.save(elder);
        }
    }

    /* ===================== 查询 ===================== */

    @Transactional(readOnly = true)
    public Map<String, Object> planDetail(Long planId) {
        CarePlan plan = mustPlan(planId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("plan", plan);
        m.put("elder", plan.getElder());
        m.put("assessment", plan.getAssessment());
        m.put("items", itemRepo.findByPlanIdOrderByIdAsc(planId));
        m.put("activities", activityRepo.findByPlanIdOrderByActivityAtAsc(planId));
        m.put("conferences", conferenceRepo.findByPlanIdOrderByHeldAtDesc(planId));
        if (plan.getEffectiveAt() != null) {
            m.put("effect", planEffect(planId));
        }
        return m;
    }

    @Transactional(readOnly = true)
    public List<CarePlan> planList(String status, Long elderId) {
        if (elderId != null) return planRepo.findByElderIdOrderByCreatedAtDesc(elderId);
        if (!isBlank(status)) return planRepo.findByStatusOrderByCreatedAtDesc(status);
        return planRepo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<CareAssessment> assessments(Long elderId) {
        return assessmentRepo.findByElderIdOrderByAssessedAtDesc(elderId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> assessmentDetail(Long id) {
        CareAssessment a = assessmentRepo.findById(id).orElseThrow(() -> new ApiException("评估记录不存在"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("assessment", a);
        m.put("elder", a.getElder());
        m.put("plans", planRepo.findAll().stream()
                .filter(p -> p.getAssessment() != null && p.getAssessment().getId().equals(id)).toList());
        return m;
    }

    @Transactional(readOnly = true)
    public List<CaseConference> conferences(Long elderId) {
        if (elderId != null) return conferenceRepo.findByElderIdOrderByHeldAtDesc(elderId);
        List<CaseConference> all = new ArrayList<>();
        conferenceRepo.findAll().forEach(all::add);
        all.sort((x, y) -> y.getHeldAt().compareTo(x.getHeldAt()));
        return all;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> conferenceDetail(Long id) {
        CaseConference c = conferenceRepo.findById(id).orElseThrow(() -> new ApiException("会商记录不存在"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("conference", c);
        m.put("participants", participantRepo.findByConferenceIdOrderByIdAsc(id));
        return m;
    }

    @Transactional(readOnly = true)
    public List<ElderArchiveNote> archiveNotes(Long elderId) {
        return archiveRepo.findByElderIdOrderByCreatedAtDesc(elderId);
    }

    /* ===================== 工具 ===================== */

    private Map<String, Object> buildMetric(Integer baseline, int current, String unit,
                                            double baselineWeekly, double currentWeekly) {
        Map<String, Object> x = new LinkedHashMap<>();
        x.put("baseline", baseline);
        x.put("current", current);
        x.put("unit", unit);
        x.put("delta", baseline == null ? null : current - baseline);
        x.put("baselineWeekly", Math.round(baselineWeekly * 10) / 10.0);
        x.put("currentWeekly", Math.round(currentWeekly * 10) / 10.0);
        return x;
    }

    private List<Long> weeklyBuckets(LocalDateTime eff, int weekCount, List<LocalDateTime> times) {
        long[] buckets = new long[weekCount];
        for (LocalDateTime t : times) {
            long idx = ChronoUnit.DAYS.between(eff, t) / 7;
            if (idx >= 0 && idx < weekCount) buckets[(int) idx]++;
        }
        List<Long> r = new ArrayList<>();
        for (long b : buckets) r.add(b);
        return r;
    }

    private List<String> weekLabels(int weekCount) {
        List<String> labels = new ArrayList<>();
        for (int i = 1; i <= weekCount; i++) labels.add("第" + i + "周");
        return labels;
    }

    private void writeArchive(Elder elder, String type, Long refId, String refNo, String title, String content) {
        ElderArchiveNote n = new ElderArchiveNote();
        n.setElder(elder);
        n.setType(type);
        n.setRefId(refId);
        n.setRefNo(refNo);
        n.setTitle(title);
        n.setContent(content.length() > 1900 ? content.substring(0, 1900) : content);
        n.setOperatorName(UserContext.currentName());
        archiveRepo.save(n);
    }

    private Elder mustElder(Long id) {
        return elderRepo.findById(id).orElseThrow(() -> new ApiException(404, "老人不存在: " + id));
    }

    private CarePlan mustPlan(Long id) {
        return planRepo.findById(id).orElseThrow(() -> new ApiException(404, "照护方案不存在: " + id));
    }

    private String generatePlanNo() {
        String datePart = java.time.LocalDate.now().toString().replace("-", "").substring(2);
        return String.format("CP%s%03d", datePart, planRepo.count() + 1);
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
    private static String nz(Object o) { return o == null ? "无数据" : String.valueOf(o); }
    private static String firstNonBlank(String... ss) {
        for (String s : ss) if (s != null && !s.isBlank()) return s;
        return null;
    }
    private static String yn(boolean b) { return b ? "是" : "否"; }

    private String itemListBrief(Long planId) {
        List<String> parts = new ArrayList<>();
        for (CarePlanItem item : itemRepo.findByPlanIdOrderByIdAsc(planId)) {
            parts.add(itemTypeLabel(item.getItemType())
                    + (item.getFrequency() != null ? "（" + item.getFrequency() + "）" : ""));
        }
        return parts.isEmpty() ? "未配置服务项" : String.join("、", parts);
    }

    public static String itemTypeLabel(String s) {
        if (s == null) return "";
        return switch (s) {
            case "REGULAR_VISIT" -> "定期探访";
            case "PSYCH_COMPANION" -> "心理陪伴";
            case "MEAL_SERVICE" -> "助餐";
            case "FAMILY_DOCTOR" -> "家庭医生随访";
            case "MED_REMINDER" -> "药盒提醒";
            case "FAMILY_REACH" -> "家属联系";
            case "OTHER" -> "其他";
            default -> s;
        };
    }

    public static String itemTypesLabel(String csv) {
        if (csv == null || csv.isBlank()) return "无";
        List<String> parts = new ArrayList<>();
        for (String s : csv.split(",")) if (!s.isBlank()) parts.add(itemTypeLabel(s));
        return parts.isEmpty() ? "无" : String.join("、", parts);
    }

    public static String familyLabel(String s) {
        return switch (s == null ? "" : s) {
            case "GOOD" -> "参与良好";
            case "AVERAGE" -> "参与一般";
            case "POOR" -> "参与不足";
            default -> s;
        };
    }

    public static String levelLabel(String s) {
        return switch (s == null ? "" : s) {
            case "LIGHT" -> "轻度关怀";
            case "MEDIUM" -> "重点照护";
            case "INTENSIVE" -> "强化照护";
            default -> s;
        };
    }

    public static String verdictLabel(String s) {
        return switch (s == null ? "" : s) {
            case "IMPROVING" -> "改善中";
            case "NO_CHANGE" -> "无明显变化";
            case "WORSENING" -> "未见改善且恶化";
            case "OBSERVING" -> "观察数据积累中";
            default -> s;
        };
    }

    public static String participantRoleLabel(String s) {
        return switch (s == null ? "" : s) {
            case "SOCIAL_WORKER" -> "社工";
            case "ELDER_ADVISOR" -> "养老顾问";
            case "COMMUNITY_DOCTOR" -> "社区医生";
            case "FAMILY" -> "家属";
            case "GRID_WORKER" -> "网格员";
            case "OTHER" -> "其他";
            default -> s;
        };
    }
}
