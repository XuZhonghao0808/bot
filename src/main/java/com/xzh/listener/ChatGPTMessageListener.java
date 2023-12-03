package com.xzh.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xzh.config.ChatGPTConfig;
import com.xzh.entity.Text;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Filters;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simboot.filter.MultiFilterMatchType;
import love.forte.simbot.ID;
import love.forte.simbot.event.FriendAddRequestEvent;
import love.forte.simbot.event.FriendMessageEvent;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.message.At;
import okhttp3.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;

@Component
public class ChatGPTMessageListener {
    private static final Logger log = LoggerFactory.getLogger(ChatGPTMessageListener.class);


    /**
     * 监听AT消息
     */
    @Listener
    @Filters(value = {
            @Filter(value = "gpt", matchType = MatchType.TEXT_STARTS_WITH),
            @Filter(value = "问题", matchType = MatchType.TEXT_STARTS_WITH)
    }, multiMatchType = MultiFilterMatchType.ANY)
    @ContentTrim
    public synchronized void onMessage(GroupMessageEvent groupMessageEvent) {
        String msg = groupMessageEvent.getMessageContent().getPlainText();
        String inputName = msg.replaceAll("gpt", "").replaceAll("问题", "");
        //把空格自动转换为逗号
        msg = msg.trim().replaceAll(" ", ",");
        log.info(groupMessageEvent.getGroup().getId() + "，chatgpt提问：" + msg);
        //自动回复
        String reply;
        try{
            reply = ChatGPTConfig.completions(inputName);
            if (reply == null) {
                reply = "宝，回复失败!重新试试把!";
            }
        } catch (IOException e) {
            reply = "宝，回复失败!重新试试把!";
        }
        //异步回复消息
        groupMessageEvent.replyAsync(reply);
    }

}
