package com.period;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import org.junit.Assert;
import org.junit.Ignore;
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
    @Ignore
    public void simpleTest(){
        try {
            URL url = TimeNormalizer.class.getResource("/TimeExp.m");
            PeriodNormalizer periodNormalizer = PeriodNormalizer.getInstance(url.toURI().toString());
            List<PeriodUnit> periods;
            periods = periodNormalizer.parse("今年3月的, 去年的, 昨天 有雷阵雨");
            System.out.println(periods.get(0));
            System.out.println(periods.get(1));
            System.out.println(periods.get(2));
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
    @Ignore
    public void newTest() {
        try {
            URL url = TimeNormalizer.class.getResource("/TimeExp.m");
            PeriodNormalizer periodNormalizer = PeriodNormalizer.getInstance(url.toURI().toString());
            List<PeriodUnit> periods;

            periods = periodNormalizer.parse("2019年1");
            System.out.println(periods.get(0));

            periods = periodNormalizer.parse("6月12");
            System.out.println(periods.get(0));

            // 期望: 2019-01-10 00:00:00 - 2019-06-12 00:00:00 ,
            // 后面的2019年要求解析为当天
            // 结果: 2019-01-01 00:00:00 - 2019-01-10 00:00:00
            periods = periodNormalizer.parse("2019年1月10号到6月");
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

    @Test
    @Ignore
    public void testCTM() {
        try {
            URL url = TimeNormalizer.class.getResource("/TimeExp.m");
            PeriodNormalizer periodNormalizer = PeriodNormalizer.getInstance(url.toURI().toString());
            List<PeriodUnit> periods;

            periods = periodNormalizer.parse("2019年1月10号到6月");
            Assert.assertEquals("2019-01-10 00:00:00 - 2019-06-30 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019年1月10日到2019年11月");
            Assert.assertEquals("2019-01-10 00:00:00 - 2019-11-30 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019年3月5日到2019年5月");
            Assert.assertEquals("2019-03-05 00:00:00 - 2019-05-31 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("3月到5月");
            Assert.assertEquals("2019-03-01 00:00:00 - 2019-05-31 23:59:59", periods.get(0).toString().trim());

            // ！！！ 时间不是23:59:59，倒是不影响功能
            periods = periodNormalizer.parse("3月到7月");
            Assert.assertEquals("2019-03-01 00:00:00 - 2019-07-18 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("3月到8月");
            Assert.assertEquals("2018-03-01 00:00:00 - 2018-08-31 23:59:59", periods.get(0).toString().trim());

            // 解析到当天
            periods = periodNormalizer.parse("今年的");
            Assert.assertEquals("2019-01-01 00:00:00 - 2019-07-18 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("今年的6月");
            Assert.assertEquals("2019-06-01 00:00:00 - 2019-06-30 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("今年的11月");
            Assert.assertEquals("2019-11-01 00:00:00 - 2019-11-30 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("这个月的");
            Assert.assertEquals("2019-07-01 00:00:00 - 2019-07-18 00:00:00", periods.get(0).toString().trim());

            // 现在是2019年7月17日
            periods = periodNormalizer.parse("7月10日");
            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());

            // 未来的时间，解析为上一年
//            periods = periodNormalizer.parse("7月30日");
//            Assert.assertEquals("2018-07-30 00:00:00 - 2018-07-30 23:59:59", periods.get(0).toString().trim());

//            periods = periodNormalizer.parse("20190710");
//            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());
//
//            periods = periodNormalizer.parse("2019.07.10");
//            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());
//
//            periods = periodNormalizer.parse("2019。07。10");
//            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());
//
//            periods = periodNormalizer.parse("2019-07-10");
//            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());
//
//            periods = periodNormalizer.parse("2019/07/10");
//            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testYing() {
        try {
            URL url = TimeNormalizer.class.getResource("/TimeExp.m");

            TimeNormalizer timeNormalizer = TimeNormalizer.getInstance(url.toURI().toString(), false);

            TimeUnit[] parse = timeNormalizer.parse("2日");
            Assert.assertEquals("2日 ---> 2019年7月2日", parse[0].toString());

            parse = timeNormalizer.parse("20日");
            Assert.assertEquals("20日 ---> 2019年6月20日", parse[0].toString());

            parse = timeNormalizer.parse("19日");
            Assert.assertEquals("19日 ---> 2019年6月19日", parse[0].toString());

            parse = timeNormalizer.parse("6月");
            Assert.assertEquals("6月 ---> 2019年6月", parse[0].toString());

            parse = timeNormalizer.parse("8月");
            Assert.assertEquals("8月 ---> 2018年8月", parse[0].toString());

            parse = timeNormalizer.parse("7月");
            Assert.assertEquals("7月 ---> 2018年7月", parse[0].toString());

            parse = timeNormalizer.parse("7月2日");
            Assert.assertEquals("7月2日 ---> 2019年7月2日", parse[0].toString());

            parse = timeNormalizer.parse("7月19日");
            Assert.assertEquals("7月19日 ---> 2018年7月19日", parse[0].toString());

            parse = timeNormalizer.parse("7月20日");
            Assert.assertEquals("7月20日 ---> 2018年7月20日", parse[0].toString());

            parse = timeNormalizer.parse("去年的");
            Assert.assertEquals("去年 ---> 2018年", parse[0].toString());

            parse = timeNormalizer.parse("上一年");
            Assert.assertEquals("上一年 ---> 2018年", parse[0].toString());

            parse = timeNormalizer.parse("前年");
            Assert.assertEquals("前年 ---> 2017年", parse[0].toString());

            parse = timeNormalizer.parse("上上一年");
            Assert.assertEquals("上上一年 ---> 2017年", parse[0].toString());

            parse = timeNormalizer.parse("大前年");
            Assert.assertEquals("大前年 ---> 2016年", parse[0].toString());

            parse = timeNormalizer.parse("昨天");
            Assert.assertEquals("昨天 ---> 2019年7月18日", parse[0].toString());

            parse = timeNormalizer.parse("昨日");
            Assert.assertEquals("昨日 ---> 2019年7月18日", parse[0].toString());

            parse = timeNormalizer.parse("前天");
            Assert.assertEquals("前天 ---> 2019年7月17日", parse[0].toString());

            parse = timeNormalizer.parse("大前天");
            Assert.assertEquals("大前天 ---> 2019年7月16日", parse[0].toString());

            parse = timeNormalizer.parse("上上日");
            Assert.assertEquals("上上日 ---> 2019年7月17日", parse[0].toString());

            parse = timeNormalizer.parse("上一月");
            Assert.assertEquals("上一月 ---> 2019年6月", parse[0].toString());

            parse = timeNormalizer.parse("上上个月");
            Assert.assertEquals("上上个月 ---> 2019年5月", parse[0].toString());

            parse = timeNormalizer.parse("上上月");
            Assert.assertEquals("上上月 ---> 2019年5月", parse[0].toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
