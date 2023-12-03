package com.xzh.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /** API秘钥 添加自己的秘钥 */
    @Value("${chtgpt.key}")
    private static final String KEY = "";
    /** url */
    private static final String URL = "https://api.openai.com/v1/chat/completions";

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
            reply = AiOne(inputName);
            if (reply == null) {
                reply = "宝，回复失败!重新试试把!";
            }
        } catch (IOException e) {
            reply = "宝，回复失败!重新试试把!";
        }
        //异步回复消息
        groupMessageEvent.replyAsync(reply);
    }

    public static String AiOne(String sendMsg) throws JsonProcessingException {
        // 创建 ObjectMapper 用于解析 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        Text text = new Text();
        // 设置模型
        text.setModel("gpt-3.5-turbo");
        // 值越小，生成的文本越可信，但也越无创造性 值越大，生成的文本越有创造性，但也越不可信 范围：0.0-1.0
        text.setTemperature(0.7);
        text.setMessages(Collections.singletonList(new Text.MessagesBean("user", sendMsg)));
        OkHttpClient client = new OkHttpClient.Builder().callTimeout(Duration.ofMinutes(1)).build();
        // 创建请求体，携带 JSON 参数
        RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(text),
                MediaType.parse("application/json; charset=utf-8"));
        // 创建请求
        Request request =
                new Request.Builder().url(URL).addHeader("Authorization", "Bearer ".concat(KEY)).post(requestBody).build();
        // 发送请求并处理响应
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            // 解析json 获取结果
            JsonNode jsonNode = objectMapper.readTree(response.body().string());
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
