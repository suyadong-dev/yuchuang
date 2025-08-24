package com.yadong.yuchuang.monitor;

/**
 * 提供对 MonitorContext 的操作
 */
public class MonitorContextHolder {
    /**
     * ThreadLocal 对象作为key，MonitorContext 对象作为value，设置为静态变量，避免重复创建
     */
    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置 MonitorContext
     *
     * @param context MonitorContext
     */
    public static void set(MonitorContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取 MonitorContext
     *
     * @return MonitorContext
     */
    public static MonitorContext get() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清空 MonitorContext
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
