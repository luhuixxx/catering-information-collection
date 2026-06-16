package com.catering.service.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.dao.mapper.SysConfigMapper;
import com.catering.model.entity.SysConfig;
import org.springframework.stereotype.Service;

@Service
public class SysConfigService extends ServiceImpl<SysConfigMapper, SysConfig> {

    public String getValue(String key, String defaultValue) {
        SysConfig config = getOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        return config == null ? defaultValue : config.getConfigValue();
    }

    public int getIntValue(String key, int defaultValue) {
        String raw = getValue(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}
