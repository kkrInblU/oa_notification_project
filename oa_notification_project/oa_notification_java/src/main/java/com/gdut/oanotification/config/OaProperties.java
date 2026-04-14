package com.gdut.oanotification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "oa")
public class OaProperties {

    private String baseUrl;
    private Miniapp miniapp = new Miniapp();
    private Delivery delivery = new Delivery();
    private Crawler crawler = new Crawler();

    @Data
    public static class Miniapp {
        private boolean enabled;
        private String appId;
        private String secret;
        private String subscribeTemplateId;
        private String subscribePage;
        private String accessTokenUrl;
        private String code2sessionUrl;
        private String subscribeSendUrl;
        private String miniprogramState;
        private String lang;
    }

    @Data
    public static class Delivery {
        private String defaultFromEmail;
        private String defaultRecipientEmail;
    }

    @Data
    public static class Crawler {
        private String pythonCommand;
        private String scriptPath;
        private String workingDirectory;
    }
}
