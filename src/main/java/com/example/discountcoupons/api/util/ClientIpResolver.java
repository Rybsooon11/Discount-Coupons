package com.example.discountcoupons.api.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ClientIpResolver {

    private static final Pattern IP_CHARS = Pattern.compile("^[0-9a-fA-F:.]{2,45}$");

    private static final String[] FORWARDED_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "True-Client-IP"
    };

    private final boolean trustForwardedHeaders;

    public ClientIpResolver(
            @Value("${coupons.api.trust-forwarded-headers:false}") boolean trustForwardedHeaders) {
        this.trustForwardedHeaders = trustForwardedHeaders;
    }

    public String resolve(HttpServletRequest request) {
        if (trustForwardedHeaders) {
            for (String header : FORWARDED_HEADERS) {
                String value = request.getHeader(header);
                if (StringUtils.hasText(value)) {
                    int comma = value.indexOf(',');
                    String candidate = (comma >= 0 ? value.substring(0, comma) : value).trim();
                    if (isWellFormedIp(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        String remote = request.getRemoteAddr();
        return isWellFormedIp(remote) ? remote : "0.0.0.0";
    }

    private boolean isWellFormedIp(String value) {
        return value != null && IP_CHARS.matcher(value).matches();
    }
}
