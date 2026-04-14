package com.gdut.oanotification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdut.oanotification.common.exception.BizException;
import com.gdut.oanotification.dto.request.BatchSubscriptionItemRequest;
import com.gdut.oanotification.dto.request.BatchSubscriptionsRequest;
import com.gdut.oanotification.entity.Subscription;
import com.gdut.oanotification.entity.User;
import com.gdut.oanotification.mapper.SubscriptionMapper;
import com.gdut.oanotification.mapper.UserMapper;
import com.gdut.oanotification.service.SubscriptionService;
import com.gdut.oanotification.vo.DepartmentSubscriptionItem;
import com.gdut.oanotification.vo.DepartmentSubscriptionsResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;
    private final UserMapper userMapper;

    @Override
    public DepartmentSubscriptionsResponse getDepartmentSubscriptions(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            throw BizException.badRequest("missing userEmail");
        }
        User user = ensureUser(userEmail.trim());
        return buildResponse(user.getEmail(), listActiveSubscriptions(user.getId()));
    }

    @Override
    @Transactional
    public DepartmentSubscriptionItem subscribeDepartment(String userEmail, String department) {
        if (!StringUtils.hasText(userEmail)) {
            throw BizException.badRequest("missing userEmail");
        }
        if (!StringUtils.hasText(department)) {
            throw BizException.badRequest("missing department");
        }
        User user = ensureUser(userEmail.trim());
        Subscription subscription = loadSubscription(user.getId(), department.trim());
        if (subscription == null) {
            subscription = new Subscription();
            subscription.setUserId(user.getId());
            subscription.setTargetType("department");
            subscription.setTargetValue(department.trim());
            subscription.setStatus(1);
            subscriptionMapper.insert(subscription);
        } else {
            subscription.setStatus(1);
            subscription.setUpdatedAt(LocalDateTime.now());
            subscriptionMapper.updateById(subscription);
        }
        return DepartmentSubscriptionItem.builder()
            .department(department.trim())
            .status(1)
            .build();
    }

    @Override
    @Transactional
    public DepartmentSubscriptionsResponse saveBatchSubscriptions(BatchSubscriptionsRequest request) {
        if (request.getSubscriptions() == null) {
            throw BizException.badRequest("subscriptions must be a list");
        }
        User user = ensureUser(request.getUserEmail().trim());
        for (BatchSubscriptionItemRequest item : request.getSubscriptions()) {
            if (item == null || !StringUtils.hasText(item.getDepartment())) {
                continue;
            }
            String department = item.getDepartment().trim();
            boolean subscribed = Boolean.TRUE.equals(item.getSubscribed());
            Subscription existing = loadSubscription(user.getId(), department);
            if (existing == null) {
                if (subscribed) {
                    Subscription subscription = new Subscription();
                    subscription.setUserId(user.getId());
                    subscription.setTargetType("department");
                    subscription.setTargetValue(department);
                    subscription.setStatus(1);
                    subscriptionMapper.insert(subscription);
                }
                continue;
            }
            existing.setStatus(subscribed ? 1 : 0);
            existing.setUpdatedAt(LocalDateTime.now());
            subscriptionMapper.updateById(existing);
        }
        return buildResponse(user.getEmail(), listActiveSubscriptions(user.getId()));
    }

    private User ensureUser(String email) {
        User existing = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getEmail, email).last("LIMIT 1")
        );
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
        return userMapper.selectById(user.getId());
    }

    private Subscription loadSubscription(Integer userId, String department) {
        return subscriptionMapper.selectOne(
            new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getTargetType, "department")
                .eq(Subscription::getTargetValue, department)
                .last("LIMIT 1")
        );
    }

    private List<Subscription> listActiveSubscriptions(Integer userId) {
        return subscriptionMapper.selectList(
            new LambdaQueryWrapper<Subscription>()
                .eq(Subscription::getUserId, userId)
                .eq(Subscription::getTargetType, "department")
                .eq(Subscription::getStatus, 1)
                .orderByAsc(Subscription::getTargetValue)
        );
    }

    private DepartmentSubscriptionsResponse buildResponse(String userEmail, List<Subscription> subscriptions) {
        List<DepartmentSubscriptionItem> items = new ArrayList<>();
        List<String> departments = new ArrayList<>();
        for (Subscription row : subscriptions) {
            String department = row.getTargetValue() == null ? "" : row.getTargetValue();
            items.add(DepartmentSubscriptionItem.builder().department(department).status(row.getStatus()).build());
            if (!department.isBlank()) {
                departments.add(department);
            }
        }
        return DepartmentSubscriptionsResponse.builder()
            .userEmail(userEmail)
            .items(items)
            .departments(departments)
            .build();
    }

    private String resolveUsername(String email) {
        int index = email.indexOf('@');
        return index > 0 ? email.substring(0, index) : email;
    }
}
