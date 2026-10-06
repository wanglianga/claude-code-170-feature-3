package com.community.eldercare.controller;

import com.community.eldercare.common.R;
import com.community.eldercare.service.CarePlanService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 老人维度的频繁夜间求助聚合视图。
 */
@RestController
@RequestMapping("/api/elders")
public class NightHelpController {

    private final CarePlanService carePlanService;

    public NightHelpController(CarePlanService carePlanService) {
        this.carePlanService = carePlanService;
    }

    /**
     * 同一老人一段时间内（默认近 30 天，含“一周内多次”）夜间求助聚合：
     * 每次求助原因、到场结果、是否孤独焦虑、是否药物管理混乱、家属参与与接听情况。
     */
    @GetMapping("/{id}/night-help-summary")
    public R<Map<String, Object>> summary(@PathVariable Long id,
                                          @RequestParam(required = false, defaultValue = "30") Integer days) {
        return R.ok(carePlanService.nightHelpSummary(id, days == null ? 30 : days));
    }
}
