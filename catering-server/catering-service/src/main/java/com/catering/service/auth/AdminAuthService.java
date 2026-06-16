package com.catering.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.exception.BusinessException;
import com.catering.dao.mapper.SysAdminRoleMapper;
import com.catering.dao.mapper.SysAdminUserMapper;
import com.catering.dao.mapper.SysAdminUserRoleMapper;
import com.catering.model.entity.SysAdminRole;
import com.catering.model.entity.SysAdminUser;
import com.catering.model.entity.SysAdminUserRole;
import com.catering.service.auth.dto.AdminLoginRequest;
import com.catering.service.auth.dto.AdminLoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final SysAdminUserMapper adminUserMapper;
    private final SysAdminUserRoleMapper adminUserRoleMapper;
    private final SysAdminRoleMapper adminRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenIssuer tokenIssuer;

    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request) {
        SysAdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<SysAdminUser>()
                .eq(SysAdminUser::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (adminUser == null || !Objects.equals(adminUser.getEnabled(), 1)) {
            throw new BusinessException(401, "账号或密码错误");
        }
        if (!passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
            throw new BusinessException(401, "账号或密码错误");
        }

        List<String> roles = loadRoleKeys(adminUser.getId());
        if (roles.isEmpty()) {
            roles = List.of("ROLE_OPERATOR");
        }

        adminUser.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateById(adminUser);

        return AdminLoginResponse.builder()
                .token(tokenIssuer.issueAdminToken(adminUser.getId(), roles))
                .expiresIn(tokenIssuer.tokenExpireSeconds())
                .userId(adminUser.getId())
                .username(adminUser.getUsername())
                .displayName(adminUser.getDisplayName())
                .roles(roles)
                .build();
    }

    private List<String> loadRoleKeys(Long adminUserId) {
        List<SysAdminUserRole> bindings = adminUserRoleMapper.selectList(new LambdaQueryWrapper<SysAdminUserRole>()
                .eq(SysAdminUserRole::getAdminUserId, adminUserId));
        if (bindings.isEmpty()) {
            return List.of();
        }
        List<Long> roleIds = bindings.stream().map(SysAdminUserRole::getRoleId).toList();
        List<SysAdminRole> roles = adminRoleMapper.selectBatchIds(roleIds);
        List<String> roleKeys = new ArrayList<>();
        for (SysAdminRole role : roles) {
            roleKeys.add(role.getRoleKey());
        }
        return roleKeys;
    }
}
