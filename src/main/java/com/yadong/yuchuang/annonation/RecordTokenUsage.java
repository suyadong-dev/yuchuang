package com.yadong.yuchuang.annonation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 记录Token使用情况的注解
 * 用于标记需要记录AI调用Token消耗的方法
 *
 * @author 超人不会飞
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordTokenUsage {
    
    /**
     * 调用类型描述
     * 默认为空，将使用方法名作为调用类型
     */
    String type() default "";
    
    /**
     * 是否需要精确计算Token数量
     * 如果为true，将使用更精确的Token计数算法
     * 默认为true
     */
    boolean preciseCount() default true;
    
    /**
     * 是否记录输入内容
     * 默认为true
     */
    boolean recordInput() default true;
    
    /**
     * 是否记录输出内容
     * 默认为true
     */
    boolean recordOutput() default true;
    
    /**
     * 内容最大长度
     * 超过此长度的内容将被截断
     * 默认为1000字符
     */
    int maxContentLength() default 1000;

}