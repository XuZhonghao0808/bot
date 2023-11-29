package com.xzh.config;

import com.baidu.aip.ocr.AipOcr;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Pattern;

@Component
public class BaiduOCRConfig {
    //设置APPID/AK/SK
    @Value("${baidu.appid}")
    public static final String APP_ID = "";
    @Value("${baidu.apikey}")
    public static final String API_KEY = "";
    @Value("${baidu.secretkey}")
    public static final String SECRET_KEY = "";
    public static AipOcr client;
    public static HashMap<String, String> options;

    public BaiduOCRConfig() {
        // 初始化一个AipOcr
        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
        //        client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
        //        client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理
        client = new AipOcr(APP_ID, API_KEY, SECRET_KEY);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);
        // 可选：设置log4j日志输出格式，若不设置，则使用默认配置
        // 也可以直接通过jvm启动参数设置此环境变量
        //        System.setProperty("aip.log4j.conf", "path/to/your/log4j.properties");
        // 传入可选参数调用接口
        options = new HashMap<String, String>();
        options.put("language_type", "CHN_ENG");//识别语言类型，默认为CHN_ENG。
        options.put("detect_direction", "false");//是否检测图像朝向，默认不检测，即：false。朝向是指输入图像是正常方向、逆时针旋转90/180/270度
        options.put("detect_language", "false");//是否检测语言，默认不检测。当前支持（中文、英语、日语、韩语）
        options.put("probability", "false");//是否返回识别结果中每一行的置信度
    }

    public static String basicGeneral(String image) {
        //        String image = "C:\\Users\\xiaoxu\\Desktop\\aaa.jpg";
        JSONObject res = client.basicGeneral(image, options);
        return res.toString();
    }

    public static String basicAccurateGeneral(String image) {
        //        String image = "C:\\Users\\xiaoxu\\Desktop\\aaa.jpg";
        JSONObject res = client.basicAccurateGeneral(image, options);
        return res.toString();
    }

    public static String basicGeneralUrl(String url) {
        //        String image = "C:\\Users\\xiaoxu\\Desktop\\aaa.jpg";
        JSONObject res = client.basicGeneralUrl(url, options);
        return res.toString(2);
    }

    public static String accurateBasicGeneralUrl(String url) {
        //        String image = "C:\\Users\\xiaoxu\\Desktop\\aaa.jpg";
        byte[] image = getImage(url);
        if(image == null){
            return null;
        }
        JSONObject res = client.basicAccurateGeneral(image, options);
        return res.toString(2);
    }

    private static byte[] getImage(String urlStr){
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // 获取图片输入流
            InputStream inputStream = connection.getInputStream();

            // 将图片输入流转换为字节数组
            byte[] bytes = toByteArray(inputStream);

            // 关闭连接和流
            inputStream.close();
            connection.disconnect();

            // 输出字节数组长度
            System.out.println("Byte array length: " + bytes.length);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 将输入流转换为字节数组的方法
    public static byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }

        return outputStream.toByteArray();
    }

    //首字母大写
    public static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;
        return String.valueOf(cs);

    }

    private static final char[] BASE62_ST = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    /**
     * Base62编码。
     */
    public static String base62(int src) {
        String rst = "";
        boolean negative = src < 0;
        if (negative) src = -src;
        while (true) {
            int a = ~~src % 62;
            rst = BASE62_ST[a] + rst;
            src = ~~(src / 62);
            if (src <= 0) {
                break;
            }
        }
        return negative ? "-" + rst : rst;
    }


    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

}
