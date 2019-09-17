package com.period;

import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;

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
    public void testCTM() {
        try {
            URL url = TimeNormalizer.class.getResource("/TimeExp.m");
            PeriodNormalizer periodNormalizer = PeriodNormalizer.getInstance(url.toURI().toString());
            List<PeriodUnit> periods;

            // 当天为2019年9月16号
            int year = 2019;
            int month = 9;
            int day = 17;
            int yearMinus1 = year - 1;
            int yearMinus2 = year - 2;
            int yearMinus3 = year - 3;
            int yearPlus1 = year + 1;
            int monthMinus1 = month - 1;
            int monthMinus2 = month - 2;
            int monthPlus1 = month + 1;
            int dayMinus1 = day - 1;
            int dayMinus2 = day - 2;
            int dayMinus3 = day - 3;
            int dayPlus1 = day + 1;

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
            Assert.assertEquals("2019-03-01 00:00:00 - 2019-07-31 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("3月到8月");
            Assert.assertEquals("2019-03-01 00:00:00 - 2019-08-31 23:59:59", periods.get(0).toString().trim());

            // 解析到当天
            periods = periodNormalizer.parse("今年的");
            Assert.assertEquals("2019-01-01 00:00:00 - 2019-0" + month + "-" + day + " 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("今年的6月");
            Assert.assertEquals("2019-06-01 00:00:00 - 2019-06-30 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("今年的11月");
            Assert.assertEquals("2019-11-01 00:00:00 - 2019-11-30 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("这个月的");
            Assert.assertEquals("2019-0" + month + "-01 00:00:00 - 2019-0" + month + "-" + day + " 00:00:00", periods.get(0).toString().trim());

            // 现在是2019年7月17日
            periods = periodNormalizer.parse("7月10日");
            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());

            // 未来的时间，解析为上一年
            periods = periodNormalizer.parse(monthPlus1 + "月" + dayPlus1 + "日");
            Assert.assertEquals("2018-" + monthPlus1 + "-" + dayPlus1 + " 00:00:00 - 2018-" + monthPlus1 + "-" + dayPlus1 + " 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("20190710");
            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019.07.10");
            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019。07。10");
            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019-07-10");
            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019/07/10");
            Assert.assertEquals("2019-07-10 00:00:00 - 2019-07-10 23:59:59", periods.get(0).toString().trim());

            //期望 最近半年
//            periods = periodNormalizer.parse("最近半年的");//IndexOutOfBoundsException
//            Assert.assertEquals("2019-01-23 00:00:00 - 2019-7-25 23:59:59", periods.get(0).toString().trim());

            //期望 近半年
//            periods = periodNormalizer.parse("最近6个月"); //IndexOutOfBoundsException
//            Assert.assertEquals("2019-01-25 00:00:00 - 2019-07-25 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("最近一年");
            Assert.assertEquals("2018-0" + month+ "-" + day + " 00:00:00 - 2019-0"  + month+ "-" + day + " 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2017年");
            Assert.assertEquals("2017-01-01 00:00:00 - 2017-12-31 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2018年1月1日至2018年12月31日");
            Assert.assertEquals("2018-01-01 00:00:00 - 2018-12-31 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2018年");
            Assert.assertEquals("2018-01-01 00:00:00 - 2018-12-31 00:00:00", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("今天");
            Assert.assertEquals("2019-0"  + month+ "-" + day +  " 00:00:00 - 2019-0" + month+ "-" + day + " 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("昨天");
            Assert.assertEquals("2019-0"  + month+ "-" + dayMinus1 +  " 00:00:00 - 2019-0" + month+ "-" + dayMinus1 + " 23:59:59", periods.get(0).toString().trim());

//            //期望 明天
//            periods = periodNormalizer.parse("明天");//明天2019-06-24 00:00:00 - 2019-06-24 23:59:59
//            Assert.assertEquals("2019-07-24 00:00:00 - 2019-07-24 23:59:59", periods.get(0).toString().trim());
//
//            //期望 大后天
//            periods = periodNormalizer.parse("大后天");//2019-06-26 00:00:00 - 2019-06-26 23:59:59
//            Assert.assertEquals("2019-07-26 00:00:00 - 2019-07-26 23:59:59", periods.get(0).toString().trim());
//
//            //期望后天
//            periods = periodNormalizer.parse("后天");//2019-06-26 00:00:00 - 2019-06-26 23:59:59
//            Assert.assertEquals("2019-07-25 00:00:00 - 2019-07-25 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("大前天");
            Assert.assertEquals("2019-0"  + month+ "-" + dayMinus3 +  " 00:00:00 - 2019-0" + month+ "-" + dayMinus3 + " 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019，7月17");//Actual   :2019-07-01 00:00:00 - 2019-07-23 00:00:00
            Assert.assertEquals("2019-07-17 00:00:00 - 2019-07-17 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("19年7月17");//Actual   :2019-07-01 00:00:00 - 2019-07-23 00:00:00
            Assert.assertEquals("2019-07-17 00:00:00 - 2019-07-17 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019年7月17");//Actual   :2019-07-01 00:00:00 - 2019-07-23 00:00:00
            Assert.assertEquals("2019-07-17 00:00:00 - 2019-07-17 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019年11月17");//Actual   :2019-07-01 00:00:00 - 2019-07-23 00:00:00
            Assert.assertEquals("2019-11-17 00:00:00 - 2019-11-17 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2018年11月17");//Actual   :2019-07-01 00:00:00 - 2019-07-23 00:00:00
            Assert.assertEquals("2018-11-17 00:00:00 - 2018-11-17 23:59:59", periods.get(0).toString().trim());

            periods = periodNormalizer.parse("2019年07月17");//Actual   :2019-07-01 00:00:00 - 2019-07-23 00:00:00
            Assert.assertEquals("2019-07-17 00:00:00 - 2019-07-17 23:59:59", periods.get(0).toString().trim());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testYing() {
        try {
            URL url = TimeNormalizer.class.getResource("/TimeExp.m");

            TimeNormalizer timeNormalizer = TimeNormalizer.getInstance(url.toURI().toString(), false);

            // 当天为2019年8月16号
            int year = 2019;
            int month = 8;
            int day = 19;
            int yearMinus1 = year - 1;
            int yearMinus2 = year - 2;
            int yearMinus3 = year - 3;
            int yearPlus1 = year + 1;
            int monthMinus1 = month - 1;
            int monthMinus2 = month - 2;
            int monthPlus1 = month + 1;
            int dayMinus1 = day - 1;
            int dayMinus2 = day - 2;
            int dayMinus3 = day - 3;
            int dayPlus1 = day + 1;

            // 未来日 -> 上个月的该日
            TimeUnit[] parse = timeNormalizer.parse(dayPlus1 + "日");
            Assert.assertEquals(dayPlus1 + "日 ---> " + year + "年" + monthMinus1 + "月" + dayPlus1 + "日", parse[0].toString());

            // 当日 -> 上个月的该日
            parse = timeNormalizer.parse(day + "日");
            Assert.assertEquals(day + "日 ---> " + year + "年" + monthMinus1 + "月" + day + "日", parse[0].toString());

            // 过去日 -> 当月的过去日
            parse = timeNormalizer.parse(dayMinus1 + "日");
            Assert.assertEquals(dayMinus1 + "日 ---> " + year + "年" + month + "月" + dayMinus1 + "日", parse[0].toString());

            // 未来月 -> 去年的该月
            parse = timeNormalizer.parse(monthPlus1 + "月");
            Assert.assertEquals(monthPlus1 + "月 ---> " + yearMinus1 + "年" + monthPlus1 + "月", parse[0].toString());

            // 当月 -> 去年的该月
            parse = timeNormalizer.parse(month + "月");
            Assert.assertEquals(month + "月 ---> " + yearMinus1 + "年" + month + "月", parse[0].toString());

            // 过去月 -> 当前年的该月
            parse = timeNormalizer.parse(monthMinus1 + "月");
            Assert.assertEquals(monthMinus1 + "月 ---> " + year + "年" + monthMinus1 + "月", parse[0].toString());

            // 未来月日 -> 上一年的该月日
            parse = timeNormalizer.parse(monthPlus1 + "月" + dayPlus1 + "日");
            Assert.assertEquals(monthPlus1 + "月" + dayPlus1 + "日 ---> " + yearMinus1 + "年" + monthPlus1 + "月" + dayPlus1 + "日", parse[0].toString());

            // 当前月日 -> 上一年的该月日
            parse = timeNormalizer.parse(month + "月" + day + "日");
            Assert.assertEquals(month + "月" + day + "日 ---> " + yearMinus1 + "年" + month + "月" + day + "日", parse[0].toString());

            // 过去的月日 -> 当前年的该月日
            parse = timeNormalizer.parse(monthMinus1 + "月" + dayMinus1 + "日");
            Assert.assertEquals(monthMinus1 + "月" + dayMinus1 + "日 ---> " + year + "年" + monthMinus1 + "月" + dayMinus1 + "日", parse[0].toString());

            parse = timeNormalizer.parse("去年的");
            Assert.assertEquals("去年 ---> " + yearMinus1 + "年", parse[0].toString());

            parse = timeNormalizer.parse("上一年");
            Assert.assertEquals("上一年 ---> " + yearMinus1 + "年", parse[0].toString());
            parse = timeNormalizer.parse("前年");
            Assert.assertEquals("前年 ---> " + yearMinus2 + "年", parse[0].toString());

            parse = timeNormalizer.parse("上上一年");
            Assert.assertEquals("上上一年 ---> " + yearMinus2 + "年", parse[0].toString());

            parse = timeNormalizer.parse("大前年");
            Assert.assertEquals("大前年 ---> " + yearMinus3 + "年", parse[0].toString());

            parse = timeNormalizer.parse("昨天");
            Assert.assertEquals("昨天 ---> " + year + "年" + month + "月" + dayMinus1 + "日", parse[0].toString());

            parse = timeNormalizer.parse("昨日");
            Assert.assertEquals("昨日 ---> " + year + "年" + month + "月" + dayMinus1 + "日", parse[0].toString());

            parse = timeNormalizer.parse("前天");
            Assert.assertEquals("前天 ---> " + year + "年" + month + "月" + dayMinus2 + "日", parse[0].toString());

            parse = timeNormalizer.parse("大前天");
            Assert.assertEquals("大前天 ---> " + year + "年" + month + "月" + dayMinus3 + "日", parse[0].toString());

            parse = timeNormalizer.parse("上上日");
            Assert.assertEquals("上上日 ---> " + year + "年" + month + "月" + dayMinus2 + "日", parse[0].toString());

            parse = timeNormalizer.parse("上一月");
            Assert.assertEquals("上一月 ---> " + year + "年" + monthMinus1 + "月", parse[0].toString());

            parse = timeNormalizer.parse("上上个月");
            Assert.assertEquals("上上个月 ---> " +year + "年" + monthMinus2 + "月", parse[0].toString());

            parse = timeNormalizer.parse("上上月");
            Assert.assertEquals("上上月 ---> " + year + "年" + monthMinus2 + "月", parse[0].toString());

            parse = timeNormalizer.parse("去年底");
            Assert.assertEquals("去年底 ---> " + yearMinus1 + "年" + "12月", parse[0].toString());

            parse = timeNormalizer.parse("去年3月");
            Assert.assertEquals("去年3月 ---> " + yearMinus1 + "年" + "3月", parse[0].toString());

            parse = timeNormalizer.parse("上一年3月");
            Assert.assertEquals("上一年3月 ---> " + yearMinus1 + "年" + "3月", parse[0].toString());

            parse = timeNormalizer.parse("前年3月");
            Assert.assertEquals("前年3月 ---> " + yearMinus2 + "年" + "3月", parse[0].toString());

            parse = timeNormalizer.parse("上上一年3月");
            Assert.assertEquals("上上一年3月 ---> " + yearMinus2 + "年" + "3月", parse[0].toString());

            parse = timeNormalizer.parse("大前年3月");
            Assert.assertEquals("大前年3月 ---> " + yearMinus3 + "年" + "3月", parse[0].toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
