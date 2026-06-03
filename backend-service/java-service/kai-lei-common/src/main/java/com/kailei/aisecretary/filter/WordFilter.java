package com.kailei.aisecretary.filter;

import cn.hutool.dfa.SensitiveUtil;
import cn.hutool.dfa.WordTree;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;


/**
 * 文件文字，敏感词过滤
 */
@Component
public class WordFilter {
    //自定义敏感词
    private static List<String> sensitiveWords = Arrays.asList(
            "暴力", "毒品", "诈骗", "外挂", "李杰"
    );
    //检查方法
    public static boolean violationInspection(String text){
        // 构建自定义 WordTree（DFA 核心容器）
        WordTree wordTree = new WordTree();
        // 加载自定义敏感词
        wordTree.addWords(sensitiveWords);
        //敏感词工具类
        boolean hasSensitive = wordTree.match(text) != null;
        return hasSensitive;
    }
}
