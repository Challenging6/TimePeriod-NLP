package com.period;

import org.junit.Test;

/**
 * @program: Time-NLP
 * @description
 * @author: ChaiLinZheng
 * @create: 2019-06-03 23:19
 **/
public class TimePeriodAnalyseTest {

    /*
    今天的。/昨天的。/前天的。
    2019年1月2日到4月10日。
    近1年的。/近5个月的。/最近一周的。
    上个月5号到昨天的。/去年12月的 / 今年以来的
    上月的。/这个月的
     */

    @Test
    public void simpleTest(){
        try {
            PeriodNormalizer periodNormalizer = new PeriodNormalizer();
            periodNormalizer.parse("周六3点到5点");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
