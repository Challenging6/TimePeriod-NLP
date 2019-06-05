package com.period;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import com.time.nlp.stringPreHandlingModule;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: Time-NLP
 * @description
 * @author: ChaiLinZheng
 * @create: 2019-06-03 21:55
 **/
public class PeriodNormalizer {

    private static final String REGEX_FILES = "/PeriodRegex.txt";  //正则文件
    private static Map<String, Pattern> PATTERNS = null;
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
            PATTERNS =readModel(REGEX_FILES); //加载正则
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
        List<TimeUnit> times;
        List<PeriodUnit> periods = new ArrayList<>();

        System.out.println("Parsing: "+target);
        times = parseTime(target);

        //需要保持和time包预处理的结果相同，用来定位时间
        target = preHandling(target);

        //先对时间进行抽取， 再对相应位置进行标记(time1, time2...)替换，方便后边正则匹配。
        String maskStr = maskTime(target, times);
        periodExtract(maskStr, target, periods, times);
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
                               String originStr,
                               List<PeriodUnit> periods,
                               List<TimeUnit> times){

        //抽取函数, 可以自己定义相应规则和抽取块
        maskStr = oneTimeToNowExtract(maskStr, originStr, periods, times);
        maskStr = twoTimeExtract(maskStr, periods, times);
        maskStr = oneTimePointExtract(maskStr, periods, times);

    }

    /**
     * @author LinZheng Chai
     * @date 2019/6/4 11:26
     * @param
     * @return
     * @description 过去的单时间点至今 (最近五月)
     */
    private String oneTimeToNowExtract(String maskStr, String originStr,
                                     List<PeriodUnit> periods,List<TimeUnit> times){
        /**
         *先匹配不需要时间抽取的(近5年,近3月, 近一周)
         */
        Pattern pattern = PATTERNS.get("oneTimeToNow1");
        Matcher matcher;
        matcher = pattern.matcher(originStr);
        while (matcher.find()) {
            List<String> timeMarks = new ArrayList<>();
            for (int i = 1; i <= matcher.groupCount(); i++) {
                if (matcher.group(i) != null) {
                    timeMarks.add(matcher.group(i));
                }
            }
            if (timeMarks.size() != 1 + 2) {
                System.out.println("匹配异常!");
            } else {
                int timeLen;
                timeLen = Integer.valueOf(timeMarks.get(1)); //时间长度

                PeriodUnit period = new PeriodUnit(
                        timeLen,
                        timeMarks.get(2),  //时间粒度
                        timeNormalizer
                );
                periods.add(period);
                maskStr = matcher.replaceFirst("近xxx");
            }
        }
        /**
         * 匹配需要时间抽取的(去年至今, 4月以来)
         */
        pattern = PATTERNS.get("oneTimeToNow2");
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
                System.out.println("时间匹配异常! 匹到的时间个数有错误");
            }else {
                int no1;
                no1 = Integer.valueOf(timeMarks.get(1));
                TimeUnit time = times.get(no1);
                if (time != null) {
                    //System.out.println(time1.Time_Norm);

                    PeriodUnit period = new PeriodUnit(
                            time, timeNormalizer, true
                    );
                    //System.out.println(time1.Time_Expression+": "+period);
                    periods.add(period);
                    times.set(no1, null);
                }
            }
        }
        return maskStr;
    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/4 11:20
    * @param
    * @return
    * @description 双时间点类型抽取
     */
    private String twoTimeExtract(String maskStr, List<PeriodUnit> periods, List<TimeUnit> times){
        Matcher matcher;
        Pattern pattern = PATTERNS.get("twoTime");
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
                TimeUnit timeStart = times.get(no1);
                TimeUnit timeEnd = times.get(no2);
                if (timeStart != null && timeEnd != null) {

                    PeriodUnit period = new PeriodUnit(timeStart, timeEnd);
                    //System.out.println(period);
                    periods.add(period);

                    times.set(no1, null);
                    times.set(no2, null);
                }
            }
        }
        return maskStr;
    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/4 11:26
    * @param
    * @return
    * @description 过去的单时间点, (昨天的, 上个月的)
     */
    private String oneTimePointExtract(String maskStr, List<PeriodUnit> periods, List<TimeUnit> times){
        Matcher matcher;
        Pattern pattern = PATTERNS.get("oneTimePoint");
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
                TimeUnit time = times.get(no1);

                if (time != null) {
                    //System.out.println(time1.Time_Norm);

                    PeriodUnit period = new PeriodUnit(
                            time, timeNormalizer, false
                    );

                    //System.out.println(time1.Time_Expression+": "+period);

                    periods.add(period);
                    times.set(no1, null);
                }
            }
        }
        return maskStr;
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
    private String maskTime(String target,List<TimeUnit> times){
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

            maskStr = maskStr.substring(0, timeStart)+
                    "time"+i+maskStr.substring(timeEnd+1);
            //System.out.println(maskStr);
        }
        return maskStr;
    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 0:26
    * @param
    * @return
    * @description 读取正则表达式, 每一组表达式保存在map中
     */
    private Map<String, Pattern> readModel(String path){
        Map<String, Pattern> regexMap = new HashMap<>();
        List<String> tempRegex;
        try {
            String encoding = "UTF-8";
            InputStream in = getClass().getResourceAsStream(path);
            InputStreamReader read = new InputStreamReader(in, encoding);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            Pattern pattern = null;

            while ((lineTxt = bufferedReader.readLine()) != null) {
                //System.out.println(lineTxt);
                if (lineTxt.trim().length() == 0) continue;
                if (lineTxt.substring(0, 2).equals("//")) continue; //跳过注释
                if (lineTxt.substring(0, 4).equals("####")){
                    String regexName = lineTxt.substring(4);
                    tempRegex = new ArrayList<>();
                    while ((lineTxt = bufferedReader.readLine()) != null){
                        if (lineTxt.trim().length() == 0) continue;

                        if (lineTxt.substring(0, 4).equals("####")){
                            pattern =Pattern.compile(String.join("|", tempRegex));
                            regexMap.put(regexName,pattern);
                            break;
                        }
                        else{
                            tempRegex.add(lineTxt.trim());
                        }

                    }
                }

            }
            read.close();
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }
        return regexMap;
    }



}
