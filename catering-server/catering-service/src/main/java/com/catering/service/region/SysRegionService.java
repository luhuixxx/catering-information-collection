package com.catering.service.region;

import com.baomidou.mybatisplus.extension.service.IService;
import com.catering.model.entity.SysRegion;
import com.catering.service.region.dto.RegionNodeVO;
import com.catering.service.region.dto.RegionSaveRequest;

import java.util.List;

public interface SysRegionService extends IService<SysRegion> {

    List<RegionNodeVO> listEnabledTree();

    List<RegionNodeVO> listAdminTree();

    List<RegionNodeVO> listChildren(Long parentId, boolean enabledOnly);

    Long createRegion(RegionSaveRequest request);

    void updateRegion(Long id, RegionSaveRequest request);

    void setEnabled(Long id, boolean enabled);

    void deleteRegion(Long id);
}
