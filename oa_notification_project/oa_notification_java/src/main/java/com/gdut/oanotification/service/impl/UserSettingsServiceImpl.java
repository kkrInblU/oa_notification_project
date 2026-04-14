package com.gdut.oanotification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdut.oanotification.common.exception.BizException;
import com.gdut.oanotification.dto.request.UpdateUserSettingsRequest;
import com.gdut.oanotification.entity.User;
import com.gdut.oanotification.mapper.UserMapper;
import com.gdut.oanotification.service.UserSettingsService;
import com.gdut.oanotification.util.TimeFormatUtils;
import com.gdut.oanotification.vo.UserSettingsResponse;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {

    private static final Set<Integer> VALID_INTERVALS = Set.of(1, 5, 30, 60);
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserSettingsResponse getUserSettings(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            throw BizException.badRequest("missing userEmail");
        }
        User user = ensureUser(userEmail.trim());
        return toResponse(user, true);
    }

    @Override
    @Transactional
    public UserSettingsResponse updateUserSettings(UpdateUserSettingsRequest request) {
        if (request.getRefreshIntervalMinutes() == null
            && request.getEmailEnabled() == null
            && request.getMiniappEnabled() == null) {
            throw BizException.badRequest("at least one setting is required");
        }
        if (request.getRefreshIntervalMinutes() != null
            && !VALID_INTERVALS.contains(request.getRefreshIntervalMinutes())) {
            throw BizException.badRequest("refresh_interval_minutes must be one of 1, 5, 30, 60");
        }

        User user = ensureUser(request.getUserEmail().trim());
        if (request.getEmailEnabled() != null) {
            user.setEmailNotificationsEnabled(Boolean.TRUE.equals(request.getEmailEnabled()) ? 1 : 0);
        }
        if (request.getMiniappEnabled() != null) {
            user.setMiniappNotificationsEnabled(Boolean.TRUE.equals(request.getMiniappEnabled()) ? 1 : 0);
        }
        if (request.getRefreshIntervalMinutes() != null) {
            user.setNotificationRefreshIntervalMinutes(request.getRefreshIntervalMinutes());
            user.setLastNotificationCheckAt(LocalDateTime.now());
        }
        userMapper.updateById(user);
        return toResponse(loadByEmail(user.getEmail()), null);
    }

    private User ensureUser(String email) {
        User existing = loadByEmail(email);
        if (existing != null) {
            return existing;
        }
        User user = new User();
        user.setUsername(resolveUsername(email));
        user.setEmail(email);
        user.setEmailNotificationsEnabled(1);
        user.setMiniappNotificationsEnabled(1);
        user.setNotificationRefreshIntervalMinutes(60);
        user.setLastNotificationCheckAt(LocalDateTime.now());
        user.setStatus(1);
        userMapper.insert(user);
        return loadByEmail(email);
    }

    private User loadByEmail(String email) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
                .last("LIMIT 1")
        );
    }

    private UserSettingsResponse toResponse(User user, Boolean hasUser) {
        return UserSettingsResponse.builder()
            .userEmail(user.getEmail())
            .emailEnabled(user.getEmailNotificationsEnabled() != null && user.getEmailNotificationsEnabled() == 1)
            .miniappEnabled(user.getMiniappNotificationsEnabled() != null && user.getMiniappNotificationsEnabled() == 1)
            .refreshIntervalMinutes(user.getNotificationRefreshIntervalMinutes() == null ? 60 : user.getNotificationRefreshIntervalMinutes())
            .lastNotificationCheckAt(TimeFormatUtils.formatMinute(user.getLastNotificationCheckAt()))
            .hasUser(hasUser)
            .build();
    }

    private String resolveUsername(String email) {
        int index = email.indexOf('@');
        return index > 0 ? email.substring(0, index) : email;
    }
}
