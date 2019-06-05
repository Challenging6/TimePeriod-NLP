package com.period;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import com.time.nlp.stringPreHandlingModule;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final String[] REGEX_FILES = new String[] {  //正则文件
            "/period0.txt",
            "/period1.txt",
            "/period2.txt",
            "/period3.txt",
    };

    private List<TimeUnit> times;
    private static List<Pattern> patterns = null;
    private TimeNormalizer timeNormalizer;

    /**
    * @author LinZheng Chai
    * @date 2019/6/5 16:56
    * @param
    * @return
    * @description 构造函数
     */
    public PeriodNormalizer(){
        try {
            patterns =readModel();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/5 16:57
    * @param
    * @return
    * @description 时间段抽取的入口, 返回包含时间段(PeriodUnit)的arrayList
     */
    public List<PeriodUnit> parse(String target) throws URISyntaxException {
        System.out.println("Parsing: "+target);
        List<PeriodUnit> periods = new ArrayList<>();
        times = parseTime(target);

        //需要保持和time包预处理的结果相同，用来定位时间
        target = preHandling(target);


        //先对时间进行抽取， 再对相应位置进行标记(time1, time2...)替换，方便后边正则匹配。
        String maskStr = maskTime(target);
        periodExtract(maskStr, target, periods);
        return periods;
    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 0:13
    * @param
    * @return
    * @description  抽取时间段
     */
    private void periodExtract(String maskStr,
                               String originStr, List<PeriodUnit> periods){

        oneTimeToNowExtract(maskStr, originStr, periods,
                patterns.get(0), patterns.get(3));
        twoTimeExtract(maskStr, periods, patterns.get(1));
        oneTimePastExtract(maskStr, periods, patterns.get(2));

    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 11:20
    * @param
    * @return
    * @description 双时间点类型抽取
     */
    private void twoTimeExtract(String maskStr, List<PeriodUnit> periods, Pattern pattern){
        Matcher matcher;

        matcher = pattern.matcher(maskStr);
        //System.out.println("patterns: "+ pattern);
        //System.out.println("Str: "+ maskStr);
        while (matcher.find()){

            List<String> timeMarks = new ArrayList<>();
            for (int i = 1; i <= matcher.groupCount(); i++){
                //System.out.println(i+" "+matcher.group(i));
                if (matcher.group(i)!=null){
                    timeMarks.add(matcher.group(i));
                }
            }
            if (timeMarks.size()!=1+2){
                System.out.println("时间匹配异常!,匹到的时间个数有错误");
            }else {
                int no1, no2;
                no1 = Integer.valueOf(timeMarks.get(1));
                no2 = Integer.valueOf(timeMarks.get(2));
                TimeUnit time1 = times.get(no1);
                TimeUnit time2 = times.get(no2);
                if (time1 != null && time2 != null) {
                   // System.out.println(time1.Time_Expression);
                   // System.out.println(time2.Time_Expression);
                    PeriodUnit period = new PeriodUnit(time1, time2);
                    //System.out.println(period);
                    periods.add(period);

                    times.set(no1, null);
                    times.set(no2, null);
                }
            }
        }
    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/4 11:26
    * @param
    * @return
    * @description 过去的单时间点, (昨天的, 上个月的)
     */
    private void oneTimePastExtract(String maskStr,
                                    List<PeriodUnit> periods,Pattern pattern){
        Matcher matcher;
        matcher = pattern.matcher(maskStr);
        //System.out.println("patterns: "+ pattern);
        //System.out.println("Str: "+ maskStr);
        while (matcher.find()){

            List<String> timeMarks = new ArrayList<>();
            for (int i = 1; i <= matcher.groupCount(); i++){
                //System.out.println(i+" "+matcher.group(i));
                if (matcher.group(i)!=null){
                    timeMarks.add(matcher.group(i));
                }
            }
            if (timeMarks.size()!=1+1){
                System.out.println("时间匹配异常!,匹到的时间个数有错误");
            }else {
                int no1;
                no1 = Integer.valueOf(timeMarks.get(1));
                TimeUnit time1 = times.get(no1);

                if (time1 != null) {
                    //System.out.println(time1.Time_Norm);

                    PeriodUnit period = new PeriodUnit(
                            time1, timeNormalizer, false
                    );

                    //System.out.println(time1.Time_Expression+": "+period);

                    periods.add(period);
                    times.set(no1, null);
                }
            }
        }
    }

    /**
     * @author LinZheng Chai
     * @date 2019/6/4 11:26
     * @param
     * @return
     * @description 过去的单时间点至今 (最近五月)
     */
    private void oneTimeToNowExtract(String maskStr, String originStr,
                                     List<PeriodUnit> periods,
                                     Pattern pattern1, Pattern pattern2){
        /**
         *先匹配不需要时间抽取的(近5年,近3月, 近一周)
         */

        Matcher matcher;
        matcher = pattern1.matcher(originStr);
        //System.out.println("patterns: "+ pattern1);
        //System.out.println("Str: "+ maskStr);
        while (matcher.find()) {
            List<String> timeMarks = new ArrayList<>();
            for (int i = 1; i <= matcher.groupCount(); i++) {
                //System.out.println(i+" "+matcher.group(i));
                if (matcher.group(i) != null) {
                    timeMarks.add(matcher.group(i));
                }
            }
            if (timeMarks.size() != 1 + 2) {
                System.out.println("匹配异常!");
            } else {
                int timeLen;
                timeLen = Integer.valueOf(timeMarks.get(1));


              PeriodUnit period = new PeriodUnit(
                       timeLen,
                       timeMarks.get(2),
                       timeNormalizer
              );
                periods.add(period);

            }
        }
        /**
         * 匹配需要时间抽取的(去年至今, 4月以来)
         */

        matcher = pattern2.matcher(maskStr);
        //System.out.println("patterns: "+ pattern);
        //System.out.println("Str: "+ maskStr);
        while (matcher.find()){
            List<String> timeMarks = new ArrayList<>();
            for (int i = 1; i <= matcher.groupCount(); i++){
                //System.out.println(i+" "+matcher.group(i));
                if (matcher.group(i)!=null){
                    timeMarks.add(matcher.group(i));
                }
            }
            if (timeMarks.size()!=1+1){
                System.out.println("时间匹配异常! 匹到的时间个数有错误");
            }else {
                int no1;
                no1 = Integer.valueOf(timeMarks.get(1));
                TimeUnit time1 = times.get(no1);
                if (time1 != null) {
                    //System.out.println(time1.Time_Norm);

                    PeriodUnit period = new PeriodUnit(
                            time1, timeNormalizer, true
                    );
                    //System.out.println(time1.Time_Expression+": "+period);
                    periods.add(period);
                    times.set(no1, null);
                }
            }
        }
    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/4 0:03
    * @param
    * @return
    * @description 进行时间抽取
     */
    private List<TimeUnit> parseTime(String target) throws URISyntaxException {
        URL url = TimeNormalizer.class.getResource("/TimeExp.m");
        //System.out.println(url.toURI().toString());
        timeNormalizer = new TimeNormalizer(url.toURI().toString());
        //timeNormalizer.setPreferFuture(true);
        TimeUnit[] temp = timeNormalizer.parse(target);
        return new ArrayList<>(Arrays.asList(temp));
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
        for (int i = 0; i < times.size(); i++){
            time = times.get(i);
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
    private List<Pattern> readModel(){

        List<Pattern> regexArr = new ArrayList<>();

        for (String file : REGEX_FILES){
            regexArr.add(loadRegexFile(file));
        }

        return regexArr;
    }

    /**
    * @author LinZheng Chai 
    * @date 2019/6/4 0:53
    * @param 
    * @return 
    * @description 加载正则表达式文件,并把每行用|连接起来
     */
    private Pattern loadRegexFile(String path){
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
                //System.out.println(lineTxt);
                regexArr.add(lineTxt);

            }
            read.close();
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        String temp = String.join("|", regexArr);
        return Pattern.compile(temp);
    }



}
