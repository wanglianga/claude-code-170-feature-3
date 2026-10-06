package com.community.eldercare.controller;

import com.community.eldercare.common.R;
import com.community.eldercare.domain.Elder;
import com.community.eldercare.domain.EmergencyContact;
import com.community.eldercare.domain.NeighborResponder;
import com.community.eldercare.repo.ElderArchiveNoteRepository;
import com.community.eldercare.repo.ElderRepository;
import com.community.eldercare.service.ElderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elders")
public class ElderController {

    private final ElderService elderService;
    private final ElderRepository elderRepo;
    private final ElderArchiveNoteRepository archiveNoteRepo;

    public ElderController(ElderService elderService, ElderRepository elderRepo,
                           ElderArchiveNoteRepository archiveNoteRepo) {
        this.elderService = elderService;
        this.elderRepo = elderRepo;
        this.archiveNoteRepo = archiveNoteRepo;
    }

    @GetMapping
    public R<List<Elder>> list(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String riskLevel) {
        if (keyword != null && !keyword.isBlank()) {
            return R.ok(elderRepo.findByNameContainingOrderByIdDesc(keyword));
        }
        if (riskLevel != null && !riskLevel.isBlank()) {
            return R.ok(elderRepo.findByRiskLevelOrderByUpdatedAtDesc(riskLevel));
        }
        return R.ok(elderRepo.findAllByOrderByUpdatedAtDesc());
    }

    @GetMapping("/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Elder e = elderRepo.findById(id).orElseThrow(() ->
                new com.community.eldercare.common.ApiException(404, "老人不存在"));
        return R.ok(Map.of(
                "elder", e,
                "contacts", elderService.contacts(id),
                "neighbors", elderService.neighbors(id),
                "archiveNotes", archiveNoteRepo.findByElderIdOrderByCreatedAtDesc(id)
        ));
    }

    @PostMapping
    public R<Elder> create(@RequestBody Elder elder) {
        elder.setId(null);
        return R.ok(elderService.save(elder));
    }

    @PutMapping("/{id}")
    public R<Elder> update(@PathVariable Long id, @RequestBody Elder elder) {
        if (!elderRepo.existsById(id)) {
            throw new com.community.eldercare.common.ApiException(404, "老人不存在");
        }
        elder.setId(id);
        return R.ok(elderService.save(elder));
    }

    /* -------- 紧急联系人 -------- */

    @GetMapping("/{id}/contacts")
    public R<List<EmergencyContact>> contacts(@PathVariable Long id) {
        return R.ok(elderService.contacts(id));
    }

    @PostMapping("/{id}/contacts")
    public R<EmergencyContact> addContact(@PathVariable Long id, @RequestBody EmergencyContact c) {
        c.setId(null);
        return R.ok(elderService.saveContact(id, c));
    }

    @DeleteMapping("/contacts/{contactId}")
    public R<Void> deleteContact(@PathVariable Long contactId) {
        elderService.deleteContact(contactId);
        return R.ok();
    }

    /* -------- 邻里响应人 -------- */

    @GetMapping("/{id}/neighbors")
    public R<List<NeighborResponder>> neighbors(@PathVariable Long id) {
        return R.ok(elderService.neighbors(id));
    }

    @PostMapping("/{id}/neighbors")
    public R<NeighborResponder> addNeighbor(@PathVariable Long id, @RequestBody NeighborResponder n) {
        n.setId(null);
        return R.ok(elderService.saveNeighbor(id, n));
    }

    @DeleteMapping("/neighbors/{neighborId}")
    public R<Void> deleteNeighbor(@PathVariable Long neighborId) {
        elderService.deleteNeighbor(neighborId);
        return R.ok();
    }
}
