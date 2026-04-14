package com.gdut.oanotification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gdut.oanotification.common.exception.BizException;
import com.gdut.oanotification.config.OaProperties;
import com.gdut.oanotification.dto.request.MiniappSendTestRequest;
import com.gdut.oanotification.dto.request.MiniappSessionRequest;
import com.gdut.oanotification.entity.Notification;
import com.gdut.oanotification.entity.User;
import com.gdut.oanotification.integration.wechat.WechatTokenManager;
import com.gdut.oanotification.mapper.NotificationMapper;
import com.gdut.oanotification.mapper.UserMapper;
import com.gdut.oanotification.service.MiniappService;
import com.gdut.oanotification.vo.MiniappSendTestResponse;
import com.gdut.oanotification.util.TimeFormatUtils;
import com.gdut.oanotification.vo.MiniappSubscribeStatusResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class MiniappServiceImpl implements MiniappService {

    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final OaProperties oaProperties;
    private final RestTemplate restTemplate;
    private final WechatTokenManager wechatTokenManager;

    @Override
    @Transactional
    public MiniappSubscribeStatusResponse getSubscribeStatus(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            throw BizException.badRequest("missing userEmail");
        }
        User user = loadByEmail(userEmail.trim());
        return MiniappSubscribeStatusResponse.builder()
            .userEmail(userEmail.trim())
            .miniappEnabled(oaProperties.getMiniapp().isEnabled())
            .serverConfigured(wechatTokenManager.miniappConfigured())
            .templateId(oaProperties.getMiniapp().getSubscribeTemplateId())
            .subscribePage(oaProperties.getMiniapp().getSubscribePage())
            .hasBoundUser(user != null)
            .hasWechatOpenid(user != null && StringUtils.hasText(user.getWechatOpenid()))
            .wechatOpenidMasked(maskOpenid(user == null ? null : user.getWechatOpenid()))
            .build();
    }

    @Override
    @Transactional
    public MiniappSubscribeStatusResponse bindSession(MiniappSessionRequest request) {
        if (!wechatTokenManager.miniappConfigured()) {
            throw new BizException(500, "miniapp notifier not configured");
        }
        User user = ensureUser(request.getUserEmail().trim());
        String url = UriComponentsBuilder.fromHttpUrl(oaProperties.getMiniapp().getCode2sessionUrl())
            .queryParam("appid", oaProperties.getMiniapp().getAppId())
            .queryParam("secret", oaProperties.getMiniapp().getSecret())
            .queryParam("js_code", request.getCode().trim())
            .queryParam("grant_type", "authorization_code")
            .toUriString();
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        String openid = response == null ? "" : String.valueOf(response.getOrDefault("openid", ""));
        if (!StringUtils.hasText(openid)) {
            throw new BizException(500, "code2Session failed");
        }
        user.setWechatOpenid(openid);
        userMapper.updateById(user);
        return MiniappSubscribeStatusResponse.builder()
            .userEmail(user.getEmail())
            .miniappEnabled(oaProperties.getMiniapp().isEnabled())
            .serverConfigured(wechatTokenManager.miniappConfigured())
            .templateId(oaProperties.getMiniapp().getSubscribeTemplateId())
            .subscribePage(oaProperties.getMiniapp().getSubscribePage())
            .hasBoundUser(true)
            .hasWechatOpenid(true)
            .wechatOpenidMasked(maskOpenid(openid))
            .sessionBound(true)
            .build();
    }

    @Override
    public Map<String, Object> sendSubscribeMessage(String openid, Map<String, Object> notification, String page) {
        if (!StringUtils.hasText(openid)) {
            throw new BizException(500, "user wechat_openid not bound");
        }
        String accessToken = wechatTokenManager.getAccessToken();
        String url = UriComponentsBuilder.fromHttpUrl(oaProperties.getMiniapp().getSubscribeSendUrl())
            .queryParam("access_token", accessToken)
            .toUriString();
        Map<String, Object> payload = new HashMap<>();
        payload.put("touser", openid);
        payload.put("template_id", oaProperties.getMiniapp().getSubscribeTemplateId());
        payload.put("page", StringUtils.hasText(page) ? page : oaProperties.getMiniapp().getSubscribePage());
        payload.put("miniprogram_state", oaProperties.getMiniapp().getMiniprogramState());
        payload.put("lang", oaProperties.getMiniapp().getLang());
        payload.put("data", buildTemplateData(notification));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(payload, headers), Map.class);
        int errCode = Integer.parseInt(String.valueOf(response == null ? 0 : response.getOrDefault("errcode", 0)));
        if (errCode != 0) {
            String errMsg = response == null ? "" : String.valueOf(response.getOrDefault("errmsg", ""));
            throw new BizException(500, "send subscribe message failed: errcode=" + errCode + " errmsg=" + errMsg);
        }
        return response;
    }

    @Override
    public MiniappSendTestResponse sendTest(MiniappSendTestRequest request) {
        String openid = StringUtils.hasText(request.getOpenid()) ? request.getOpenid().trim() : "";
        String userEmail = StringUtils.hasText(request.getUserEmail()) ? request.getUserEmail().trim() : "";
        if (!StringUtils.hasText(openid)) {
            if (!StringUtils.hasText(userEmail)) {
                throw BizException.badRequest("missing openid or userEmail");
            }
            User user = loadByEmail(userEmail);
            if (user == null) {
                throw BizException.notFound("user not found");
            }
            if (!StringUtils.hasText(user.getWechatOpenid())) {
                throw BizException.badRequest("user wechat_openid not bound");
            }
            openid = user.getWechatOpenid();
        }

        Map<String, Object> notification = buildTestNotification(request);
        Map<String, Object> response = sendSubscribeMessage(
            openid,
            notification,
            StringUtils.hasText(request.getPage()) ? request.getPage().trim() : "pages/detail/detail?newsId=" + notification.get("newsId")
        );

        return MiniappSendTestResponse.builder()
            .userEmail(userEmail)
            .newsId(String.valueOf(notification.getOrDefault("newsId", "")))
            .openidMasked(maskOpenid(openid))
            .wechatResponse(response)
            .build();
    }

    private Map<String, Object> buildTemplateData(Map<String, Object> notification) {
        String title = safe(notification.get("title"), 20, "Campus Notification");
        String department = safe(notification.get("publishDepartment"), 20, safe(notification.get("category"), 20, "OA"));
        String publishTime = notification.get("publishTime") == null
            ? TimeFormatUtils.formatMinute(LocalDateTime.now())
            : String.valueOf(notification.get("publishTime"));
        String summary = safe(notification.get("contentText"), 20, title);
        Map<String, Object> data = new HashMap<>();
        data.put("thing31", Map.of("value", department));
        data.put("thing30", Map.of("value", title));
        data.put("thing2", Map.of("value", summary));
        data.put("time3", Map.of("value", publishTime));
        return data;
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
        return userMapper.selectById(user.getId());
    }

    private User loadByEmail(String email) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email).last("LIMIT 1"));
    }

    private Map<String, Object> buildTestNotification(MiniappSendTestRequest request) {
        if (StringUtils.hasText(request.getNewsId())) {
            Notification notification = notificationMapper.selectOne(
                new LambdaQueryWrapper<Notification>()
                    .eq(Notification::getNewsId, request.getNewsId().trim())
                    .last("LIMIT 1")
            );
            if (notification == null) {
                throw BizException.notFound("notification not found");
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("newsId", notification.getNewsId());
            payload.put("title", notification.getTitle());
            payload.put("category", notification.getCategory());
            payload.put("publishDepartment", notification.getPublishDepartment());
            payload.put("publishTime", TimeFormatUtils.formatMinute(notification.getPublishTime()));
            payload.put("detailUrl", notification.getDetailUrl());
            payload.put("contentText", notification.getContentText());
            return payload;
        }

        String title = StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : "测试校园通知";
        String department = StringUtils.hasText(request.getDepartment()) ? request.getDepartment().trim() : "测试部门";
        String category = StringUtils.hasText(request.getCategory()) ? request.getCategory().trim() : "测试分类";
        String contentText = StringUtils.hasText(request.getContentText()) ? request.getContentText().trim() : "这是一条用于联调的小程序测试消息。";
        String newsId = "TEST-" + System.currentTimeMillis();
        Map<String, Object> payload = new HashMap<>();
        payload.put("newsId", newsId);
        payload.put("title", title);
        payload.put("category", category);
        payload.put("publishDepartment", department);
        payload.put("publishTime", TimeFormatUtils.formatSecond(LocalDateTime.now()));
        payload.put("detailUrl", StringUtils.hasText(request.getDetailUrl()) ? request.getDetailUrl().trim() : "");
        payload.put("contentText", contentText);
        return payload;
    }

    private String resolveUsername(String email) {
        int index = email.indexOf('@');
        return index > 0 ? email.substring(0, index) : email;
    }

    private String maskOpenid(String openid) {
        if (!StringUtils.hasText(openid)) {
            return "";
        }
        String clean = openid.trim();
        if (clean.length() <= 10) {
            return clean;
        }
        return clean.substring(0, 4) + "***" + clean.substring(clean.length() - 4);
    }

    private String safe(Object value, int limit, String defaultValue) {
        String text = value == null ? "" : String.valueOf(value).replace("\r", " ").replace("\n", " ").trim();
        if (!StringUtils.hasText(text)) {
            return defaultValue;
        }
        return text.length() > limit ? text.substring(0, limit) : text;
    }
}
