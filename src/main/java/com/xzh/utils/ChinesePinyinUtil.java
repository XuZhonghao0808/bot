package com.xzh.utils;


import net.duguying.pinyin.Pinyin;
import net.duguying.pinyin.PinyinException;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.HashMap;


public class ChinesePinyinUtil {

    /**
     * 得到 汉字的全拼
     * @param src 中文字符串
     * @return
     */
    public static String getPingYin(String src) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        StringBuilder sb = new StringBuilder();
        char[] srcArray = src.toCharArray();
        try {
            for (int i = 0; i < srcArray.length; i++) {
                // 判断是否为汉字字符
                if (java.lang.Character.toString(srcArray[i]).matches("[\\u4E00-\\u9FA5]+")) {
                    String[] targetArray = PinyinHelper.toHanyuPinyinStringArray(srcArray[i], format);
                    sb.append(targetArray[0]);
                } else {
                    sb.append(java.lang.Character.toString(srcArray[i]));
                }
            }
            return sb.toString();
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            e1.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * 得到中文字符串第一个汉字首字母 多音字返回多个
     * @param str 中文字符串
     * @return
     */
    public static Object getPinYinFirstMapChar(String str) {
        char word = str.charAt(0);
        String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
        if(pinyinArray.length > 1){
            HashMap<String, String> map = new HashMap<>();
            for(int a = 0; a < pinyinArray.length; a++){
                map.put(a + 1 + "", pinyinArray[a].charAt(0) + "");
            }
            return map;
        }
        return pinyinArray[0].charAt(0);
    }

    /**
     * 得到中文字符串第一个汉字首字母
     * @param str 中文字符串
     * @return
     */
    public static String getPinYinFirstChar(String str) {
        char word = str.charAt(0);
        String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
        return pinyinArray[0].charAt(0) + "";
    }

    /**
     * 得到中文首字母,例如"专科"得到zk返回
     * @param str 中文字符串
     * @return
     */
    public static String getPinYinHeadChar(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char word = str.charAt(i);
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
            if (pinyinArray != null) {
                sb.append(pinyinArray[0].charAt(0));
            } else {
                sb.append(word);
            }
        }
        return sb.toString();
    }

    /**
     * 将字符串转移为ASCII码
     * @param cnStr  中文字符串
     * @return
     */
    public static String getCnASCII(String cnStr) {
        StringBuilder sb = new StringBuilder();
        byte[] bGBK = cnStr.getBytes();
        for (int i = 0; i < bGBK.length; i++) {
            sb.append(Integer.toHexString(bGBK[i] & 0xff));
        }
        return sb.toString();
    }

    /**
     *  匹配汉字字符串第一个汉字首字母
     * @param str
     * @return
     */
    public static String getPinYinFirst(String str){
        try {
            Pinyin pinyin = new Pinyin();
            String s4 = pinyin.translateNoMark(str);
            return s4.charAt(0) + "";
        } catch (PinyinException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
//        String cnStr = "重庆,重视昭君发展(专科)环-境喵邈";
//        System.out.println(getPingYin(cnStr));
//        System.out.println(getPinYinHeadChar(cnStr));
//        System.out.println(getCnASCII("专科"));
//        System.out.println(getPinYinFirstChar("重庆"));
//        HashMap<String, String> map = (HashMap<String, String>)getPinYinFirstMapChar("重庆");
//        System.out.println(map);
        System.out.println(getPinYinFirst("长沙"));
        System.out.println(getPinYinFirst("行李"));
        System.out.println(getPinYinFirst("重庆"));

    }
}
