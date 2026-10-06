package com.community.eldercare.controller;

import com.community.eldercare.common.R;
import com.community.eldercare.domain.*;
import com.community.eldercare.dto.*;
import com.community.eldercare.service.CarePlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 频繁夜间求助 → 长期照护评估 → 长期照护方案 → 服务执行 → 效果观察 → 个案会商。
 */
@RestController
@RequestMapping("/api/care-plans")
public class CarePlanController {

    private final CarePlanService service;

    public CarePlanController(CarePlanService service) {
        this.service = service;
    }

    /* -------- 评估与方案 -------- */

    /** 发起长期照护评估（同时拟定服务安排，方案为待生效） */
    @PostMapping("/assessments")
    public R<LongTermCarePlan> assess(@Valid @RequestBody CarePlanRequest req) {
        return R.ok(service.initiateAssessment(req));
    }

    /** 方案列表：?elderId=&status= */
    @GetMapping
    public R<List<LongTermCarePlan>> list(@RequestParam(required = false) Long elderId,
                                          @RequestParam(required = false) String status) {
        return R.ok(service.listPlans(elderId, status));
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        return R.ok(service.planDetail(id));
    }

    /** 方案生效，进入观察期 */
    @PostMapping("/{id}/activate")
    public R<LongTermCarePlan> activate(@PathVariable Long id) {
        return R.ok(service.activate(id));
    }

    /** 结束方案 */
    @PostMapping("/{id}/end")
    public R<LongTermCarePlan> end(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return R.ok(service.endPlan(id, reason));
    }

    /** 效果观察：夜间求助/助餐签到/药盒提醒/家属接听率变化 */
    @GetMapping("/{id}/observation")
    public R<Map<String, Object>> observation(@PathVariable Long id) {
        return R.ok(service.observation(id));
    }

    /* -------- 服务安排与执行 -------- */

    @PostMapping("/{id}/items")
    public R<PlanServiceItem> addItem(@PathVariable Long id, @RequestBody PlanItemRequest req) {
        return R.ok(service.addItem(id, req));
    }

    @PutMapping("/items/{itemId}/status")
    public R<PlanServiceItem> itemStatus(@PathVariable Long itemId, @RequestParam String status) {
        return R.ok(service.updateItemStatus(itemId, status));
    }

    /** 登记一次长期服务执行（定期探访/心理陪伴/家庭医生随访） */
    @PostMapping("/{id}/executions")
    public R<PlanServiceExecution> execution(@PathVariable Long id, @RequestBody ServiceExecutionRequest req) {
        return R.ok(service.registerExecution(id, req));
    }

    /* -------- 助餐签到 / 药盒提醒 / 家属接听 -------- */

    @PostMapping("/{id}/meal-checkins")
    public R<MealCheckin> mealCheckin(@PathVariable Long id, @RequestBody MealCheckinRequest req) {
        return R.ok(service.mealCheckin(id, req));
    }

    @PostMapping("/{id}/medication-reminders")
    public R<MedicationReminder> reminder(@PathVariable Long id, @RequestBody MedicationReminderRequest req) {
        return R.ok(service.createReminder(id, req));
    }

    @PutMapping("/medication-reminders/{reminderId}/status")
    public R<MedicationReminder> reminderStatus(@PathVariable Long reminderId,
                                                @RequestParam String status,
                                                @RequestParam(required = false) String confirmSource) {
        return R.ok(service.updateReminder(reminderId, status, confirmSource));
    }

    @PostMapping("/{id}/family-calls")
    public R<FamilyCallRecord> familyCall(@PathVariable Long id, @RequestBody FamilyCallRequest req) {
        return R.ok(service.familyCall(id, req));
    }

    /* -------- 个案会商 -------- */

    @PostMapping("/conferences")
    public R<CaseConference> createConference(@RequestBody CaseConferenceRequest req) {
        return R.ok(service.initiateConference(req));
    }

    @GetMapping("/conferences/{id}")
    public R<CaseConference> conference(@PathVariable Long id) {
        return R.ok(service.getConference(id));
    }

    /** 填写会商结论并写回老人档案 */
    @PostMapping("/conferences/{id}/conclusion")
    public R<CaseConference> conclude(@PathVariable Long id, @RequestBody ConferenceConclusionRequest req) {
        return R.ok(service.concludeConference(id, req));
    }
}
