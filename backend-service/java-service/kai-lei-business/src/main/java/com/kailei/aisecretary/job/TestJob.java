package com.kailei.aisecretary.job;

import cn.hutool.extra.mail.MailUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Random;

/**
 * 1、创建牛马类
 * JOB是执行器 = 牛马 = 被调度的对象
 */
@Slf4j
@Component
public class TestJob {

    /**
     * 2、给牛马起个名
     */
    @XxlJob("sjyTest")
    public void test(){
        log.info("TestJob 我被调度了,{}",new Date());
    }

    /**
     * 给女神5:20的时候发消息
     */
    @XxlJob("sjyMsg")
    public void msg(){
        log.info("在调度我");
        try {
            String msg = MailUtil.send("3309230166@qq.com", "To小甜心", getRandomSweetWord(), false);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        log.info("结束");
    }

    // 土味情话库
    private static final String[] SWEET_WORDS = {
            "你知道我想喝什么吗？我想呵护你。",
            "你猜我什么星座？为你量身定做。",
            "最近有谣言说我喜欢你，我要澄清一下，那不是谣言。",
            "我觉得你特别像一款游戏，我的世界。",
            "你是不是作弊了，不然在我心里怎么永远满分？",
            "我最近有点忙，忙着喜欢你。",
            "你知道我最喜欢吃什么水果吗？是你这个开心果。",
            "别让我看见你，不然见你一次喜欢你一次。",
            "我想买一块地，你的死心塌地。",
            "你知道我的缺点是什么吗？是缺点你。"
    };

    /**
     * 随机返回一条土味情话
     * @return 随机情话
     */
    public static String getRandomSweetWord() {
        Random random = new Random();
        // 随机下标
        int index = random.nextInt(SWEET_WORDS.length);
        return SWEET_WORDS[index];
    }


}
