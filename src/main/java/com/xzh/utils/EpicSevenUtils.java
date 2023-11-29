package com.xzh.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.xzh.entity.Artifact;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class EpicSevenUtils {

    public static String getDef(String codes) {
        String url = "https://krivpfvxi0.execute-api.us-west-2.amazonaws.com/dev/getDef";
        String result = sendPost(url, codes);
        JSONObject resultJson = JSONObject.parseObject(result);
        if (!resultJson.containsKey("data")) {
            return null;
        }
        JSONObject data = resultJson.getJSONObject("data");
        Map<String, Integer> map = new LinkedHashMap<>();
        //按场次排序
        for (String key : data.keySet()) {
            JSONObject value = data.getJSONObject(key);
            Integer w = value.getInteger("w");
            Integer l = value.getInteger("l");
            Integer d = value.getInteger("d");
            if (w != null && l != null && d != null) {
                map.put(key, w + d + l);
            }
        }
        LinkedHashMap<String, Integer> newMap = map.entrySet().stream()
                .sorted((entry1, entry2) -> Math.toIntExact(entry2.getValue() - entry1.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        int count = 0;
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : newMap.entrySet()) {
            if (count > 9) {
                break;
            }
            count++;
            JSONObject value = data.getJSONObject(entry.getKey());
            Integer w = value.getInteger("w");
            Integer l = value.getInteger("l");
            Double sum = Double.valueOf(entry.getValue());
            DecimalFormat decimalFormat = new DecimalFormat("#.#%");
            String percentage = decimalFormat.format(w / sum);
            stringBuilder.append(entry.getKey()).append("：")
                    .append(w).append("，")
                    .append(l).append("，")
                    .append(percentage).append("\n");
        }
        return stringBuilder.toString();
    }


    public static JSONObject getHeroLibrary(Pair<String, String> enAndCnName, List<Artifact> artifactList) {
        String url = "https://krivpfvxi0.execute-api.us-west-2.amazonaws.com/dev/getBuilds";
        String result = sendPost(url, enAndCnName.getLeft());
        JSONObject resultJson = JSONObject.parseObject(result);
        JSONArray dataJsonArray = resultJson.getJSONArray("data");
        //统计属性平均值
        if (dataJsonArray.size() == 0) {
            return null;
        }
        String code = dataJsonArray.getJSONObject(0).getString("unitCode");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", code);
        //前5%的属性
        jsonObject.put("valuePercent", get5Percent(dataJsonArray, enAndCnName.getRight()));
        //全部神器占比
        jsonObject.put("artifactPercent", getArtifactPercent(dataJsonArray, artifactList));
        //全部装备套装占比
        jsonObject.put("setsPercent", getSetsPercent(dataJsonArray));
        return jsonObject;
    }

    private static String getSetsPercent(JSONArray dataJsonArray) {
        Map<JSONObject, Long> artifactCode = dataJsonArray.stream()
                .map(it -> ((JSONObject) it).getJSONObject("sets"))
                .collect(Collectors.groupingBy(it -> it, Collectors.counting()));
        LinkedHashMap<JSONObject, Long> newMap = artifactCode.entrySet().stream()
                .sorted((entry1, entry2) -> Math.toIntExact(entry2.getValue() - entry1.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        //循环map
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        Double size = (double) dataJsonArray.size();
        for (Map.Entry<JSONObject, Long> entry : newMap.entrySet()) {
            if (count > 9) {
                break;
            }
            StringBuilder stringBuilder2 = new StringBuilder();
            JSONObject sets = entry.getKey();
            sets.keySet().forEach(it -> {
                if (stringBuilder2.length() != 0) {
                    stringBuilder2.append(",");
                }
                stringBuilder2.append(it);

            });
            //占比
            Long num = entry.getValue();
            DecimalFormat decimalFormat = new DecimalFormat("#.#%");
            String percentage = decimalFormat.format(num / size);
            stringBuilder.append(stringBuilder2).append(" ").append(percentage).append("\n");
            count++;
        }
        return stringBuilder.toString();
    }

    private static String getArtifactPercent(JSONArray dataJsonArray, List<Artifact> artifactList) {
        Map<String, Long> artifactCode = dataJsonArray.stream()
                .map(it -> ((JSONObject) it).getString("artifactCode"))
                .collect(Collectors.groupingBy(it -> it, Collectors.counting()));
        LinkedHashMap<String, Long> newMap = artifactCode.entrySet().stream()
                .sorted((entry1, entry2) -> Math.toIntExact(entry2.getValue() - entry1.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        //循环map
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        Double size = (double) dataJsonArray.size();
        for (Map.Entry<String, Long> entry : newMap.entrySet()) {
            if (count > 9) {
                break;
            }
            String code = entry.getKey();
            Optional<Artifact> first = artifactList.stream().filter(it -> Objects.equals(it.getCode(), code)).findFirst();
            if (first.isEmpty()) {
                continue;
            }
            String name = first.get().getName();
            //占比
            Long num = entry.getValue();
            DecimalFormat decimalFormat = new DecimalFormat("#.#%");
            String percentage = decimalFormat.format(num / size);
            stringBuilder.append(name).append(" ").append(percentage).append("\n");
            count++;
        }
        return stringBuilder.toString();
    }

    private static String get5Percent(JSONArray dataJsonArray, String cnName) {
        double atk = 0;//攻击
        double def = 0;//防御
        double hp = 0;//生命
        double spd = 0;//速度
        double chc = 0;//暴击率
        double chd = 0;//暴击伤害
        double eff = 0;//效果命中
        double efr = 0;//效果抗性
        int count = 0;
        int size = dataJsonArray.size();
        for (int i = 0; i < size / 20; i++) {
            JSONObject item = dataJsonArray.getJSONObject(i);
            Double atk1 = item.getDouble("atk");
            atk += atk1;
            Double def1 = item.getDouble("def");
            def += def1;
            Double hp1 = item.getDouble("hp");
            hp += hp1;
            Double spd1 = item.getDouble("spd");
            spd += spd1;
            Double chc1 = item.getDouble("chc");
            chc += chc1;
            Double chd1 = item.getDouble("chd");
            chd += chd1;
            Double eff1 = item.getDouble("eff");
            eff += eff1;
            Double efr1 = item.getDouble("efr");
            efr += efr1;
            count++;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(cnName).append("\n");
        stringBuilder.append("前5%（").append(count).append("条）属性平均值：").append("\n");
        stringBuilder.append("攻击：").append(Math.round(atk / count)).append("\n");
        stringBuilder.append("防御：").append(Math.round(def / count)).append("\n");
        stringBuilder.append("生命：").append(Math.round(hp / count)).append("\n");
        stringBuilder.append("速度：").append(Math.round(spd / count)).append("\n");
        stringBuilder.append("暴击率：").append(Math.round(chc / count)).append("\n");
        stringBuilder.append("暴击伤害：").append(Math.round(chd / count)).append("\n");
        stringBuilder.append("效果命中：").append(Math.round(eff / count)).append("\n");
        stringBuilder.append("效果抗性：").append(Math.round(efr / count)).append("\n");
        return stringBuilder.toString();
    }

    public static String getHeroAnalysis(String code, String nowSeasonCode) {
        String url = "https://epic7.gg.onstove.com/gameApi/getHeroAnalysis";
        HashMap<String, String> map = new HashMap<>();
        map.put("lang", "zh-TW");
        map.put("hero_code", code);
        map.put("season_code", nowSeasonCode);
        map.put("grade_code", "master");
        String result = sendPost(url, map);
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONObject resultBody = jsonObject.getJSONObject("result_body");
//        for (int i = 0; i < jsonArray.size(); i++) {
//
//            return jsonObject.getString("season_code");
//        }
        return null;
    }


    public static String getNowSeasonCode() {
        String url = "https://epic7.gg.onstove.com/gameApi/getSeasonList";
        HashMap<String, String> map = new HashMap<>();
        map.put("lang", "zh-TW");
        String result = null;
        try {
            result = HttpClientUtils.post(url, map);
        } catch (IOException e) {
            return null;
        }
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        JSONArray jsonArray = JSONObject.parseArray(result);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Integer isNowSeason = jsonObject.getInteger("is_now_season");
            if (isNowSeason != 1) {
                continue;
            }
            return jsonObject.getString("season_code");
        }
        return null;
    }


    private static String sendPost(String url, HashMap<String, String> map) {
        String result = null;
        try {
            result = HttpClientUtils.post(url, map);
        } catch (IOException e) {
            return null;
        }
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        return result;
    }

    private static String sendPost(String url, String str) {
        String result = null;
        try {
            result = HttpClientUtils.post(url, str);
        } catch (IOException e) {
            return null;
        }
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        return result;
    }


    private static String sendGet(String url) {
        String result = null;
        try {
            result = HttpClientUtils.get(url);
        } catch (IOException e) {
            return null;
        }
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        return result;
    }
}