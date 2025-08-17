package com.yadong.yuchuang.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;

/**
 * 缓存工具类
 */
public class CacheUtil {

    private CacheUtil() {
    }

    /**
     * 获取缓存key
     *
     * @param obj 请求对象
     * @return 缓存key
     */
    public static String getCacheKey(Object obj) {
        // 请求对象为空也返回一个默认的缓存key
        if (ObjectUtil.isEmpty(obj)) {
            return DigestUtil.md5Hex("null");
        }
        // 将obj对象转换为字符串
        String jsonStr = JSONUtil.toJsonStr(obj);
        return DigestUtil.md5Hex(jsonStr);
    }
}
