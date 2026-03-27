package com.coursebuddy.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * 讯飞星火 WebSocket 鉴权工具类
 * 基于 HMAC-SHA256 生成带鉴权信息的 WebSocket 连接 URL
 */
@Slf4j
@UtilityClass
public class XunFeiAuthUtil {

    private static final String ALGORITHM = "HmacSHA256";

    /**
     * 生成带鉴权参数的 WebSocket URL
     *
     * @param endpoint  WebSocket 端点，例如 wss://spark-api.xf-yun.com/v3.5/chat
     * @param apiKey    讯飞 API Key
     * @param apiSecret 讯飞 API Secret
     * @return 带鉴权参数的完整 WebSocket URL
     */
    public String buildAuthUrl(String endpoint, String apiKey, String apiSecret) {
        try {
            String date = buildRfc1123Date();
            String host = extractHost(endpoint);
            String requestPath = extractPath(endpoint);

            String signingString = "host: " + host + "\n"
                    + "date: " + date + "\n"
                    + "GET " + requestPath + " HTTP/1.1";

            String signature = hmacSHA256Base64(apiSecret, signingString);

            String authorizationOrigin = String.format(
                    "api_key=\"%s\", algorithm=\"hmac-sha256\", headers=\"host date request-line\", signature=\"%s\"",
                    apiKey, signature);

            String authorization = Base64.getEncoder()
                    .encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8));

            return endpoint
                    + "?authorization=" + URLEncoder.encode(authorization, StandardCharsets.UTF_8)
                    + "&date=" + URLEncoder.encode(date, StandardCharsets.UTF_8)
                    + "&host=" + URLEncoder.encode(host, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Failed to build XunFei auth URL", e);
            throw new RuntimeException("Failed to build XunFei authentication URL", e);
        }
    }

    private String buildRfc1123Date() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

    /** Strips the scheme prefix (wss://, ws://, https://, http://) from a URL */
    private String stripScheme(String url) {
        return url.replaceFirst("^(wss?|https?)://", "");
    }

    private String extractHost(String endpoint) {
        // e.g. wss://spark-api.xf-yun.com/v3.5/chat -> spark-api.xf-yun.com
        String withoutScheme = stripScheme(endpoint);
        int slashIdx = withoutScheme.indexOf('/');
        return slashIdx > 0 ? withoutScheme.substring(0, slashIdx) : withoutScheme;
    }

    private String extractPath(String endpoint) {
        // e.g. wss://spark-api.xf-yun.com/v3.5/chat -> /v3.5/chat
        String withoutScheme = stripScheme(endpoint);
        int slashIdx = withoutScheme.indexOf('/');
        return slashIdx >= 0 ? withoutScheme.substring(slashIdx) : "/";
    }

    private String hmacSHA256Base64(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        mac.init(keySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }
}
