package com.xzh.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.xzh.config.BaiduOCRConfig;
import com.xzh.entity.Artifact;
import com.xzh.entity.Hero;
import com.xzh.service.ArtifactService;
import com.xzh.service.HeroService;
import com.xzh.utils.EpicSevenUtils;
import com.xzh.utils.ImageCompositingUtils;
import com.xzh.utils.PinyinFuzzySearchUtil;
import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Filters;
import love.forte.simboot.annotation.Listener;
import love.forte.simboot.filter.MatchType;
import love.forte.simboot.filter.MultiFilterMatchType;
import love.forte.simbot.component.mirai.message.MiraiImage;
import love.forte.simbot.event.GroupMessageEvent;
import love.forte.simbot.message.*;

import love.forte.simbot.resources.Resource;
import love.forte.simbot.resources.StandardResource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EpicSevenMessageListener {
    private static final Logger log = LoggerFactory.getLogger(EpicSevenMessageListener.class);

    @Autowired
    private HeroService heroService;

    @Autowired
    private ArtifactService artifactService;

    /**
     * 月光召唤
     */
    @Filter(value = "月光召唤", matchType = MatchType.TEXT_EQUALS)
    @Listener
    @ContentTrim
    public synchronized void streaksOfMoonlight(GroupMessageEvent groupMessageEvent) {
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        List<Integer> gradeList = EpicSevenUtils.getTenStreaksOfMoonlight(1);
        Pair<String, String> heroByGrade = getHeroByGrade(gradeList.get(0));
        if(heroByGrade == null){
            return;
        }
        //图片合成
        try {
            URL url = new URL("https://epic-seven.oss-cn-beijing.aliyuncs.com/Vp/"+heroByGrade.getKey()+".png");
            if(url.openStream() == null){
                messagesBuilder.text("没有立绘："+heroByGrade.getValue());
            }else {
                BufferedImage bufferedImage = ImageIO.read(url);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", os);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(os.toByteArray());
                messagesBuilder.image(Resource.of(inputStream));
            }

            groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        messagesBuilder.text("生成图片失败");
        //异步回复消息
        groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
    }

    /**
     * 月光十连
     */
    @Filter(value = "#月光十连", matchType = MatchType.TEXT_EQUALS)
    @Listener
    @ContentTrim
    public synchronized void tenStreaksOfMoonlight(GroupMessageEvent groupMessageEvent) {
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        List<Integer> gradeList = EpicSevenUtils.getTenStreaksOfMoonlight(10);
        List<String> heroList = getHeroByGrade(gradeList);
        //图片合成
        try {
            InputStream inputStream = ImageCompositingUtils.tenStreaksOfMoonlight(heroList);
            messagesBuilder.image(Resource.of(inputStream));
//            messagesBuilder.text(heroList.toString());
            groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        messagesBuilder.text("生成图片失败");
        //异步回复消息
        groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
    }

    private List<String> getHeroByGrade(List<Integer> gradeList) {
        List<String> heroList = Lists.newArrayList();
        QueryWrapper<Hero> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("attribute_cd","dark")
                .or()
                .eq("attribute_cd","light");
        List<Hero> list = heroService.list(queryWrapper);
        gradeList.forEach(grade -> {
            List<Hero> heroes = list.stream().filter(it -> Objects.equals(it.getGrade(), grade)).toList();
            //随机取一个
            if (!CollectionUtils.isEmpty(heroes)) {
                Hero hero = heroes.get(new Random().nextInt(heroes.size()));
                heroList.add(hero.getCode());
            }
        });
        return heroList;
    }

    private Pair<String,String> getHeroByGrade(Integer grade) {
        QueryWrapper<Hero> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("attribute_cd","dark")
                .or()
                .eq("attribute_cd","light");
        List<Hero> list = heroService.list(queryWrapper);
        List<Hero> heroes = list.stream().filter(it -> Objects.equals(it.getGrade(), grade)).toList();
        //随机取一个
        if (!CollectionUtils.isEmpty(heroes)) {
            Hero hero = heroes.get(new Random().nextInt(heroes.size()));
            return Pair.of(hero.getCode(),hero.getName());
        }
        return null;
    }

    /**
     * OCR装备算分
     */
    @Filters(value = {
            @Filter(value = "分数", matchType = MatchType.TEXT_ENDS_WITH),
            @Filter(value = "算分", matchType = MatchType.TEXT_ENDS_WITH)
    }, multiMatchType = MultiFilterMatchType.ANY)
    @Listener
    @ContentTrim
    public synchronized void ocrProcess(GroupMessageEvent groupMessageEvent) {
        ReceivedMessageContent messageContent = groupMessageEvent.getMessageContent();
        Messages messages = messageContent.getMessages();
        // 遍历消息链取第一张图
        String url = null;
        for (Message.Element<?> element : messages) {
            if (element instanceof MiraiImage image) {
                url = image.getResource().getName();
            }
        }
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        if (url == null) {
            messagesBuilder.text("未找到图片");
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }
        String ocrValue = ImageCompositingUtils.getOcrValue(url);
        if (ocrValue == null) {
            messagesBuilder.text("识别失败");
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }
        //图片合成
        try {
            InputStream inputStream = ImageCompositingUtils.scoreProcess(ocrValue);
            messagesBuilder.image(Resource.of(inputStream));
            groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        messagesBuilder.text("生成图片失败");
        //异步回复消息
        groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());

    }


    /**
     * 进攻xxx，xxx，xxx
     * 抄作业xxx，xxx，xxx
     */
    @Filters(value = {
            @Filter(value = "进攻", matchType = MatchType.TEXT_CONTAINS),
            @Filter(value = "抄作业", matchType = MatchType.TEXT_CONTAINS)
    }, multiMatchType = MultiFilterMatchType.ANY)
    @Listener
    @ContentTrim
    public synchronized void getDef(GroupMessageEvent groupMessageEvent) {
        String msg = groupMessageEvent.getMessageContent().getPlainText();
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        String inputName = msg.replaceAll("进攻", "").replaceAll("抄作业", "");
        if (StringUtils.isEmpty(inputName)) {
            return;
        }
        String[] split = inputName.replaceAll("，", ",").split(",");
        if (split.length != 3) {
            messagesBuilder.text("必须三个防守角色");
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }
        //别名、谐音查询
        String code = getHeroCodeByName(split);
        if (code.split(",").length != 3) {
            messagesBuilder.text("未查到角色名称");
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }
        log.info(groupMessageEvent.getGroup().getId() + "进攻：输入内容：" + inputName + ",处理后查询英雄code：" + code);

        String gvgDef = EpicSevenUtils.getDef(code);
        if (gvgDef == null) {
            messagesBuilder.text("没有相关的gvg数据");
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }

        //图片合成
        try {
            InputStream inputStream = ImageCompositingUtils.defProcess(code,gvgDef);
            StandardResource of = Resource.of(inputStream);
            messagesBuilder.image(of);
//            messagesBuilder.text(gvgDef);
            groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        messagesBuilder.text("生成图片失败");
        //异步回复消息
        groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
    }

    /**
     * 防守排行
     */
    @Filters(value = {
            @Filter(value = "防守排行榜", matchType = MatchType.TEXT_CONTAINS),
    }, multiMatchType = MultiFilterMatchType.ANY)
    @Listener
    @ContentTrim
    public synchronized void defRank(GroupMessageEvent groupMessageEvent) {
        String msg = groupMessageEvent.getMessageContent().getPlainText();
        MessagesBuilder messagesBuilder = new MessagesBuilder();

        String gvgDef = EpicSevenUtils.defRank();
        if (gvgDef == null) {
            messagesBuilder.text("没有相关的gvg数据");
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }
        //图片合成
        try {
            InputStream inputStream = ImageCompositingUtils.defProcess(null,gvgDef);
            StandardResource of = Resource.of(inputStream);
            messagesBuilder.image(of);
//            messagesBuilder.text(gvgDef);
            groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        messagesBuilder.text("生成图片失败");
        //异步回复消息
        groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
    }

    /**
     * xxx装备推荐
     */
    @Filters(value = {
            @Filter(value = "装备推荐", matchType = MatchType.TEXT_ENDS_WITH),
            @Filter(value = "出装推荐", matchType = MatchType.TEXT_ENDS_WITH)
    }, multiMatchType = MultiFilterMatchType.ANY)
    @Listener
    @ContentTrim
    public synchronized void getHeroLibrary(GroupMessageEvent groupMessageEvent) {
        String msg = groupMessageEvent.getMessageContent().getPlainText();
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        String inputName = msg.replaceAll("装备推荐", "").replaceAll("出装推荐", "");
        if (StringUtils.isEmpty(inputName)) {
            return;
        }
        //别名、谐音查询
        Pair<String, String> enAndChName = processHeroEnName(inputName);
        if (enAndChName == null) {
            messagesBuilder.text("未查到角色名称，可通过xxx添加别名xxx命令添加");
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }
        log.info(groupMessageEvent.getGroup().getId() + "装备推荐查询：输入内容：" + inputName + ",处理后查询英雄：" + enAndChName.getRight());
        //所有神器中英名称
        List<Artifact> artifactList = artifactService.list();
        JSONObject heroLibrary = EpicSevenUtils.getHeroLibrary(enAndChName, artifactList);
        if (heroLibrary == null) {
            messagesBuilder.text("未查到");
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }
        String code = heroLibrary.getString("code");
        String valuePercent = heroLibrary.getString("valuePercent");
        String artifactPercent = heroLibrary.getString("artifactPercent");
        String setsPercent = heroLibrary.getString("setsPercent");

        //图片合成
        try {
            InputStream inputStream = ImageCompositingUtils.heroGearProcess(
                    valuePercent,
                    artifactPercent,
                    setsPercent,
                    code);
            StandardResource of = Resource.of(inputStream);
            messagesBuilder.image(of);
            groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        messagesBuilder.text("生成图片失败");
        //异步回复消息
        groupMessageEvent.getGroup().sendAsync(messagesBuilder.build());
//        groupMessageEvent.replyAsync();
    }

    /**
     * xxx添加别名xxx,xxx,xxx
     */
    @Filter(value = "添加别名", matchType = MatchType.TEXT_CONTAINS)
    @Listener
    @ContentTrim
    public synchronized void addAlias(GroupMessageEvent groupMessageEvent) {
        String msg = groupMessageEvent.getMessageContent().getPlainText();
        log.info(groupMessageEvent.getGroup().getId() + ": " + msg);
        MessagesBuilder messagesBuilder = new MessagesBuilder();
        String[] split = msg.split("添加别名");
        if (split.length < 2) {
            return;
        }
        //别名、谐音查询
        Pair<String, String> enAndChName = processHeroEnName(split[0]);
        if (enAndChName == null) {
            messagesBuilder.text("没找到" + split[0]);
            //异步回复消息
            groupMessageEvent.replyAsync(messagesBuilder.build());
            return;
        }
        List<Hero> heroList = getHeroByEnName(enAndChName.getLeft());
        if (CollectionUtils.isEmpty(heroList)) {
            return;
        }
        heroList.forEach(hero -> {
            String alias = hero.getAlias();
            String addAlisa = split[1].replaceAll("，", ",");
            String[] aliasArray = addAlisa.split(",");
            for (String item : aliasArray) {
                if (StringUtils.isEmpty(alias)) {
                    alias = item;
                } else {
                    String[] split1 = alias.split(",");
                    if (Arrays.asList(split1).contains(item)) {
                        continue;
                    }
                    alias = alias + "," + item;
                }
            }
            hero.setAlias(addAlisa);
            heroService.updateById(hero);
        });
        messagesBuilder.text("更新完成");
        //异步回复消息
        groupMessageEvent.replyAsync(messagesBuilder.build());
    }

    private List<Hero> getHeroByEnName(String enName) {
        QueryWrapper<Hero> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("en_name", enName);
        return heroService.list(queryWrapper);
    }

    private Pair<String, String> processHeroEnName(String inputName) {
        Hero hero = getHeroByName(inputName);
        if(hero == null){
            return null;
        }
        return Pair.of(hero.getEnName(), hero.getName());
    }

    private String getHeroCodeByName(String[] inputName) {
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(inputName).forEach(s -> {
            Hero hero = getHeroByName(s);
            if(stringBuilder.length() != 0){
                stringBuilder.append(",");
            }
            stringBuilder.append(hero.getCode());
        });
        return stringBuilder.toString();
    }

    private Hero getHeroByName(String name) {
        QueryWrapper<Hero> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        List<Hero> list = heroService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            //别名查询
            List<Hero> heroList = heroService.list();
            List<Hero> filterHeroList = heroList.stream().filter(hero -> matchAlias(hero.getName(),hero.getAlias(), name)).toList();
            if (CollectionUtils.isEmpty(filterHeroList)) {
                return null;
            }
            return filterHeroList.get(0);
        }
        return list.get(0);
    }

    private boolean matchAlias(String name,String alias, String checkName) {
        if(PinyinFuzzySearchUtil.chineseCharactersFuzzySearch(name,checkName)){
            return true;
        }
        if(StringUtils.isEmpty(alias)){
            return false;
        }
        String[] split = alias.split(",");
        //别名查不到，再使用谐音查询
        return Arrays.stream(split).anyMatch(it ->
                        Objects.equals(it, checkName)
                        || PinyinFuzzySearchUtil.chineseCharactersFuzzySearch(it,checkName));
    }
}
