package com.period;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import com.time.nlp.stringPreHandlingModule;

import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;

/**
 * @program: Time-NLP
 * @description
 * @author: ChaiLinZheng
 * @create: 2019-06-03 21:55
 **/
public class PeriodNormalizer {


    private PeriodUnit[] periods;
    private TimeUnit[] times;

    public PeriodNormalizer(){

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


}
