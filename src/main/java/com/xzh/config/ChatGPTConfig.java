package com.xzh.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xzh.entity.Text;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

@Component
public class ChatGPTConfig {

    /** API秘钥 添加自己的秘钥 */
    @Value("${chat.gpt.apikey}")
    public static final String API_KEY = "";
    /** url */
    public static final String URL = "https://api.openai.com/v1/chat/completions";

    public static String completions(String sendMsg) throws JsonProcessingException {
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
                new Request.Builder().url(URL).addHeader("Authorization", "Bearer ".concat(API_KEY)).post(requestBody).build();
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
