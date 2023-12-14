package com.xzh.controller;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.xzh.entity.Hero;
import com.xzh.service.HeroService;
import com.xzh.utils.HttpClientUtils;
import com.xzh.utils.JsoupUtils;
import com.xzh.utils.Result;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author xzh
 * @since 2023-11-24
 */
@RestController
@RequestMapping("/hero")
public class HeroController {

    @Autowired
    private HeroService heroService;

    @GetMapping("/getHeroImage")
    public Result getHeroCodeByName(@RequestBody String name) throws Exception {
        JsoupUtils scraper = new JsoupUtils();

        String urlToScrape = "https://epic7.gamekee.com/";
        // get the HTML document from the target url
        Document document = scraper.getDocumentFromURL(new URL(urlToScrape));
        // 获取所有的链接
        Elements links = document.select("a[href]");
        HashMap<String, String> map = Maps.newHashMap();
        map.put("Game-Alias","epic7");
        map.put("Game-Id","12");
        File file = new File("/Users/xiaoxu/Downloads/Vp/");
        String[] list = file.list();
        int skip = 0;
        for (Element link : links) {
            skip++;
            if(skip<130){
                continue;
            }
            if(!link.attr("class").equals("item")){
                continue;
            }
            String linkText = link.text();
            String linkUrl = link.attr("href");
            Result heroCodeByName = heroService.getHeroCodeByName(linkText);
            System.out.println(linkText+","+heroCodeByName);
            if(heroCodeByName.getCode() == 20000){
                Hero hero = (Hero)heroCodeByName.getData();
                String code = hero.getCode();
                //list包含code
                if(list!=null){
                    boolean anyMatch = Arrays.stream(list).anyMatch(it -> it.replaceAll("\\.png", "").equals(code));
                    if(anyMatch){
                        continue;
                    }
                }
                String str = linkUrl.replaceAll("/", "").replaceAll(".html", "");
                String response = HttpClientUtils.get("https://epic7.gamekee.com/v1/content/detail/" + str, map);
                if(response == null){
                    continue;
                }
                JSONObject data = JSONObject.parseObject(response).getJSONObject("data");
                if(data==null){
                    continue;
                }
                String thumb = data.getString("thumb");
                if(StringUtils.isEmpty(thumb)){
                    continue;
                }

                String[] split = thumb.split(",");
                for (int index = 0; index < split.length; index++) {
                    String thumbAddress = split[index];
                    String s = "/Users/xiaoxu/Downloads/Vp/" + code + ".png";
                    saveImage(thumbAddress,s);
                    File image = new File(s);
                    long length = image.length();
                    if(length<102400){
                        continue;
                    }
                }
            }
        }
        return Result.success();
    }

    private void saveImage(String address, String savePath) throws Exception {
        URL url = new URL("https:"+address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int resCode = connection.getResponseCode();
        if (resCode == HttpURLConnection.HTTP_OK) {
            //读取数据并写入到文件中
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outStream = new FileOutputStream(savePath)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, length);
                }
                outStream.flush();
                System.out.println("完成："+savePath);
            }
        } else {
            System.out.println("文件下载错误码为" + resCode);
        }
    }

    @GetMapping("/getById")
    public Result getById(@RequestBody String code){
        if(StringUtils.isEmpty(code)){
            return Result.error().message("code不能为空");
        }
        Hero hero = heroService.getById(code);
        return Result.success(hero);
    }


    @GetMapping("/initOrUpdate")
    public Result initOrUpdate(){
        System.out.println(heroService.count());
        heroService.initOrUpdate();
        System.out.println(heroService.count());
        return Result.success();
    }

}

