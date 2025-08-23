package com.yadong.yuchuang.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token 计数工具类
 * 提供更精确的Token计算功能
 *
 * @author 超人不会飞
 */
public class TokenCountUtil {

    /**
     * 估算文本的Token数量
     * 这是一个相对准确的估算方法，基于常见的Tokenization规则
     *
     * @param text 要估算的文本
     * @return Token数量
     */
    public static int estimateTokenCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // 基础估算：每4个字符约等于1个Token
        int basicCount = (text.length() + 3) / 4;

        // 考虑中文、日文、韩文等字符（CJK字符）
        // CJK字符通常每个字符算作1个Token
        int cjkCount = countCjkCharacters(text);
        
        // 考虑空格、标点符号等
        int whitespaceCount = countWhitespace(text);
        int punctuationCount = countPunctuation(text);
        
        // 基于经验的调整算法
        // 这是一个粗略的调整，实际Token数量可能会有所不同
        int adjustedCount = (int) (basicCount * 0.9 + 
                                 cjkCount * 0.2 + 
                                 (whitespaceCount + punctuationCount) * 0.1);
        
        // 确保调整后的计数不小于基础计数的一半，也不大于基础计数的两倍
        adjustedCount = Math.max(basicCount / 2, Math.min(adjustedCount, basicCount * 2));
        
        return Math.max(1, adjustedCount);
    }

    /**
     * 计算文本中的CJK（中文、日文、韩文）字符数量
     */
    private static int countCjkCharacters(String text) {
        Pattern cjkPattern = Pattern.compile("[\\u4e00-\\u9fa5\\u3040-\\u309f\\u30a0-\\u30ff\\uac00-\\ud7af]");
        Matcher matcher = cjkPattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 计算文本中的空白字符数量
     */
    private static int countWhitespace(String text) {
        Pattern whitespacePattern = Pattern.compile("\\s+");
        Matcher matcher = whitespacePattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 计算文本中的标点符号数量
     */
    private static int countPunctuation(String text) {
        Pattern punctuationPattern = Pattern.compile("[\\p{P}\\p{S}]");
        Matcher matcher = punctuationPattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 获取文本的Token使用摘要信息
     *
     * @param text 文本内容
     * @return 摘要信息
     */
    public static String getTokenUsageSummary(String text) {
        if (text == null) {
            return "Empty text (0 tokens)";
        }
        int tokenCount = estimateTokenCount(text);
        int charCount = text.length();
        int lineCount = StringUtils.countMatches(text, "\n") + 1;
        
        return String.format("Chars: %d, Lines: %d, Estimated tokens: %d", charCount, lineCount, tokenCount);
    }

}