package com.catering.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.dao.mapper.AppUserMapper;
import com.catering.model.entity.AppUser;
import com.catering.service.user.AppUserService;
import org.springframework.stereotype.Service;

@Service
public class AppUserServiceImpl extends ServiceImpl<AppUserMapper, AppUser> implements AppUserService {
}

