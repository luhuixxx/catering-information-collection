package com.catering.api.controller.admin;

import com.catering.common.result.Result;
import com.catering.service.region.SysRegionService;
import com.catering.service.region.dto.RegionNodeVO;
import com.catering.service.region.dto.RegionSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "Admin - Region")
@RestController
@RequestMapping("/api/admin/regions")
@RequiredArgsConstructor
public class AdminRegionController {

    private final SysRegionService sysRegionService;

    @Operation(summary = "地区树（含停用）")
    @GetMapping("/tree")
    public Result<List<RegionNodeVO>> tree() {
        return Result.ok(sysRegionService.listAdminTree());
    }

    @Operation(summary = "下级地区列表")
    @GetMapping("/children")
    public Result<List<RegionNodeVO>> children(@RequestParam Long parentId) {
        return Result.ok(sysRegionService.listChildren(parentId, false));
    }

    @Operation(summary = "新增地区")
    @PostMapping
    public Result<Map<String, Long>> create(@Valid @RequestBody RegionSaveRequest request) {
        Long id = sysRegionService.createRegion(request);
        return Result.ok(Map.of("id", id));
    }

    @Operation(summary = "更新地区")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RegionSaveRequest request) {
        sysRegionService.updateRegion(id, request);
        return Result.ok();
    }

    @Operation(summary = "启用/停用地区")
    @PutMapping("/{id}/enabled")
    public Result<Void> setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        sysRegionService.setEnabled(id, enabled);
        return Result.ok();
    }

    @Operation(summary = "删除地区")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRegionService.deleteRegion(id);
        return Result.ok();
    }
}
