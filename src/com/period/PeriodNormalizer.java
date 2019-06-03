package com.period;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import com.time.nlp.stringPreHandlingModule;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: Time-NLP
 * @description
 * @author: ChaiLinZheng
 * @create: 2019-06-03 21:55
 **/
public class PeriodNormalizer {


    private PeriodUnit[] periods;
    private TimeUnit[] times;
    private static Pattern patterns = null;

    public PeriodNormalizer(){
        try {
            patterns = readModel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void parse(String target) throws URISyntaxException {

        times = parseTime(target);

        //需要保持和time包预处理的结果相同，用来定位时间
        target = preHandling(target);

        //先对时间进行抽取， 再对相应位置进行替换，方便后边正则匹配。
        String maskStr = maskTime(target);
        periodExtract(maskStr);


    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 0:13
    * @param
    * @return
    * @description  抽取时间段
     */
    private void periodExtract(String maskStr){
        Matcher matcher;
        int startLine = -1, endLine = -1;
        matcher = patterns.matcher(maskStr);
        System.out.println("patterns: "+ patterns);
        System.out.println("Str: "+ maskStr);
        while (matcher.find()){
            startLine = matcher.start();
            System.out.println(matcher.group());
            
        }
    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/4 0:03
    * @param
    * @return
    * @description 进行时间抽取
     */
    private TimeUnit[] parseTime(String target) throws URISyntaxException {
        URL url = TimeNormalizer.class.getResource("/TimeExp.m");
        System.out.println(url.toURI().toString());
        TimeNormalizer timeNormalizer = new TimeNormalizer(url.toURI().toString());
        //timeNormalizer.setPreferFuture(true);
        return timeNormalizer.parse(target);
    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 0:03
    * @param
    * @return
    * @description 待匹配字符串的清理空白符和语气助词以及大写数字转化的预处理
     */
    private String preHandling(String str) {
        str = stringPreHandlingModule.delKeyword(str, "\\s+"); // 清理空白符
        str = stringPreHandlingModule.delKeyword(str, "[的]+"); // 清理语气助词
        str = stringPreHandlingModule.numberTranslator(str);// 大写数字转化
        // TODO 处理大小写标点符号
        return str;
    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/4 0:02
    * @param
    * @return
    * @description 对字符串中的时间进行掩盖, 方便后边正则匹配, 将时间替换为time1, time2....
     */
    private String maskTime(String target){
        TimeUnit time;
        String timeExp;
        int timeStart;
        int timeEnd;
        String maskStr = target;
        for (int i = 1; i <= times.length; i++){
            time = times[i-1];
            timeExp = time.Time_Expression;
            timeStart = maskStr.indexOf(timeExp);
            timeEnd = timeStart+timeExp.length()-1;

            maskStr = maskStr.substring(0, timeStart)+"time"+i+maskStr.substring(timeEnd+1);
            //System.out.println(maskStr);
        }
        return maskStr;
    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 0:26
    * @param
    * @return
    * @description 读取正则模型, 返回所有正则表达式的拼接
     */
    private Pattern readModel(){
        String[] files = new String[] {
                "/period1.txt",
                "/period2.txt",
        };
        List<String> regexArr = new ArrayList<>();

        for (String file : files){
            regexArr.add(loadRegexFile(file));
        }

        String allRegex = String.join("|", regexArr);
        return Pattern.compile(allRegex);
    }

    /**
    * @author LinZheng Chai 
    * @date 2019/6/4 0:53
    * @param 
    * @return 
    * @description 加载正则表达式文件,并把每行用|连接起来
     */
    public String loadRegexFile(String path){
        List<String> regexArr = new ArrayList<>();
        try {
            //编码格式
            String encoding = "UTF-8";
            //文件路径
            InputStream in = getClass().getResourceAsStream(path);
            //输入流
            InputStreamReader read = new InputStreamReader(in, encoding);// 考虑到编码格
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            //读取一行
            while ((lineTxt = bufferedReader.readLine()) != null) {
                //正则表达式
                System.out.println(lineTxt);
                regexArr.add(lineTxt);

            }
            read.close();
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return String.join("|", regexArr);
    }

    public static void main(String[] args){
        try {
            //编码格式
            String encoding = "UTF-8";
            //文件路径

            File file = new File("C:\\Users\\Challenging\\Desktop\\NLPLearning\\Time-NLP\\resource\\period1.txt");
            //输入流
            InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            //读取一行
            while ((lineTxt = bufferedReader.readLine()) != null) {
                //正则表达式
                System.out.println(lineTxt);
            }
            read.close();
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
    }

}
