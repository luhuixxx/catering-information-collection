package com.catering.service.region.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.dao.mapper.SysRegionMapper;
import com.catering.model.entity.SysRegion;
import com.catering.service.region.SysRegionService;
import org.springframework.stereotype.Service;

@Service
public class SysRegionServiceImpl extends ServiceImpl<SysRegionMapper, SysRegion> implements SysRegionService {
}

