package com.drowndreamer.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String msg ="你好，我是drowndreamer";
        String ans = loveApp.doChat(msg, chatId);
        // 第二轮
        msg ="我想找到我的另一半，我希望她温柔善良，貌美体贴";
        ans = loveApp.doChat(msg, chatId);
        Assertions.assertNotNull(ans);
        // 第三轮
        msg ="你记得我是谁吗";
        ans = loveApp.doChat(msg, chatId);
        Assertions.assertNotNull(ans);
    }


    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        //第一轮
        String msg ="你好，我是drowndreamer，我想让另一半（雪之下雪乃）更爱我，但我不知道该怎么做";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(msg, chatId);
        assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String msg ="我已经结婚了，但是婚后关系不太亲密，怎么办？";
        String ans = loveApp.doChatWithRag(msg, chatId);
        assertNotNull(ans);
    }
}
