package com.community.eldercare.service;

import com.community.eldercare.common.ApiException;
import com.community.eldercare.domain.*;
import com.community.eldercare.dto.*;
import com.community.eldercare.notify.SseService;
import com.community.eldercare.repo.*;
import com.community.eldercare.security.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 长期照护方案服务。
 *
 * 频繁夜间求助老人 → 社工发起长期照护评估 → 制定并生效方案（定期探访、心理陪伴、
 * 助餐、家庭医生随访）→ 持续观察夜间求助、助餐签到、药盒提醒、家属接听率 →
 * 无改善时发起个案会商，养老顾问/社区医生/家属协商结论写回老人档案。
 */
@Service
public class CarePlanService {

    private final LongTermCarePlanRepository planRepo;
    private final PlanServiceItemRepository itemRepo;
    private final PlanServiceExecutionRepository executionRepo;
    private final MealCheckinRepository mealRepo;
    private final MedicationReminderRepository medRepo;
    private final FamilyCallRecordRepository callRepo;
    private final CaseConferenceRepository conferenceRepo;
    private final PlanLogRepository logRepo;
    private final ElderRepository elderRepo;
    private final EmergencyContactRepository contactRepo;
    private final HelpEventRepository eventRepo;
    private final FamilyCommunicationRepository commRepo;
    private final OnsiteVisitRepository onsiteRepo;
    private final SseService sse;

    public CarePlanService(LongTermCarePlanRepository planRepo, PlanServiceItemRepository itemRepo,
                           PlanServiceExecutionRepository executionRepo, MealCheckinRepository mealRepo,
                           MedicationReminderRepository medRepo, FamilyCallRecordRepository callRepo,
                           CaseConferenceRepository conferenceRepo, PlanLogRepository logRepo,
                           ElderRepository elderRepo, EmergencyContactRepository contactRepo,
                           HelpEventRepository eventRepo, FamilyCommunicationRepository commRepo,
                           OnsiteVisitRepository onsiteRepo, SseService sse) {
        this.planRepo = planRepo;
        this.itemRepo = itemRepo;
        this.executionRepo = executionRepo;
        this.mealRepo = mealRepo;
        this.medRepo = medRepo;
        this.callRepo = callRepo;
        this.conferenceRepo = conferenceRepo;
        this.logRepo = logRepo;
        this.elderRepo = elderRepo;
        this.contactRepo = contactRepo;
        this.eventRepo = eventRepo;
        this.commRepo = commRepo;
        this.onsiteRepo = onsiteRepo;
        this.sse = sse;
    }

    /* ===================== 评估 + 方案 ===================== */

    /** 发起长期照护评估，同时录入评估结论、目标与拟定服务项（方案为 DRAFT，待生效） */
    @Transactional
    public LongTermCarePlan initiateAssessment(CarePlanRequest req) {
        Elder elder = elderRepo.findById(req.getElderId())
                .orElseThrow(() -> new ApiException(404, "老人不存在: " + req.getElderId()));

        planRepo.findFirstByElderIdAndStatusOrderByCreatedAtDesc(elder.getId(), "ACTIVE")
                .ifPresent(p -> {
                    throw new ApiException("该老人生效中的长期照护方案 " + p.getPlanNo() + " 尚未结束，不能重复发起");
                });

        LocalDateTime now = LocalDateTime.now();
        LongTermCarePlan plan = new LongTermCarePlan();
        plan.setPlanNo(generateNo("P"));
        plan.setElder(elder);
        plan.setStatus("DRAFT");
        plan.setInitiatorName(UserContext.currentName());
        plan.setInitiatedAt(now);

        long night30 = eventRepo.findNightHelpsSince(elder.getId(), now.minusDays(30)).size();
        String trigger = req.getTriggerReason();
        if (trigger == null || trigger.isBlank()) {
            trigger = "近30天夜间求助 " + night30 + " 次，社工发起长期照护评估";
        }
        plan.setTriggerReason(trigger);
        plan.setLonelinessScore(req.getLonelinessScore());
        plan.setMedicationScore(req.getMedicationScore());
        plan.setFamilySupportScore(req.getFamilySupportScore());
        plan.setSelfCareScore(req.getSelfCareScore());
        plan.setAssessmentConclusion(req.getAssessmentConclusion());
        plan.setGoals(req.getGoals());
        plan.setObserveDays(req.getObserveDays() == null || req.getObserveDays() <= 0 ? 30 : req.getObserveDays());
        snapshotBaseline(plan, elder, now);
        plan = planRepo.save(plan);

        List<String> itemDesc = new ArrayList<>();
        if (req.getItems() != null) {
            for (CarePlanRequest.ItemDraft d : req.getItems()) {
                if (d.getServiceType() == null || d.getProviderName() == null) {
                    continue;
                }
                PlanServiceItem item = new PlanServiceItem();
                item.setPlan(plan);
                item.setServiceType(d.getServiceType());
                item.setProviderName(d.getProviderName());
                item.setProviderPhone(d.getProviderPhone());
                item.setFrequency(d.getFrequency() == null || d.getFrequency().isBlank() ? "按需" : d.getFrequency());
                item.setContent(d.getContent());
                if (d.getScheduledStartAt() != null) {
                    item.setScheduledStartAt(d.getScheduledStartAt());
                }
                itemRepo.save(item);
                itemDesc.add(planServiceLabel(d.getServiceType()) + "（" + d.getProviderName() + "，" + item.getFrequency() + "）");
            }
        }

        addLog(plan, "ASSESS", "社工 " + plan.getInitiatorName() + " 发起长期照护评估：" + trigger
                + (req.getAssessmentConclusion() != null ? "。评估结论：" + req.getAssessmentConclusion() : ""),
                plan.getInitiatorName());
        if (!itemDesc.isEmpty()) {
            addLog(plan, "PLAN", "拟定长期服务安排：" + String.join("；", itemDesc), plan.getInitiatorName());
        }
        broadcast("care-plan-update", plan.getId());
        return plan;
    }

    /** 方案生效，进入观察期 */
    @Transactional
    public LongTermCarePlan activate(Long planId) {
        LongTermCarePlan plan = mustPlan(planId);
        if (!"DRAFT".equals(plan.getStatus())) {
            throw new ApiException("仅待生效方案可以生效，当前状态 " + plan.getStatus());
        }
        LocalDateTime now = LocalDateTime.now();
        plan.setStatus("ACTIVE");
        plan.setEffectiveAt(now);
        plan.setUpdatedAt(now);
        // 生效瞬间刷新一次基线（评估到生效之间可能又有夜间求助）
        snapshotBaseline(plan, plan.getElder(), now);
        planRepo.save(plan);

        Elder elder = plan.getElder();
        elder.setLongTermCareNote(appendNote(elder.getLongTermCareNote(),
                now.toLocalDate() + " 长期照护方案 " + plan.getPlanNo() + " 生效，观察期 "
                        + plan.getObserveDays() + " 天（" + plan.getInitiatorName() + "发起）"));
        elder.setUpdatedAt(now);
        elderRepo.save(elder);

        List<PlanServiceItem> items = itemRepo.findByPlanIdOrderByIdAsc(planId);
        items.forEach(i -> {
            i.setStatus("ACTIVE");
            itemRepo.save(i);
            if ("MEAL_SERVICE".equals(i.getServiceType())) {
                elder.setSuggestMealService(true);
            } else if ("PSYCH_COMPANION".equals(i.getServiceType())) {
                elder.setSuggestPsychCompanion(true);
            } else if ("REGULAR_VISIT".equals(i.getServiceType())) {
                elder.setSuggestDailyVisit(true);
            }
        });
        elderRepo.save(elder);

        addLog(plan, "PLAN", "长期照护方案生效，进入 " + plan.getObserveDays()
                + " 天观察期。基线（前30天）：夜间求助 " + nz(plan.getBaselineNightHelps())
                + " 次、家属接听率 " + nz(plan.getBaselineFamilyAnswerRate())
                + "%、助餐签到 " + nz(plan.getBaselineMealCheckins())
                + " 次、药盒确认 " + nz(plan.getBaselineMedReminders()) + " 次", UserContext.currentName());
        broadcast("care-plan-update", planId);
        return plan;
    }

    @Transactional
    public LongTermCarePlan endPlan(Long planId, String reason) {
        LongTermCarePlan plan = mustPlan(planId);
        LocalDateTime now = LocalDateTime.now();
        plan.setStatus("ENDED");
        plan.setEndedAt(now);
        plan.setEndReason(reason);
        plan.setUpdatedAt(now);
        planRepo.save(plan);
        addLog(plan, "NOTE", "方案结束：" + nz(reason, "未填写原因"), UserContext.currentName());
        broadcast("care-plan-update", planId);
        return plan;
    }

    @Transactional
    public PlanServiceItem addItem(Long planId, PlanItemRequest req) {
        LongTermCarePlan plan = mustPlan(planId);
        PlanServiceItem item = new PlanServiceItem();
        item.setPlan(plan);
        item.setServiceType(req.getServiceType());
        item.setProviderName(req.getProviderName());
        item.setProviderPhone(req.getProviderPhone());
        item.setFrequency(req.getFrequency() == null || req.getFrequency().isBlank() ? "按需" : req.getFrequency());
        item.setContent(req.getContent());
        item.setStatus("ACTIVE".equals(plan.getStatus()) ? "ACTIVE" : "ACTIVE");
        item = itemRepo.save(item);
        addLog(plan, "PLAN", "追加服务安排：" + planServiceLabel(req.getServiceType())
                + "（" + req.getProviderName() + "，" + item.getFrequency() + "）", UserContext.currentName());
        broadcast("care-plan-update", planId);
        return item;
    }

    @Transactional
    public PlanServiceItem updateItemStatus(Long itemId, String status) {
        PlanServiceItem item = itemRepo.findById(itemId).orElseThrow(() -> new ApiException(404, "服务项不存在"));
        item.setStatus(status);
        itemRepo.save(item);
        addLog(item.getPlan(), "PLAN", planServiceLabel(item.getServiceType()) + " 安排状态调整为 "
                + itemStatusLabel(status), UserContext.currentName());
        broadcast("care-plan-update", item.getPlan().getId());
        return item;
    }

    /* ===================== 服务执行（探访 / 心理陪伴 / 家庭医生随访） ===================== */

    @Transactional
    public PlanServiceExecution registerExecution(Long planId, ServiceExecutionRequest req) {
        LongTermCarePlan plan = mustPlan(planId);
        PlanServiceExecution ex = new PlanServiceExecution();
        ex.setPlan(plan);
        ex.setElder(plan.getElder());
        String type = req.getServiceType();
        PlanServiceItem item = null;
        if (req.getItemId() != null) {
            item = itemRepo.findById(req.getItemId())
                    .orElseThrow(() -> new ApiException(404, "服务项不存在"));
            if (!item.getPlan().getId().equals(planId)) {
                throw new ApiException("服务项不属于该方案");
            }
            type = item.getServiceType();
        }
        if (type == null || type.isBlank()) {
            throw new ApiException("服务类型不能为空");
        }
        ex.setItem(item);
        ex.setServiceType(type);
        ex.setExecutorName(req.getExecutorName() == null || req.getExecutorName().isBlank()
                ? UserContext.currentName() : req.getExecutorName());
        ex.setExecutorPhone(req.getExecutorPhone());
        ex.setContent(req.getContent());
        ex.setDoctorConclusion(req.getDoctorConclusion());
        if (req.getExecutedAt() != null) {
            ex.setExecutedAt(req.getExecutedAt());
        }
        ex = executionRepo.save(ex);

        if (item != null) {
            item.setExecutedCount(item.getExecutedCount() + 1);
            item.setLastExecutedAt(ex.getExecutedAt());
            itemRepo.save(item);
        }

        // 家庭医生随访结论同步写回老人档案
        if ("FAMILY_DOCTOR".equals(type) && req.getDoctorConclusion() != null && !req.getDoctorConclusion().isBlank()) {
            Elder elder = plan.getElder();
            elder.setLastDoctorFollowUpDate(ex.getExecutedAt().toLocalDate());
            elder.setDoctorFollowUpConclusion(req.getDoctorConclusion() + "（长期照护方案随访 " + plan.getPlanNo() + "）");
            elder.setUpdatedAt(LocalDateTime.now());
            elderRepo.save(elder);
        }

        addLog(plan, "SERVICE", planServiceLabel(type) + "执行一次：" + ex.getExecutorName()
                + (req.getContent() != null ? "，情况：" + req.getContent() : "")
                + (req.getDoctorConclusion() != null ? "，随访结论：" + req.getDoctorConclusion() : ""),
                ex.getExecutorName());
        broadcast("care-plan-update", planId);
        return ex;
    }

    /* ===================== 助餐签到 ===================== */

    @Transactional
    public MealCheckin mealCheckin(Long planId, MealCheckinRequest req) {
        LongTermCarePlan plan = mustPlan(planId);
        MealCheckin m = new MealCheckin();
        m.setElder(plan.getElder());
        m.setPlan(plan);
        m.setMealDate(req.getMealDate() == null ? LocalDate.now() : req.getMealDate());
        m.setMealType(req.getMealType() == null || req.getMealType().isBlank() ? "午餐" : req.getMealType());
        m.setDeliveryMode(req.getDeliveryMode() == null || req.getDeliveryMode().isBlank() ? "DINE_IN" : req.getDeliveryMode());
        boolean checked = req.getCheckedIn() == null || req.getCheckedIn();
        m.setCheckedIn(checked);
        m.setCheckedAt(checked ? LocalDateTime.now() : null);
        m.setOperatorName(UserContext.currentName());
        m.setNote(req.getNote());
        m = mealRepo.save(m);
        addLog(plan, "MEAL", "助餐" + (checked ? "签到" : "登记未用餐") + "：" + m.getMealDate()
                + " " + m.getMealType()
                + ("DELIVERY".equals(m.getDeliveryMode()) ? "（配送上门）" : "（堂食）")
                + (req.getNote() != null ? "，" + req.getNote() : ""), m.getOperatorName());
        broadcast("care-plan-update", planId);
        return m;
    }

    /* ===================== 药盒提醒 ===================== */

    @Transactional
    public MedicationReminder createReminder(Long planId, MedicationReminderRequest req) {
        LongTermCarePlan plan = mustPlan(planId);
        MedicationReminder r = new MedicationReminder();
        r.setElder(plan.getElder());
        r.setPlan(plan);
        r.setMedicineName(req.getMedicineName() == null ? "常规药品" : req.getMedicineName());
        r.setDosageSlot(req.getDosageSlot() == null || req.getDosageSlot().isBlank() ? "早" : req.getDosageSlot());
        r.setRemindAt(req.getRemindAt() == null ? LocalDateTime.now() : req.getRemindAt());
        r.setStatus(req.getStatus() == null || req.getStatus().isBlank() ? "PENDING" : req.getStatus());
        r.setConfirmSource(req.getConfirmSource());
        r.setNote(req.getNote());
        if ("CONFIRMED".equals(r.getStatus())) {
            r.setConfirmedAt(LocalDateTime.now());
        }
        r = medRepo.save(r);
        addLog(plan, "MEDICATION", "药盒提醒：" + r.getMedicineName() + "（" + r.getDosageSlot()
                + "药）" + medStatusLabel(r.getStatus())
                + (req.getConfirmSource() != null ? "，确认来源 " + req.getConfirmSource() : ""), UserContext.currentName());
        broadcast("care-plan-update", planId);
        return r;
    }

    @Transactional
    public MedicationReminder updateReminder(Long reminderId, String status, String confirmSource) {
        MedicationReminder r = medRepo.findById(reminderId)
                .orElseThrow(() -> new ApiException(404, "提醒记录不存在"));
        r.setStatus(status);
        if ("CONFIRMED".equals(status)) {
            r.setConfirmedAt(LocalDateTime.now());
            r.setConfirmSource(confirmSource == null ? "老人按键确认" : confirmSource);
        }
        medRepo.save(r);
        if (r.getPlan() != null) {
            addLog(r.getPlan(), "MEDICATION", "药盒提醒：" + r.getMedicineName() + "（" + r.getDosageSlot()
                    + "药）状态更新为 " + medStatusLabel(status)
                    + (r.getConfirmSource() != null ? "，确认来源 " + r.getConfirmSource() : ""), UserContext.currentName());
            broadcast("care-plan-update", r.getPlan().getId());
        }
        return r;
    }

    /* ===================== 家属接听观察 ===================== */

    @Transactional
    public FamilyCallRecord familyCall(Long planId, FamilyCallRequest req) {
        LongTermCarePlan plan = mustPlan(planId);
        FamilyCallRecord c = new FamilyCallRecord();
        c.setElder(plan.getElder());
        c.setPlan(plan);
        c.setContactName(req.getContactName());
        c.setContactPhone(req.getContactPhone());
        c.setRelationship(req.getRelationship());
        c.setChannel(req.getChannel() == null || req.getChannel().isBlank() ? "电话" : req.getChannel());
        c.setReached(req.isReached());
        c.setAttitude(req.getAttitude());
        c.setNote(req.getNote());
        c.setOperatorName(UserContext.currentName());
        c = callRepo.save(c);

        // 同步累计紧急联系人接通统计（与事件家属沟通口径一致）
        if (req.getContactId() != null) {
            contactRepo.findById(req.getContactId()).ifPresent(ec -> {
                ec.setContactAttempts(ec.getContactAttempts() + 1);
                if (req.isReached()) {
                    ec.setContactReached(ec.getContactReached() + 1);
                }
                if (ec.getContactAttempts() >= 3
                        && (ec.getContactAttempts() - ec.getContactReached()) * 3 >= ec.getContactAttempts() * 2) {
                    ec.setOftenUnreachable(true);
                }
                contactRepo.save(ec);
            });
        }

        addLog(plan, "FAMILY", "主动联系家属 " + req.getContactName()
                + (req.getRelationship() != null ? "（" + req.getRelationship() + "）" : "")
                + "：" + (req.isReached() ? "已接通" + (req.getAttitude() != null ? "，态度 " + req.getAttitude() : "") : "未接通")
                + (req.getNote() != null ? "，" + req.getNote() : ""), c.getOperatorName());
        broadcast("care-plan-update", planId);
        return c;
    }

    /* ===================== 个案会商 ===================== */

    @Transactional(readOnly = true)
    public CaseConference getConference(Long conferenceId) {
        return conferenceRepo.findById(conferenceId)
                .orElseThrow(() -> new ApiException(404, "个案会商不存在"));
    }

    @Transactional
    public CaseConference initiateConference(CaseConferenceRequest req) {
        Elder elder;
        LongTermCarePlan plan = null;
        if (req.getPlanId() != null) {
            plan = mustPlan(req.getPlanId());
            elder = plan.getElder();
        } else if (req.getElderId() != null) {
            elder = elderRepo.findById(req.getElderId())
                    .orElseThrow(() -> new ApiException(404, "老人不存在"));
        } else {
            throw new ApiException("必须指定老人或长期照护方案");
        }

        CaseConference c = new CaseConference();
        c.setConferenceNo(generateNo("C"));
        c.setElder(elder);
        c.setPlan(plan);
        c.setConvenerName(UserContext.currentName());
        c.setReason(req.getReason());
        c.setParticipants(req.getParticipants());
        c.setScheduledAt(req.getScheduledAt() == null ? LocalDateTime.now().plusDays(2) : req.getScheduledAt());
        c.setStatus("SCHEDULED");
        c = conferenceRepo.save(c);

        if (plan != null) {
            addLog(plan, "CONFERENCE", "社工会同发起个案会商 " + c.getConferenceNo()
                    + "，参与方：" + nz(req.getParticipants(), "待确认")
                    + "。原因：" + nz(req.getReason(), "方案观察指标未见改善"), c.getConvenerName());
            broadcast("care-plan-update", plan.getId());
        }
        return c;
    }

    /** 填写会商结论并写回老人档案 */
    @Transactional
    public CaseConference concludeConference(Long conferenceId, ConferenceConclusionRequest req) {        CaseConference c = conferenceRepo.findById(conferenceId)
                .orElseThrow(() -> new ApiException(404, "个案会商不存在"));
        c.setStatus("HELD");
        c.setHeldAt(LocalDateTime.now());
        c.setConclusion(req.getConclusion());
        c.setActionItems(req.getActionItems());
        c.setWrittenBackToProfile(true);
        conferenceRepo.save(c);

        Elder elder = c.getElder();
        LocalDate today = c.getHeldAt().toLocalDate();
        elder.setLastCaseConferenceDate(today);
        String summary = today + " 个案会商 " + c.getConferenceNo()
                + "（参与方：" + nz(c.getParticipants(), "未登记") + "）\n"
                + "协商结论：" + nz(req.getConclusion(), "无") + "\n"
                + "后续行动：" + nz(req.getActionItems(), "无");
        elder.setCaseConferenceNote(appendNote(elder.getCaseConferenceNote(), summary));
        elder.setLongTermCareNote(appendNote(elder.getLongTermCareNote(),
                today + " 个案会商 " + c.getConferenceNo() + " 结论已写回档案"));

        // 按行动安排关键词联动服务建议
        String actions = nz(req.getActionItems(), "") + nz(req.getConclusion(), "");
        if (actions.contains("心理")) {
            elder.setSuggestPsychCompanion(true);
        }
        if (actions.contains("助餐") || actions.contains("用餐") || actions.contains("送餐")) {
            elder.setSuggestMealService(true);
        }
        if (actions.contains("巡访") || actions.contains("探访")) {
            elder.setSuggestDailyVisit(true);
        }
        if (actions.contains("适老") || actions.contains("扶手") || actions.contains("改造")) {
            elder.setSuggestAgingRetrofit(true);
        }
        elder.setUpdatedAt(LocalDateTime.now());
        elderRepo.save(elder);

        LongTermCarePlan plan = c.getPlan();
        if (plan != null) {
            plan.setLastConference(c);
            if (Boolean.TRUE.equals(req.getAdjustPlan())) {
                plan.setStatus("ADJUSTED");
                plan.setEndReason("个案会商 " + c.getConferenceNo() + " 后调整方案");
                plan.setEndedAt(c.getHeldAt());
                addLog(plan, "CONFERENCE", "会商结论：" + nz(req.getConclusion(), "无")
                        + "。后续行动：" + nz(req.getActionItems(), "无")
                        + "。结论已写回老人档案，原方案标记为已调整，可按结论发起新方案", c.getConvenerName());
            } else {
                addLog(plan, "CONFERENCE", "会商结论：" + nz(req.getConclusion(), "无")
                        + "。后续行动：" + nz(req.getActionItems(), "无") + "。结论已写回老人档案", c.getConvenerName());
            }
            plan.setUpdatedAt(LocalDateTime.now());
            planRepo.save(plan);
            broadcast("care-plan-update", plan.getId());
        }
        return c;
    }

    /* ===================== 查询 ===================== */

    @Transactional(readOnly = true)
    public List<LongTermCarePlan> listPlans(Long elderId, String status) {
        if (elderId != null && status != null && !status.isBlank()) {
            return "ACTIVE".equals(status)
                    ? planRepo.findFirstByElderIdAndStatusOrderByCreatedAtDesc(elderId, status)
                    .map(List::of).orElseGet(List::of)
                    : planRepo.findByElderIdOrderByCreatedAtDesc(elderId).stream()
                    .filter(p -> status.equals(p.getStatus())).toList();
        }
        if (elderId != null) {
            return planRepo.findByElderIdOrderByCreatedAtDesc(elderId);
        }
        if (status != null && !status.isBlank()) {
            return planRepo.findByStatusOrderByCreatedAtDesc(status);
        }
        return planRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> planDetail(Long planId) {
        LongTermCarePlan plan = mustPlan(planId);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("plan", plan);
        m.put("elder", plan.getElder());
        m.put("items", itemRepo.findByPlanIdOrderByIdAsc(planId));
        m.put("executions", executionRepo.findByPlanIdOrderByExecutedAtDesc(planId));
        m.put("meals", mealRepo.findByPlanIdOrderByMealDateDescIdDesc(planId));
        m.put("reminders", medRepo.findByPlanIdOrderByRemindAtDesc(planId));
        m.put("familyCalls", callRepo.findByPlanIdOrderByCalledAtDesc(planId));
        m.put("conferences", conferenceRepo.findByPlanIdOrderByCreatedAtDesc(planId));
        m.put("logs", logRepo.findByPlanIdOrderByCreatedAtAsc(planId));
        m.put("observation", observation(planId));
        return m;
    }

    /**
     * 方案效果观察：夜间求助次数、助餐签到、药盒提醒、家属接听率的变化。
     * 当前窗口与“方案生效前相同时长”窗口对比，避免观察天数不同造成误判。
     */
    @Transactional(readOnly = true)
    public Map<String, Object> observation(Long planId) {
        LongTermCarePlan plan = mustPlan(planId);
        Map<String, Object> m = new LinkedHashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = plan.getEffectiveAt();

        m.put("planId", plan.getId());
        m.put("planNo", plan.getPlanNo());
        m.put("status", plan.getStatus());
        m.put("observeDays", plan.getObserveDays());
        m.put("baseline", Map.of(
                "nightHelps30", nz(plan.getBaselineNightHelps()),
                "familyAnswerRate", nz(plan.getBaselineFamilyAnswerRate()),
                "mealCheckins30", nz(plan.getBaselineMealCheckins()),
                "medConfirmed30", nz(plan.getBaselineMedReminders())
        ));

        if (start == null) {
            m.put("active", false);
            m.put("message", "方案尚未生效，生效后开始观察");
            return m;
        }

        long elapsedDays = Math.max(1, java.time.Duration.between(start, now).toDays() + 1);
        long compareDays = Math.min(elapsedDays, plan.getObserveDays());
        LocalDateTime currentFrom = start;
        LocalDateTime beforeFrom = start.minusDays(compareDays);

        // 1. 夜间求助
        long nightCurrent = eventRepo.findNightHelpsSince(plan.getElder().getId(), currentFrom).stream()
                .filter(e -> e.getCreatedAt().isBefore(now)).count();
        long nightBefore = eventRepo.findNightHelpsSince(plan.getElder().getId(), beforeFrom).stream()
                .filter(e -> e.getCreatedAt().isBefore(start)).count();
        // 2. 助餐签到
        long mealsCurrent = mealRepo.findByPlanIdOrderByMealDateDescIdDesc(planId).stream()
                .filter(x -> x.isCheckedIn() && x.getCheckedAt() != null
                        && !x.getCheckedAt().isBefore(start)).count();
        long mealsBefore = mealRepo.countByElderIdAndCheckedInTrueAndCheckedAtBetween(
                plan.getElder().getId(), beforeFrom, start);
        // 3. 药盒提醒
        List<MedicationReminder> medsCurrent = medRepo.findByPlanIdOrderByRemindAtDesc(planId).stream()
                .filter(r -> !r.getRemindAt().isBefore(start)).toList();
        long medConfirmed = medsCurrent.stream().filter(r -> "CONFIRMED".equals(r.getStatus())).count();
        long medMissed = medsCurrent.stream().filter(r -> "MISSED".equals(r.getStatus())).count();
        long medPending = medsCurrent.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long medAdherence = medsCurrent.isEmpty() ? 0
                : Math.round(100.0 * medConfirmed / medsCurrent.size());
        long medConfirmedBefore = medRepo.countByElderIdAndStatusAndRemindAtBetween(
                plan.getElder().getId(), "CONFIRMED", beforeFrom, start);
        // 4. 家属接听率
        List<FamilyCallRecord> callsCurrent = callRepo.findByPlanIdOrderByCalledAtDesc(planId).stream()
                .filter(c -> !c.getCalledAt().isBefore(start)).toList();
        long callReached = callsCurrent.stream().filter(FamilyCallRecord::isReached).count();
        long answerRate = callsCurrent.isEmpty() ? 0
                : Math.round(100.0 * callReached / callsCurrent.size());
        List<FamilyCommunication> commsBefore = commRepo.findByElderSince(plan.getElder().getId(), beforeFrom)
                .stream().filter(c -> c.getCommunicatedAt().isBefore(start)).toList();
        long answerBefore = commsBefore.isEmpty() ? 0
                : Math.round(100.0 * commsBefore.stream().filter(FamilyCommunication::isReached).count()
                / (double) commsBefore.size());

        boolean nightDecreased = nightCurrent < nightBefore;
        boolean familyRateImproved = callsCurrent.isEmpty()
                ? plan.getBaselineFamilyAnswerRate() != null && answerRate >= plan.getBaselineFamilyAnswerRate()
                : answerRate >= answerBefore;
        boolean mealActive = mealsCurrent > 0;
        boolean medicationManaged = medMissed == 0 && medConfirmed > 0;
        // 无改善判定：夜间求助没有下降（核心指标）
        boolean noImprovement = !nightDecreased && compareDays >= 3;

        m.put("active", true);
        m.put("effectiveAt", start);
        m.put("elapsedDays", elapsedDays);
        m.put("compareDays", compareDays);
        m.put("nightHelp", Map.of(
                "current", nightCurrent,
                "beforeSameWindow", nightBefore,
                "baseline30", nz(plan.getBaselineNightHelps()),
                "decreased", nightDecreased
        ));
        m.put("mealCheckin", Map.of(
                "current", mealsCurrent,
                "beforeSameWindow", mealsBefore,
                "active", mealActive
        ));
        m.put("medication", Map.of(
                "confirmed", medConfirmed,
                "missed", medMissed,
                "pending", medPending,
                "adherenceRate", medAdherence,
                "confirmedBefore", medConfirmedBefore,
                "managed", medicationManaged
        ));
        m.put("familyAnswer", Map.of(
                "calls", callsCurrent.size(),
                "reached", callReached,
                "answerRate", answerRate,
                "beforeSameWindow", answerBefore,
                "improved", familyRateImproved
        ));

        // 服务执行情况
        Map<String, Long> executionByType = new LinkedHashMap<>();
        for (String t : List.of("REGULAR_VISIT", "PSYCH_COMPANION", "FAMILY_DOCTOR")) {
            executionByType.put(t, executionRepo.countByPlanIdAndServiceTypeAndExecutedAtBetween(
                    planId, t, start, now.plusDays(1)));
        }
        m.put("executions", executionByType);

        m.put("nightDecreased", nightDecreased);
        m.put("familyRateImproved", familyRateImproved);
        m.put("mealActive", mealActive);
        m.put("medicationManaged", medicationManaged);
        m.put("noImprovement", noImprovement);
        m.put("improved", nightDecreased);
        return m;
    }

    /**
     * 老人夜间求助聚合视图：社区端查看同一老人一周内（默认近30天）每次夜间求助的
     * 原因、到场结果、孤独焦虑、药物管理混乱、家属参与情况。
     */
    @Transactional(readOnly = true)
    public Map<String, Object> nightHelpSummary(Long elderId, int days) {
        Elder elder = elderRepo.findById(elderId)
                .orElseThrow(() -> new ApiException(404, "老人不存在"));
        LocalDateTime since = LocalDateTime.now().minusDays(days <= 0 ? 30 : days);
        List<HelpEvent> nightEvents = eventRepo.findNightHelpsSince(elderId, since);

        List<Map<String, Object>> details = new ArrayList<>();
        int lonely = 0, medChaos = 0, familyInvolved = 0;
        for (HelpEvent e : nightEvents) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("eventId", e.getId());
            row.put("eventNo", e.getEventNo());
            row.put("createdAt", e.getCreatedAt());
            row.put("source", e.getSource());
            row.put("voiceContent", e.getVoiceContent());
            row.put("triageType", e.getTriageType());
            row.put("severity", e.getSeverity());
            row.put("status", e.getStatus());
            row.put("riskFlags", e.getRiskFlags());

            List<OnsiteVisit> visits = onsiteVisits(e.getId());
            row.put("visits", visits);
            boolean hasLonely = visits.stream().anyMatch(OnsiteVisit::isLonelyAnxious);
            boolean hasMed = visits.stream().anyMatch(OnsiteVisit::isMedicationChaos);
            boolean hasFamily = visits.stream().anyMatch(OnsiteVisit::isFamilyInvolved);
            if (hasLonely) lonely++;
            if (hasMed) medChaos++;
            if (hasFamily) familyInvolved++;
            row.put("lonelyAnxious", hasLonely);
            row.put("medicationChaos", hasMed);
            row.put("familyInvolved", hasFamily);

            List<FamilyCommunication> comms = commRepo.findByEventIdOrderByCommunicatedAtAsc(e.getId());
            long attempts = comms.size();
            long reached = comms.stream().filter(FamilyCommunication::isReached).count();
            Map<String, Object> family = new LinkedHashMap<>();
            family.put("attempts", attempts);
            family.put("reached", reached);
            family.put("answerRate", attempts == 0 ? null : Math.round(100.0 * reached / attempts));
            family.put("contacts", comms);
            row.put("familyCommunication", family);
            details.add(row);
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("elderId", elderId);
        m.put("elderName", elder.getName());
        m.put("days", days);
        m.put("totalNightHelps", nightEvents.size());
        m.put("weekCount", nightEvents.stream().filter(e -> e.getCreatedAt().isAfter(LocalDateTime.now().minusDays(7))).count());
        m.put("lonelyAnxiousCount", lonely);
        m.put("medicationChaosCount", medChaos);
        m.put("familyInvolvedCount", familyInvolved);
        m.put("events", details);
        m.put("activePlan", planRepo.findFirstByElderIdAndStatusOrderByCreatedAtDesc(elderId, "ACTIVE").orElse(null));
        return m;
    }

    private List<OnsiteVisit> onsiteVisits(Long eventId) {
        return onsiteRepo.findByEventIdOrderByArrivedAtAsc(eventId);
    }

    /* ===================== 内部工具 ===================== */

    private void snapshotBaseline(LongTermCarePlan plan, Elder elder, LocalDateTime now) {
        plan.setBaselineNightHelps((int) eventRepo.findNightHelpsSince(elder.getId(), now.minusDays(30)).size());
        List<FamilyCommunication> comms = commRepo.findByElderSince(elder.getId(), now.minusDays(30));
        plan.setBaselineFamilyAnswerRate(comms.isEmpty() ? 0
                : (int) Math.round(100.0 * comms.stream().filter(FamilyCommunication::isReached).count() / comms.size()));
        plan.setBaselineMealCheckins((int) mealRepo.countByElderIdAndCheckedInTrueAndCheckedAtBetween(
                elder.getId(), now.minusDays(30), now));
        plan.setBaselineMedReminders((int) medRepo.countByElderIdAndStatusAndRemindAtBetween(
                elder.getId(), "CONFIRMED", now.minusDays(30), now));
    }

    private LongTermCarePlan mustPlan(Long id) {
        return planRepo.findById(id).orElseThrow(() -> new ApiException(404, "长期照护方案不存在: " + id));
    }

    private void addLog(LongTermCarePlan plan, String type, String content, String operator) {
        PlanLog l = new PlanLog();
        l.setPlan(plan);
        l.setType(type);
        l.setContent(content);
        l.setOperatorName(operator);
        logRepo.save(l);
    }

    private void broadcast(String name, Long planId) {
        Map<String, Object> p = new HashMap<>();
        p.put("planId", planId);
        p.put("at", LocalDateTime.now().toString());
        sse.broadcast(name, p);
    }

    private String generateNo(String prefix) {
        String datePart = LocalDate.now().toString().replace("-", "").substring(2);
        long count = planRepo.count() + conferenceRepo.count() + 1;
        return String.format("%s%s%03d", prefix, datePart, count);
    }

    private String appendNote(String old, String line) {
        if (old == null || old.isBlank()) {
            return line;
        }
        return old + "\n" + line;
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private String nz(String s, String def) {
        return s == null || s.isBlank() ? def : s;
    }

    public static String planServiceLabel(String s) {
        if (s == null) return "";
        return switch (s) {
            case "REGULAR_VISIT" -> "定期探访";
            case "PSYCH_COMPANION" -> "心理陪伴";
            case "MEAL_SERVICE" -> "助餐";
            case "FAMILY_DOCTOR" -> "家庭医生随访";
            default -> s;
        };
    }

    public static String itemStatusLabel(String s) {
        return switch (s) {
            case "ACTIVE" -> "执行中";
            case "PAUSED" -> "暂停";
            case "DONE" -> "已结束";
            default -> s;
        };
    }

    public static String medStatusLabel(String s) {
        return switch (s) {
            case "PENDING" -> "待确认";
            case "CONFIRMED" -> "已确认服药";
            case "MISSED" -> "漏服";
            default -> s;
        };
    }
}
