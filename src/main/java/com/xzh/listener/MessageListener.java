package com.xzh.listener;

import com.xzh.entity.Hero;
import com.xzh.service.HeroService;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simbot.ID;
import love.forte.simbot.Identifies;
import love.forte.simbot.event.FriendAddRequestEvent;
import love.forte.simbot.event.FriendMessageEvent;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.MessagesBuilder;
import love.forte.simbot.message.ReceivedMessageContent;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class MessageListener {
    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    /**
     * 监听好友添加请求
     */
    @Listener
    public void onFriendAddRequest(FriendAddRequestEvent friendAddRequestEvent) {
        String message = friendAddRequestEvent.getMessage();
        ID id = friendAddRequestEvent.getFriend().getId();
        log.info(id + ",添加我为好友");
        //触发关键词即自动同意，否则拒绝
        if (Objects.equals(message,"鸡气人")) {
            log.info("同意添加" + id + "为好友");
            friendAddRequestEvent.acceptAsync();
        } else {
            log.info("拒绝添加" + id + "为好友");
            friendAddRequestEvent.rejectAsync();
        }
    }

    /**
     * 监听消息
     */
    @Listener
    public synchronized void onMessage(FriendMessageEvent friendMessage) {
        String msg = friendMessage.getMessageContent().getPlainText();
        //把空格自动转换为逗号
        msg = msg.trim().replaceAll(" ", ",");
        log.info(friendMessage.getFriend().getId() + "提问：" + msg);
        //AI自动回复
        String reply = AiOne(msg);
        if (reply == null) {
            reply = "宝，回复失败!重新试试把!";
        }
        //异步回复消息
        friendMessage.replyAsync(reply);
    }

    public static String AiOne(String sendMsg) {
        try {
            HttpGet httpGet = new HttpGet("http://api.qingyunke.com/api.php?key=free&appid=0&msg=" + sendMsg);
            String user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.42";
            httpGet.addHeader("user-agent", user_agent);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(httpGet);
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            body = body.substring(body.indexOf("content") + 10, body.length() - 2);
            log.info("AiOne={}", body);
            return body;
        } catch (Exception e) {
            log.error(e.toString());
            return null;
        }
    }
}
