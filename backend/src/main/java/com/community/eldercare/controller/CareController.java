package com.community.eldercare.controller;

import com.community.eldercare.common.R;
import com.community.eldercare.domain.*;
import com.community.eldercare.dto.*;
import com.community.eldercare.service.CareService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 长期照护相关接口：
 * 夜间求助观察、长期照护评估、照护方案与服务签到、效果观察、个案会商、档案留痕。
 */
@RestController
public class CareController {

    private final CareService careService;

    public CareController(CareService careService) {
        this.careService = careService;
    }

    /* ---------- 夜间求助观察 ---------- */

    /** 社区端查看同一老人近一周每次夜间求助的原因、到场结果、孤独焦虑/药物混乱、家属参与情况 */
    @GetMapping("/api/elders/{elderId}/night-review")
    public R<Map<String, Object>> nightReview(@PathVariable Long elderId,
                                              @RequestParam(required = false) Integer days) {
        return R.ok(careService.nightReview(elderId, days));
    }

    /* ---------- 长期照护评估 ---------- */

    @PostMapping("/api/elders/{elderId}/assessments")
    public R<CareAssessment> createAssessment(@PathVariable Long elderId,
                                              @Valid @RequestBody AssessmentRequest req) {
        return R.ok(careService.createAssessment(elderId, req));
    }

    @GetMapping("/api/elders/{elderId}/assessments")
    public R<List<CareAssessment>> assessments(@PathVariable Long elderId) {
        return R.ok(careService.assessments(elderId));
    }

    @GetMapping("/api/assessments/{id}")
    public R<Map<String, Object>> assessmentDetail(@PathVariable Long id) {
        return R.ok(careService.assessmentDetail(id));
    }

    /* ---------- 长期照护方案 ---------- */

    @GetMapping("/api/care-plans")
    public R<List<CarePlan>> plans(@RequestParam(required = false) String status,
                                   @RequestParam(required = false) Long elderId) {
        return R.ok(careService.planList(status, elderId));
    }

    @PostMapping("/api/care-plans")
    public R<CarePlan> createPlan(@Valid @RequestBody PlanRequest req) {
        return R.ok(careService.createPlan(req));
    }

    @GetMapping("/api/care-plans/{id}")
    public R<Map<String, Object>> planDetail(@PathVariable Long id) {
        return R.ok(careService.planDetail(id));
    }

    /** 方案生效（生效时记录基线指标） */
    @PostMapping("/api/care-plans/{id}/activate")
    public R<CarePlan> activate(@PathVariable Long id) {
        return R.ok(careService.activatePlan(id));
    }

    /** 观察期结束 */
    @PostMapping("/api/care-plans/{id}/complete")
    public R<CarePlan> complete(@PathVariable Long id) {
        return R.ok(careService.completePlan(id));
    }

    /** 效果观察：夜间求助、助餐签到、药盒提醒、家属接听率变化 */
    @GetMapping("/api/care-plans/{id}/effect")
    public R<Map<String, Object>> effect(@PathVariable Long id) {
        return R.ok(careService.planEffect(id));
    }

    /** 社工保存一次效果复评结论 */
    @PostMapping("/api/care-plans/{id}/review")
    public R<CarePlan> review(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return R.ok(careService.saveReview(id, body.get("result"), body.get("note")));
    }

    /* ---------- 服务执行 / 签到登记 ---------- */

    @PostMapping("/api/care-plans/{id}/activities")
    public R<CareActivity> addActivity(@PathVariable Long id, @Valid @RequestBody ActivityRequest req) {
        return R.ok(careService.addActivity(id, req));
    }

    /* ---------- 个案会商 ---------- */

    @PostMapping("/api/conferences")
    public R<CaseConference> createConference(@Valid @RequestBody ConferenceRequest req) {
        return R.ok(careService.createConference(req));
    }

    @PutMapping("/api/conferences/{id}")
    public R<CaseConference> updateConference(@PathVariable Long id,
                                              @Valid @RequestBody ConferenceRequest req) {
        return R.ok(careService.updateConference(id, req));
    }

    @GetMapping("/api/conferences/{id}")
    public R<Map<String, Object>> conferenceDetail(@PathVariable Long id) {
        return R.ok(careService.conferenceDetail(id));
    }

    @GetMapping("/api/conferences")
    public R<List<CaseConference>> conferences(@RequestParam(required = false) Long elderId) {
        return R.ok(careService.conferences(elderId));
    }

    /* ---------- 老人档案留痕 ---------- */

    @GetMapping("/api/elders/{elderId}/archive-notes")
    public R<List<ElderArchiveNote>> archiveNotes(@PathVariable Long elderId) {
        return R.ok(careService.archiveNotes(elderId));
    }
}
