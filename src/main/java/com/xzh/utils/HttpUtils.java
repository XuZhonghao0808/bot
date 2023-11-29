package com.xzh.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class HttpUtils {

    private static final Logger logger = LogManager.getLogger(HttpUtils.class);

    @Autowired
    private CloseableHttpClient httpClient;

    @Autowired
    private RequestConfig config;

    /**
     * 发送POST请求
     *
     * @param url   请求URL
     * @param param 请求体
     * @return
     */
    public String doPost(String url, Map<String, Object> param) {

        // 创建Http Post请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);

        CloseableHttpResponse response = null;
        String resultString = "";
        try {

            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : param.entrySet()) {
                    paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            logger.error("调用第三方错误:{}", e.getMessage());
            return null;
        } finally {
            try {
                httpPost.abort();
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送post请求
     *
     * @param url     请求URL
     * @param param   请求体
     * @param headers 请求头信息
     * @return
     */
    public String doPost(String url, Map<String, Object> param, Map<String, String> headers) {

        // 创建Http Post请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(config);
        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach((k, v) -> httpPost.addHeader(k, v));
        }

        CloseableHttpResponse response = null;
        try {
            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : param.entrySet()) {
                    paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            return EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (Exception e) {
            logger.error("调用第三方错误:{}", e.getMessage());
            return null;
        } finally {
            try {
                httpPost.abort();
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}