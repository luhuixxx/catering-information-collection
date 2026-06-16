package com.catering.service.region.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.common.exception.BusinessException;
import com.catering.dao.mapper.SysRegionMapper;
import com.catering.model.entity.SysRegion;
import com.catering.service.region.SysRegionService;
import com.catering.service.region.dto.RegionNodeVO;
import com.catering.service.region.dto.RegionSaveRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysRegionServiceImpl extends ServiceImpl<SysRegionMapper, SysRegion> implements SysRegionService {

    @Override
    public List<RegionNodeVO> listEnabledTree() {
        List<SysRegion> regions = list(new LambdaQueryWrapper<SysRegion>()
                .eq(SysRegion::getEnabled, 1)
                .orderByAsc(SysRegion::getSortNo)
                .orderByAsc(SysRegion::getId));
        return buildTree(regions, 0L);
    }

    @Override
    public List<RegionNodeVO> listAdminTree() {
        List<SysRegion> regions = list(new LambdaQueryWrapper<SysRegion>()
                .orderByAsc(SysRegion::getSortNo)
                .orderByAsc(SysRegion::getId));
        return buildTree(regions, 0L);
    }

    @Override
    public List<RegionNodeVO> listChildren(Long parentId, boolean enabledOnly) {
        LambdaQueryWrapper<SysRegion> wrapper = new LambdaQueryWrapper<SysRegion>()
                .eq(SysRegion::getParentId, parentId)
                .orderByAsc(SysRegion::getSortNo)
                .orderByAsc(SysRegion::getId);
        if (enabledOnly) {
            wrapper.eq(SysRegion::getEnabled, 1);
        }
        return list(wrapper).stream().map(this::toNode).toList();
    }

    @Override
    @Transactional
    public Long createRegion(RegionSaveRequest request) {
        validateParent(request.getParentId(), request.getLevel(), null);
        SysRegion region = new SysRegion();
        region.setParentId(request.getParentId());
        region.setLevel(request.getLevel());
        region.setCode(request.getCode());
        region.setName(request.getName());
        region.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        region.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
        save(region);
        return region.getId();
    }

    @Override
    @Transactional
    public void updateRegion(Long id, RegionSaveRequest request) {
        SysRegion existing = requireRegion(id);
        validateParent(request.getParentId(), request.getLevel(), id);
        existing.setParentId(request.getParentId());
        existing.setLevel(request.getLevel());
        existing.setCode(request.getCode());
        existing.setName(request.getName());
        if (request.getSortNo() != null) {
            existing.setSortNo(request.getSortNo());
        }
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }
        updateById(existing);
    }

    @Override
    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        SysRegion region = requireRegion(id);
        region.setEnabled(enabled ? 1 : 0);
        updateById(region);
    }

    @Override
    @Transactional
    public void deleteRegion(Long id) {
        requireRegion(id);
        long childCount = count(new LambdaQueryWrapper<SysRegion>().eq(SysRegion::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(400, "存在下级地区，无法删除");
        }
        removeById(id);
    }

    private SysRegion requireRegion(Long id) {
        SysRegion region = getById(id);
        if (region == null) {
            throw new BusinessException(404, "地区不存在");
        }
        return region;
    }

    private void validateParent(Long parentId, Integer level, Long selfId) {
        if (parentId == null || parentId == 0L) {
            if (level != 1) {
                throw new BusinessException(400, "顶级地区层级必须为省");
            }
            return;
        }
        SysRegion parent = getById(parentId);
        if (parent == null) {
            throw new BusinessException(400, "父级地区不存在");
        }
        if (selfId != null && selfId.equals(parentId)) {
            throw new BusinessException(400, "不能将自身设为父级");
        }
        if (level != parent.getLevel() + 1) {
            throw new BusinessException(400, "地区层级与父级不匹配");
        }
    }

    private List<RegionNodeVO> buildTree(List<SysRegion> regions, Long rootParentId) {
        Map<Long, List<SysRegion>> grouped = regions.stream()
                .collect(Collectors.groupingBy(region -> region.getParentId() == null ? 0L : region.getParentId()));
        return buildChildren(grouped, rootParentId);
    }

    private List<RegionNodeVO> buildChildren(Map<Long, List<SysRegion>> grouped, Long parentId) {
        List<SysRegion> children = grouped.getOrDefault(parentId, List.of());
        children = children.stream()
                .sorted(Comparator.comparing(SysRegion::getSortNo).thenComparing(SysRegion::getId))
                .toList();
        List<RegionNodeVO> nodes = new ArrayList<>();
        for (SysRegion child : children) {
            RegionNodeVO node = toNode(child);
            node.setChildren(buildChildren(grouped, child.getId()));
            nodes.add(node);
        }
        return nodes;
    }

    private RegionNodeVO toNode(SysRegion region) {
        RegionNodeVO node = new RegionNodeVO();
        BeanUtils.copyProperties(region, node);
        return node;
    }
}
