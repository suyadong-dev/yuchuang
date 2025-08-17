package com.yadong.yuchuang.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 相关工具类
 */
public class IpUtil {
    /**
     * 私有构造函数，防止实例化
     */
    private IpUtil() {
    }

    /**
     * 获取客户端IP
     *
     * @param request 客户端请求
     * @return 客户端IP
     */
    public static String getClientIP(HttpServletRequest request) {
        // 按优先级顺序检查各种可能的IP头信息
        String ip = getHeader(request, "X-Forwarded-For");

        if (isValidIp(ip)) {
            // X-Forwarded-For可能包含多个IP，取第一个（最原始的客户端IP）
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            return ip;
        }

        ip = getHeader(request, "Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = getHeader(request, "WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = getHeader(request, "HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = getHeader(request, "HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }

        // 如果以上都没有获取到有效IP，则使用远程地址
        return request.getRemoteAddr();
    }

    /**
     * 获取请求头中的IP
     *
     * @param request    客户端请求
     * @param headerName 请求头名称
     * @return 请求头值
     */
    private static String getHeader(HttpServletRequest request, String headerName) {
        return request.getHeader(headerName);
    }

    /**
     * 判断IP地址是否合法
     *
     * @param ip IP地址
     * @return 是否为合法IP
     */
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
