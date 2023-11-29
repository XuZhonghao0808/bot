package com.xzh;

import java.io.IOException;
import java.util.*;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xzh.entity.Hero;
import com.xzh.utils.HttpClientUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

public class HttpClientTest {
    public static void main(String[] args) {

        HttpClientTest test = new HttpClientTest();
        //测试get请求
        test.testGet();
        //测试String类型Post请求
//        test.testStringPost();
//        //测试Map类型Post请求
//        test.testMapPost();
    }


    /**
     * 测试POST请求（String入参）
     */
    private void testStringPost() {

        String url = "http://107.12.57.187:8080/sms/post1";
        String str = "{\"english\":\"json\"}";
        try {
            String result = HttpClientUtils.post(url, str);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试POST请求（Map入参）
     */
    private void testMapPost() {
        String url = "http://107.12.57.187:8080/sms/post1";
        HashMap<String, String> map = new HashMap<>();
        map.put("english", "json");
        try {
            String result = HttpClientUtils.post(url, map);
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试GET请求
     */
    private void testGet() {
        String url = "https://static.smilegatemegaport.com/gameRecord/epic7/epic7_hero.json?_=1700726958007";
        try {
            String result = HttpClientUtils.get(url);
            if(StringUtils.isBlank(result)){
                return;
            }

            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("zh-CN");
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject heroMessage = jsonArray.getJSONObject(i);
                String code = heroMessage.getString("code");
                Integer grade = heroMessage.getInteger("grade");
                String name = heroMessage.getString("name");
                String job_cd = heroMessage.getString("job_cd");
                String attribute_cd = heroMessage.getString("attribute_cd");
                Hero hero = new Hero();
                hero.setCode(code);
                hero.setGrade(grade);
                hero.setName(name);
                hero.setJobCd(job_cd);
                hero.setAttributeCd(attribute_cd);

            }
//            setOtherNameValue(list,jsonObject,"de");
//            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setOtherNameValue(List<Hero> list, JSONObject jsonObject, String key) {
        List<Object> list2 = Lists.newArrayList();
        JSONArray jsonArray = jsonObject.getJSONArray(key);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject heroMessage = jsonArray.getJSONObject(i);
            String code = heroMessage.getString("code");
            String name = heroMessage.getString("name");
            Optional<Hero> first = list.stream().filter(it -> Objects.equals(it.getCode(), code)).findFirst();
            if (first.isPresent()) {
                Hero hero = first.get();
                hero.setDeName(name);
                list2.add(hero);
            }
        }
    }
}


