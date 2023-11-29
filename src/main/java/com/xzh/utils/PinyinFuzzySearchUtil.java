package com.xzh.utils;


import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 拼音模糊搜索工具类
 */
@Slf4j
public class PinyinFuzzySearchUtil {

    //汉字
    private static final int CHINESE_CHARACTER = 1;
    //拼音
    private static final int PIN_YIN = 2;
    //即包含汉字,也包含字母
    private static final int WHOLE = 3;


    //测试方法
    public static void main(String[] args) {
        List<String> list = Arrays.asList("赛纳", "赛娜", "塞娜", "光兰德");
        list.forEach(name ->  System.out.println("------------------------------最终搜索结果>>>" + chineseCharactersFuzzySearch("塞纳", name)));
    }

    /**
     * 汉字模糊匹配
     *
     * @param chineseCharacters 用户输入的汉字
     * @param checkName          需要匹配的名称
     * @return 匹配是否成功
     */
    public static boolean chineseCharactersFuzzySearch(String chineseCharacters, String checkName) {
//        System.out.println(chineseCharacters+","+checkName);
        if (StringUtils.isEmpty(checkName)) {
            return false;
        }
        if(chineseCharacters.length() != checkName.length()){
            return false;
        }
        //如果用户输入的事多个字符, 则按照谐音去搜索, 既输入长三 可以搜索出张三
        if (chineseCharacters.length() > 1) {
            //将汉字转换为拼音
            List<String> pinyin1 = pinYinConvertNoInitial(chineseCharacters);
            List<String> pinyin2 = pinYinConvertNoInitial(checkName);
            //循环拼音模糊搜索, 将返回值扁平流组装为一个数组
//            System.out.println(chineseCharacters + "---------" + pinyin1);
//            System.out.println(checkName + "---------" + pinyin2);
            return pinyin1.stream().anyMatch(py1 -> pinyin2.stream().anyMatch(py1::equals));
        }
        return false;
    }


    // 将n个数组元素进行组合
    public static List<String> combineArrays(List<List<String>> arrays) {
        List<String> result = new ArrayList<>();
        // 从索引0开始组合
        combineHelper(arrays, 0, "", result);
        return result;
    }

    // 递归函数，用于将数组元素进行组合
    private static void combineHelper(List<List<String>> arrays, int index, String current, List<String> result) {
        // 当索引等于数组个数时，表示已经遍历完所有数组元素，将当前组合添加到结果列表中
        if (index == arrays.size()) {
            result.add(current);
            return;
        }

        // 获取当前数组
        List<String> currentArray = arrays.get(index);
        // 遍历当前数组的元素
        for (String word : currentArray) {
            // 递归调用，将当前元素与下一个数组进行组合
            combineHelper(arrays, index + 1, current + word, result);
        }
    }

    /**
     * 拼音转换(不含首字母)
     *
     * @param chinese 中文姓名
     * @return 拼音
     */
    public static List<String> pinYinConvertNoInitial(String chinese) {
        // 存储拼音字符串
        List<List<String>> pinyinArrays = new ArrayList<>();

        for (int i = 0; i < chinese.length(); i++) {
            char c = chinese.charAt(i);

            // 忽略空格
            if (Character.isWhitespace(c)) {
                continue;
            }

            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
//            log.info("pinyinArray:{}", (Object) pinyinArray);

            if (pinyinArray != null && pinyinArray.length > 0) {
                // 使用正则表达式去除声调
                List<String> pinyin = Arrays.stream(pinyinArray).map(str -> str.replaceAll("[1-5]", "")).collect(Collectors.toList());
                pinyinArrays.add(pinyin);
            }
        }
        //最终的拼音组合
//        System.out.println("---------" + pinyinArrays);
        List<String> pinyinCombinations = combineArrays(pinyinArrays);
//        log.info("输出拼音:{}", JSON.toJSONString(pinyinCombinations));
        return pinyinCombinations;
    }

    /**
     * 拼音转换
     *
     * @param chinese 中文姓名
     * @return 拼音和首字母
     */
    public static String pinYinConvert(String chinese) {


        // 存储拼音字符串
        List<List<String>> pinyinArrays = new ArrayList<>();
        // 存储首字母
        List<List<String>> initialArrays = new ArrayList<>();

        for (int i = 0; i < chinese.length(); i++) {
            char c = chinese.charAt(i);

            // 忽略空格
            if (Character.isWhitespace(c)) {
                continue;
            }


            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
            log.info("pinyinArray:{}", (Object) pinyinArray);

            if (pinyinArray != null && pinyinArray.length > 0) {

                // 使用正则表达式去除声调
                List<String> pinyin = Arrays.stream(pinyinArray).map(str -> str.replaceAll("[1-5]", "")).collect(Collectors.toList());
                pinyinArrays.add(pinyin);

                //首字母
                List<String> initial = Arrays.stream(pinyinArray).map(str -> str.replaceAll("[1-5]", "")).map(str -> String.valueOf(str.charAt(0))).collect(Collectors.toList());
                initialArrays.add(initial);

            }
        }

        //最终的拼音组合
        List<String> pinyinCombinations = combineArrays(pinyinArrays);
        String pinyinCombinationsJoin = String.join(",", pinyinCombinations);

        //最终的首字母组合
        List<String> initialCombinations = combineArrays(initialArrays);
        String initialCombinationsJoin = String.join(",", initialCombinations);

        log.info("输出首字母,拼音:{}", initialCombinationsJoin + "," + pinyinCombinationsJoin);
        return initialCombinationsJoin + "," + pinyinCombinationsJoin;
    }



    /**
     * 汉字模糊搜索
     *
     * @param chineseCharacters 用户输入的汉字
     * @param userName          需要匹配的名称集合
     * @return 匹配成功用户名称
     */
    public static List<String> chineseCharactersFuzzySearch(String chineseCharacters, List<String> userName) {
        if (CollectionUtils.isEmpty(userName)) {
            log.info("userName为空");
            return null;
        }

        //如果用户输入的是单个字符,则按照中文模糊搜索
        if (chineseCharacters.length() == 1) {
            return userName.stream().map(name -> {
                if (name.contains(chineseCharacters)) {
                    return name;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        }

        //如果用户输入的事多个字符, 则按照谐音去搜索, 既输入长三 可以搜索出张三
        if (chineseCharacters.length() > 1) {
            //将汉字转换为拼音
            List<String> pinyin = pinYinConvertNoInitial(chineseCharacters);

            //循环拼音模糊搜索, 将返回值扁平流组装为一个数组
            return pinyin.stream().flatMap(user -> Objects.requireNonNull(pinyinFuzzySearch(user, userName)).stream()).distinct().collect(Collectors.toList());
        }

        return null;
    }

    /**
     * 拼音模糊搜索
     *
     * @param pinyin   用户输入的拼音
     * @param userName 需要匹配的名称集合
     * @return 匹配成功用户名称
     */
    public static List<String> pinyinFuzzySearch(String pinyin, List<String> userName) {

        String lowercasePinYin = pinyin.toLowerCase();

        if (CollectionUtils.isEmpty(userName)) {
            log.info("userName为空");
            return null;
        }

        //存储<拼音大小写, 姓名>
        Map<String, String> pinYinMap = new HashMap<>();
        for (String name : userName) {
            //将姓名转换为 拼音首字母 + 拼音
            String pinYin = pinYinConvert(name);
            pinYinMap.put(pinYin, name);
        }

        List<String> userNameList = new ArrayList<>();

        //循环对拼音进行模糊匹配
        pinYinMap.forEach((py, name) -> {
            if (py.contains(lowercasePinYin)) {
                userNameList.add(name);
            }
        });

        return userNameList;
    }

    /**
     * 判断当前字符串的类型
     *
     * @param userName 用户名称
     * @return 1是汉字 2是字母 3既是汉字也是字母
     */
    public static int determineStringType(String userName) {
        if (userName.matches("[\\u4E00-\\u9FA5]+")) {
//            log.info("字符串由汉字组成:{}", userName);
            return CHINESE_CHARACTER;
        } else if (userName.matches("[a-zA-Z]+")) {
//            log.info("字符串由字母组成:{}", userName);
            return PIN_YIN;
        } else {
//            log.info("字符串既包含汉字又包含字母:{}", userName);
            return WHOLE;
        }
    }

    public static String  convertToChinese(String str) {
        String pattern = ".*\\d+.*"; // 包含数字的正则表达式

        if (str.matches(pattern)) {
            str = str.replaceAll("0", "零")
                    .replaceAll("1", "一")
                    .replaceAll("2", "二")
                    .replaceAll("3", "三")
                    .replaceAll("4", "四")
                    .replaceAll("5", "五")
                    .replaceAll("6", "六")
                    .replaceAll("7", "七")
                    .replaceAll("8", "八")
                    .replaceAll("9", "九");
        }
        return str; // 返回修改后的值
    }


    public static List<String> numberToChineseCharacters(List<String> userName) {
        List<String> list = new ArrayList<>();
        for (String str : userName) {
            list.add(convertToChinese(str));
        }
        return list;
    }

    /**
     * 模糊查询
     *
     * @param name     用户输入的拼音
     * @param userName 需要匹配的名称集合
     */
    public static List<String> fuzzyQuery(String name, List<String> userName) {

        String convertName = convertToChinese(name);

        List<String> convertUserName = numberToChineseCharacters(userName);

        //判断当前字符串的类型
        int state = determineStringType(convertName);

        //判断用户输入的是否是 汉字
        if (state == CHINESE_CHARACTER) {
            return chineseCharactersFuzzySearch(convertName, convertUserName);
        }

        //判断用户输入的是否是 拼音
        if (state == PIN_YIN) {
            return pinyinFuzzySearch(convertName, convertUserName);
        }

        //判断用户输入的 即输入拼音也输入汉字
        if (state == WHOLE) {
            return null;
        }

        return null;
    }


    /**
     * 将中文汉字替换为阿拉伯数字
     * @param input
     * @return
     */
    public static String replaceChineseNumber(String input) {
        String[] chineseNumbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "零"};
        String[] arabicNumbers = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "0"};

        for (int i = 0; i < chineseNumbers.length; i++) {
            input = input.replace(chineseNumbers[i], arabicNumbers[i]);
        }

        return input;
    }

}