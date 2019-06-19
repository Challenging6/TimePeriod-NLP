package com.period;

import com.time.nlp.TimeNormalizer;
import org.junit.Test;

import java.net.URL;
import java.util.List;

/**
 * @program: Time Period 基本功能测试
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

    /*
    1.两个时间点明确的
        // xx到xx, xx-xx, xx至xx

    2.单时间点的, 表示那一天, 周, 月, 年, 季度的
        //今天的。/昨天的。/前天的, 去年12月的, 上月的, 这个月的, 上个季度的

    3.单时间点的, 表示那一时间点到至今的.
        //近一年的, 近五个月的, 最近一周的, 今年以来的,
     */

    @Test
    public void simpleTest(){
        try {
            URL url = TimeNormalizer.class.getResource("/TimeExp.m");
            PeriodNormalizer periodNormalizer = PeriodNormalizer.getInstance(url.toURI().toString());
            List<PeriodUnit> periods;
            periods = periodNormalizer.parse("今年3月的, 去年的, 去年12月, 昨天 有雷阵雨");
            System.out.println(periods.get(0));
            System.out.println(periods.get(1));
            System.out.println(periods.get(2));
            System.out.println(periods.get(3));
            periods = periodNormalizer.parse("6月1号-5月3号");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("6月1号和5月3号之间");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("今天的有雷阵雨");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("昨天的有雷阵雨");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("前天的有雷阵雨");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("近两年的");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("近五个月的有雷阵雨");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("近三月的有雷阵雨, 昨天没有");
            System.out.println(periods.get(0));
            System.out.println(periods.get(1));
            periods = periodNormalizer.parse("近3天的有雷阵雨");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("近一年的有雷阵雨");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("最近一周的有雷阵雨");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("上个月5号到昨天的");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("去年12月的");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("今年以来的");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("上月的");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("这个月的");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("过去3个月的");
            System.out.println(periods.get(0));
            periods = periodNormalizer.parse("过去一周");
            System.out.println(periods.get(0));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void newTest() {
        try {
            URL url = TimeNormalizer.class.getResource("/TimeExp.m");
            PeriodNormalizer periodNormalizer = PeriodNormalizer.getInstance(url.toURI().toString());
            List<PeriodUnit> periods;

            // 期望: 2019-01-10 00:00:00 - 2019-06-12 00:00:00 , 后面的2019年要求解析为当天
            // 结果: 2019-01-01 00:00:00 - 2019-01-10 00:00:00
            periods = periodNormalizer.parse("2019年2");
            System.out.println(periods.get(0));

            periods = periodNormalizer.parse("2019年1月10日到2019年11月");
            System.out.println(periods.get(0));

            // 期望：2019-03-05 00:00:00 - 2019-05-31 00:00:00，解析成月末
            // 结果：2019-03-05 00:00:00 - 2019-05-01 00:00:00
            periods = periodNormalizer.parse("2019年3月5日到2019年5月");
            System.out.println(periods.get(0));

            // 期望：2019-03-01 00:00:00 - 2019-05-31 00:00:00, 应该是2019年的，5月到月底。
            // 结果：2020-03-01 00:00:00 - 2020-05-01 00:00:00
            periods = periodNormalizer.parse("3月到5月");
            System.out.println(periods.get(0));



            // 期望：2019-03-01 00:00:00 - 2019-06-12 00:00:00, 应该是2019年的，6月到当天。
            // 结果：2020-03-01 00:00:00 - 2020-06-01 00:00:00
            periods = periodNormalizer.parse("3月到6月");
            System.out.println(periods.get(0));

            periods = periodNormalizer.parse("3月到7月");
            System.out.println(periods.get(0));

            // 期望：2019-01-01 00:00:00 - 2019-06-12 00:00:00 年初到当天
            // 结果：2019-01-01 00:00:00 - 2019-12-31 00:00:00
            periods = periodNormalizer.parse("今年的");
            System.out.println(periods.get(0));

            periods = periodNormalizer.parse("今年的11月");
            System.out.println(periods.get(0));


            // 期望：2019-06-01 00:00:00 - 2019-06-12 这个月初到当天
            // 结果：2019-06-01 00:00:00 - 2019-06-30 00:00:00
            periods = periodNormalizer.parse("这个月的");
            System.out.println(periods.get(0));



        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
