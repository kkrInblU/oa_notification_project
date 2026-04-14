package com.gdut.oanotification.integration.wechat;

import com.gdut.oanotification.common.exception.BizException;
import com.gdut.oanotification.config.OaProperties;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class WechatTokenManager {

    private final RestTemplate restTemplate;
    private final OaProperties oaProperties;

    private volatile String accessToken;
    private volatile Instant expireAt = Instant.EPOCH;

    public synchronized String getAccessToken() {
        if (StringUtils.hasText(accessToken) && Instant.now().isBefore(expireAt)) {
            return accessToken;
        }
        if (!miniappConfigured()) {
            throw new BizException(500, "miniapp notifier not configured");
        }
        String url = UriComponentsBuilder.fromHttpUrl(oaProperties.getMiniapp().getAccessTokenUrl())
            .queryParam("grant_type", "client_credential")
            .queryParam("appid", oaProperties.getMiniapp().getAppId())
            .queryParam("secret", oaProperties.getMiniapp().getSecret())
            .toUriString();
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class, new LinkedMultiValueMap<>());
        String token = response == null ? "" : String.valueOf(response.getOrDefault("access_token", ""));
        if (!StringUtils.hasText(token)) {
            throw new BizException(500, "failed to get access_token");
        }
        Object expiresIn = response.getOrDefault("expires_in", 7200);
        long ttlSeconds = Long.parseLong(String.valueOf(expiresIn));
        this.accessToken = token;
        this.expireAt = Instant.now().plusSeconds(Math.max(ttlSeconds - 300, 60));
        return this.accessToken;
    }

    public boolean miniappConfigured() {
        return oaProperties.getMiniapp().isEnabled()
            && StringUtils.hasText(oaProperties.getMiniapp().getAppId())
            && StringUtils.hasText(oaProperties.getMiniapp().getSecret())
            && StringUtils.hasText(oaProperties.getMiniapp().getSubscribeTemplateId());
    }
}
