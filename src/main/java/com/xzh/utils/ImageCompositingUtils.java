package com.xzh.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.xzh.config.BaiduOCRConfig;
import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static net.sf.jsqlparser.parser.feature.Feature.set;

public class ImageCompositingUtils {

    private static final BaiduOCRConfig baiduOCRConfig = new BaiduOCRConfig();
    private static final String ossPath = "https://epic-seven.oss-cn-beijing.aliyuncs.com";
    private static final String fontName = "Songti";
    private static final int fontStyle = Font.PLAIN;
    private static final int fontSize = 14;
    private static final Color fontColor = Color.BLACK;
    private static final Color backgroundColor = Color.WHITE;


    public static InputStream defProcess(String code, String defStr) throws IOException {
        // 创建一个 BufferedImage 对象
        BufferedImage image = new BufferedImage(1300, 1400, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // 设置背景色和文本颜色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, 1300, 1400);
        graphics.setColor(Color.BLACK);

        // 设置字体
        Font font = new Font(fontName, fontStyle, 60);
        graphics.setFont(font);

        List<Pair<String, BufferedImage>> imageList = Lists.newArrayList();
        // 绘制防守角色文本
        String[] lines = code.split("\n");
        int y = 10;
        for (String line : lines) {
            String[] split = line.split(",");
            int x = 5;
            for (int i = 0; i < split.length; i++) {
                String heroCode = split[i];
                Optional<Pair<String, BufferedImage>> first = imageList.stream()
                        .filter(it -> it.getKey().equals(heroCode)).findFirst();
                BufferedImage bufferedImage;
                if(first.isPresent()){
                    bufferedImage = first.get().getRight();
                }else {
                    URL url = new URL(ossPath+"/cachedimages/" + heroCode + "_s.png");
                    bufferedImage = ImageIO.read(url);
                    imageList.add(Pair.of(heroCode,bufferedImage));
                }
                graphics.drawImage(bufferedImage,x,y,100,100,null);
                x += 120;
            }
            y += 120;
        }
        y += 40;
        graphics.drawString("-----------------------------------", 5, y);
        y += 40;
        // 绘制进攻角色文本
        String[] lines2 = defStr.split("\n");
        URL url1 = new URL(ossPath+"/cachedimages/battle_pvp_icon_lose.png");
        BufferedImage lose = ImageIO.read(url1);
        URL url2 = new URL(ossPath+"/cachedimages/battle_pvp_icon_win.png");
        BufferedImage win = ImageIO.read(url2);
        for (String line : lines2) {
            String codes = line.split("：")[0];
            String[] split = codes.split(",");
            int x = 5;
            for (int i = 0; i < split.length; i++) {
                String heroCode = split[i];
                Optional<Pair<String, BufferedImage>> first = imageList.stream()
                        .filter(it -> it.getKey().equals(heroCode)).findFirst();
                BufferedImage bufferedImage;
                if(first.isPresent()){
                    bufferedImage = first.get().getRight();
                }else {
                    URL url = new URL(ossPath+"/cachedimages/" + heroCode + "_s.png");
                    bufferedImage = ImageIO.read(url);
                    imageList.add(Pair.of(heroCode,bufferedImage));
                }
                graphics.drawImage(bufferedImage,x,y,100,100,null);
                x += 120;
            }
            String[] split1 = line.split("：")[1].split("，");
            graphics.drawImage(win,x + 50,y,100,100,null);
            String w = split1[0];
            graphics.drawString(w, x + 150, y + 80);
            graphics.drawImage(lose,x + 350,y,100,100,null);
            String l = split1[1];
            graphics.drawString(l, x + 450, y + 80);
            graphics.drawString(split1[2], x + 650, y + 80);
            y += 120;
        }
        // 释放资源
        graphics.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    public static InputStream heroGearProcess(String valuePercent, String artifactPercent, String setsPercent, String code) throws IOException {
        // 创建一个 BufferedImage 对象
        BufferedImage image = new BufferedImage(880, 210, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // 设置背景色和文本颜色
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, 880, 210);
        graphics.setColor(fontColor);

        // 设置字体
        Font font = new Font(fontName, fontStyle, fontSize);
        graphics.setFont(font);

        // 绘制属性值文本
        String[] lines = valuePercent.split("\n");
        int y = 20;
        for (String line : lines) {
            graphics.drawString(line, 280, y);
            y += 20;
        }
        // 绘制神器占比文本
        String[] lines2 = artifactPercent.split("\n");
        y = 20;
        for (String line : lines2) {
            graphics.drawString(line, 480, y);
            y += 20;
        }
        // 绘制装备套装占比文本
        String[] lines3 = setsPercent.split("\n");
        y = 20;
        List<Pair<String, URL>> urlList = Lists.newArrayList();
        for (String line : lines3) {
            String[] split = line.split(" ");
            String[] sets = split[0].split(",");
            int x = 680;
            for (int i = 0; i < sets.length; i++) {
                String set = sets[i];
                Optional<Pair<String, URL>> first = urlList.stream().filter(it -> it.getKey().equals(set)).findFirst();
                URL url;
                if(first.isPresent()){
                    url = first.get().getRight();
                }else {
                    url = new URL(ossPath+"/sets/" + set + ".png");
                    urlList.add(Pair.of(set,url));
                }
                BufferedImage bufferedImage = ImageIO.read(url);
                graphics.drawImage(bufferedImage,x,y - 14,20,20,null);
                x += 24;
            }
            graphics.drawString(split[1], x, y);
            y += 20;
        }
        //File file = ResourceUtils.getFile("classpath:cachedimages/"+code+"_su.png");
        URL url = new URL(ossPath+"/cachedimages/"+code+"_su.png");
        BufferedImage bufferedImage = ImageIO.read(url);
        graphics.drawImage(bufferedImage,0,0,280,200, 0, 0, 280, 200,null);
        // 释放资源
        graphics.dispose();
        // 保存生成的图片
//            File output = new File("/Users/xiaoxu/IdeaProjects/bot/src/main/resources/image/a.jpg");
//            ImageIO.write(image, "png", output);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }

    public static String getOcrValue(String url) {
        String[] list = new String[]{"攻击力", "攻擊力", "防御力", "防禦力", "生命值", "速度", "暴击率", "暴擊率", "暴击伤害", "暴擊傷害", "效果命中", "效果抗性"};
        String s = baiduOCRConfig.accurateBasicGeneralUrl(url);
        if (s == null) {
            return s;
        }
        JSONObject jsonObject = JSONObject.parseObject(s);
        JSONArray words_result = jsonObject.getJSONArray("words_result");
        System.out.println(s);
        Double sum = -1d;
        Double sum2 = -1d;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        boolean isRecast = words_result.stream().anyMatch(it -> ((JSONObject) it).getString("words").contains(">") || ((JSONObject) it).getString("words").contains("》"));
        StringBuffer sb = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        for (int i = 0; i < words_result.size(); i++) {
            com.alibaba.fastjson.JSONObject jsonObject1 = words_result.getJSONObject(i);
            String words = jsonObject1.getString("words");
            for (int j = 0; j < list.length; j++) {
                if (words.equals(list[j])) {
                    int index = i + 1;
                    JSONObject jsonObject2 = words_result.getJSONObject(index);
                    String value = jsonObject2.getString("words");
                    boolean containsDigits = value.matches(".*\\d+.*");
                    if (!containsDigits) {
                        index++;
                        jsonObject2 = words_result.getJSONObject(index);
                        value = jsonObject2.getString("words");
                    }
                    if (isRecast) {
                        JSONObject jsonObject3 = words_result.getJSONObject(index + 1);
                        String value2 = jsonObject3.getString("words");
                        if (value2.contains(">") || value2.contains("》")) {
                            jsonObject3 = words_result.getJSONObject(index + 2);
                            value2 = jsonObject3.getString("words");
                        }
                        if (sum2 < 1) {
                            sum2++;
                        }
                        if (sum2 > 0d) {
                            //计算分数
                            Double fraction = getFraction(list[j], value2);
                            String formattedFraction = decimalFormat.format(fraction);
                            sum2 = sum2 + fraction;
                            sb2.append(list[j] + "：" + value2.split("\\(")[0] + "，分数：" + formattedFraction + "\n");
                        }
                    }
                    if (sum < 1) {
                        sum++;
                    }
                    if (sum > 0d) {
                        //计算分数
                        Double fraction = getFraction(list[j], value);
                        String formattedFraction = decimalFormat.format(fraction);
                        sum = sum + fraction;
                        sb.append(list[j] + "：" + value.split("\\(")[0] + "，分数：" + formattedFraction + "\n");
                    }
                }
            }
        }
        String formattedSum = decimalFormat.format(sum);
        sb.append("总分：" + formattedSum + "\n");
        if (isRecast) {
            sb.append("\n");
            sb.append("重铸后分数:\n");
            sb.append(sb2);
            String formattedSum2 = decimalFormat.format(sum2);
            sb.append("总分：" + formattedSum2 + "\n");
        }
        return sb.toString();
    }

    private static Double getFraction(String key, String value) {
        if (Objects.equals(key, "速度")) {
            return Double.valueOf(value.split("\\(")[0]) * 2;
        }
        if (key.equals("暴击率") || key.equals("暴擊率")) {
            return Double.valueOf(value.split("%")[0]) * 1.6;
        }
        if (key.equals("暴击伤害") || key.equals("暴擊傷害")) {
            return Double.valueOf(value.split("%")[0]) * 1.14285714;
        }
        //百分比
        String[] list1 = new String[]{"攻击力", "攻擊力", "防御力", "防禦力", "生命值", "效果命中", "效果抗性"};
        if (Arrays.asList(list1).contains(key) && value.contains("%")) {
            return Double.valueOf(value.split("%")[0]);
        }
        //固定值
        if ((key.equals("攻击力") || key.equals("攻擊力")) && !value.contains("%")) {
            return Double.valueOf(value.split("%")[0]) * (3.46 / 39);
        }
        if ((key.equals("防御力") || key.equals("防禦力")) && !value.contains("%")) {
            return Double.valueOf(value.split("%")[0]) * (4.99 / 31);
        }
        if (key.equals("生命值") && !value.contains("%")) {
            return Double.valueOf(value.split("%")[0]) * (3.09 / 174);
        }
        return 0d;
    }

    /**
     * 装备算分图片合成
     * @param valuePercent
     * @return
     * @throws IOException
     */
    public static InputStream scoreProcess(String valuePercent) throws IOException {
        boolean isRecast = valuePercent.contains("重铸后分数");
        int heigtht = 130;
        if(isRecast){
            heigtht = 260;
        }
        // 创建一个 BufferedImage 对象
        BufferedImage image = new BufferedImage(200, heigtht, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // 设置背景色和文本颜色
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, 200, heigtht);
        graphics.setColor(fontColor);

        // 设置字体
        Font font = new Font(fontName, fontStyle, fontSize);
        graphics.setFont(font);

        // 绘制属性值文本
        String[] lines = valuePercent.split("\n");
        int y = 20;
        for (String line : lines) {
            graphics.drawString(line, 5, y);
            y += 20;
        }
        // 释放资源
        graphics.dispose();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
