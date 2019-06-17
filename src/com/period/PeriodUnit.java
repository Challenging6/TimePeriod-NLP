package com.period;

import com.time.enums.TimeAcc;
import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import com.time.util.DateUtil;
import java.util.Calendar;
import java.util.Date;

/**
 * @program: Time-NLP
 * @description: 时间段类， 包括两个TimeUnit， 起始时间和终止时间
 * @author: ChaiLinZheng
 * @create: 2019-06-03 21:29
 **/
public class PeriodUnit {
    public TimeUnit start;
    public TimeUnit end;
    public TimeAcc timeAcc; //用于判断单时间点的时间段的单位(年月日)
    private TimeNormalizer timeNormalizer;

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 17:11
    * @param
    * @return
    * @description 具有明确的两个时间点
     */
    public PeriodUnit(TimeUnit timeStart, TimeUnit timeEnd, TimeNormalizer timeNormalizer){
        String endExp = timeEnd.Time_Expression;
        String endNorm = timeEnd.Time_Norm;
        String startNorm = timeStart.Time_Norm;
        this.timeNormalizer = timeNormalizer;


        //年份不相同就交换， 相同就先不交换

        // 2019年3月到2019年, 6月1日到6月， 5月3日到5月， 5月6日到五月3日
        if ((startNorm.charAt(startNorm.length()-1)  == '月' ||
             startNorm.charAt(startNorm.length()-1)  == '日')&&
                endNorm.charAt(endNorm.length()-1)  == '年' &&
                compareYear(timeStart.getTime(), timeEnd.getTime()) == 0
        ){
            start = timeStart;
            end = timeEnd;

        }
        else if (startNorm.charAt(startNorm.length()-1)  == '日' &&
                endNorm.charAt(endNorm.length()-1)  == '月' &&
                compareMonth(timeStart.getTime(), timeEnd.getTime()) == 0
        ){
            start = timeStart;
            end = timeEnd;
        }
        else {
            if (timeStart.getTime().compareTo(timeEnd.getTime()) > 0) {
                start = timeEnd;
                end = timeStart;

            } else {
                start = timeStart;
                end = timeEnd;
            }
        }

        judgeSetFuture(true);

    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/13 11:44
    * @param
    * @return
    * @description 判断是不是今年和当前月份， 用来把结束时间改为今天
     */
    private void judgeSetFuture(boolean setEnd){
        String endNorm = end.Time_Norm;
        char lastChar = endNorm.charAt(endNorm.length()-1);
        int thisYear, thisMonth;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        thisYear = calendar.get(Calendar.YEAR);
        thisMonth = calendar.get(Calendar.MONTH);

        if (lastChar == '月'){
            calendar.setTime(end.getTime()); //TODO 抽象出这块为函数
            calendar.add(Calendar.MONTH, 1);
            calendar.add(Calendar.SECOND, -1);

            if (setEnd) {
                end = new TimeUnit(
                        formatDate(calendar.getTime()),
                        timeNormalizer,
                        start._tp
                );
            }
            if (calendar.get(Calendar.YEAR) == thisYear &&
                    calendar.get(Calendar.MONTH) == thisMonth
            ){
                setFutureToToday();
            }

        }
        else if (lastChar == '年'){

            calendar.setTime(end.getTime()); //TODO 抽象出这块为函数
            calendar.add(Calendar.YEAR, 1);
            calendar.add(Calendar.SECOND, -1);
            if (setEnd) {
                end = new TimeUnit(
                        formatDate(calendar.getTime()),
                        timeNormalizer,
                        start._tp
                );
            }
            if (calendar.get(Calendar.YEAR) == thisYear){
                setFutureToToday();
            }
        }
    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 17:11
    * @param
    * @return
    * @description 只有一个时间点
     */
    public PeriodUnit(TimeUnit timeStart, TimeNormalizer timeNormalizer, boolean toNow){
        this.timeNormalizer = timeNormalizer;
        if (!toNow){

            String timeNorm = timeStart.Time_Norm;
            String accStr = timeNorm.substring(timeNorm.length()-1);
            start = timeStart;
            setTimeAcc(accStr);
            setTimeEnd();
        }
        else{
            start = timeStart;
            end = new TimeUnit("今天", timeNormalizer);
        }
        //System.out.println(end.Time_Norm);

    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/13 1:20
    * @param
    * @return
    * @description 比较两个时间的年份
     */
    private int compareYear(Date date1, Date date2){
        Calendar calendar = Calendar.getInstance();
        int year1, year2;
        calendar.setTime(date1);
        year1 = calendar.get(Calendar.YEAR);
        calendar.setTime(date2);
        year2 = calendar.get(Calendar.YEAR);

        if (year1> year2)   return 1;
        else if (year1 < year2) return -1;
        return 0;

    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/13 11:16
    * @param
    * @return
    * @description 比较月份
     */
    private int compareMonth(Date date1, Date date2){
        Calendar calendar = Calendar.getInstance();
        int month1, month2;
        calendar.setTime(date1);
        month1 = calendar.get(Calendar.MONTH);
        calendar.setTime(date2);
        month2 = calendar.get(Calendar.MONTH);

        if (month1> month2)   return 1;
        else if (month1 < month2) return -1;
        return 0;

    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 19:43
    * @param: timeLen 向前数的天数,
     *        measure 单位(年月日周)
    * @return
    * @description 初始化从现在时间往前数几天,几个月,几周的时间段
     */
    public PeriodUnit(int timeLen, String measure, TimeNormalizer timeNormalizer){
        this.timeNormalizer = timeNormalizer;
        end = new TimeUnit("今天", timeNormalizer);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end.getTime());
        if (measure.equals("年")){
            calendar.add(Calendar.YEAR, -timeLen);
        }
        else if (measure.equals("月")){
            calendar.add(Calendar.MONTH, -timeLen);
        }
        else if (measure.equals("日") || measure.equals("天")){
            calendar.add(Calendar.DAY_OF_MONTH, -timeLen);
        }
        else if (measure.equals("周")){
            calendar.add(Calendar.WEEK_OF_MONTH, -timeLen);
        }
        else{
            System.out.println("不能识别时间单位!");
        }
        start = new TimeUnit(formatDate(calendar.getTime()), timeNormalizer);

    }
    /**
    * @author LinZheng Chai
    * @date 2019/6/13 0:37
    * @param
    * @return
    * @description 将结束时间中未来时间拉回今天
     */
    private void setFutureToToday(){
        //Date endDate = end.getTime();
        Calendar calendar = Calendar.getInstance();
            end = new TimeUnit("今天", timeNormalizer);
    }

    /**
     * @author LinZheng Chai
     * @date 2019/6/4 15:59
     * @param
     * @return
     * @description 设定时间精度
     */
    private void setTimeAcc(String accStr) {
        if (accStr.equals("年")){
            timeAcc = TimeAcc.year;
        }
        else if (accStr.equals("月")){
            timeAcc = TimeAcc.month;
        }
        else if (accStr.equals("日")){
            timeAcc = TimeAcc.day;
        }
        else if (accStr.equals("时")){
            timeAcc = TimeAcc.hour;
        }
        else if (accStr.equals("分")){
            timeAcc = TimeAcc.minute;
        }
        else if (accStr.equals("秒")){
            timeAcc = TimeAcc.second;
        }
        else{
            timeAcc = TimeAcc.day;
        }

    }

    /**
    * @author LinZheng Chai
    * @date 2019/6/4 16:03
    * @param
    * @return
    * @description 只有一个时间点的情况下, 根据时间精度设置结束时间
     */
    private void setTimeEnd() {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(start.getTime());
        if (timeAcc==TimeAcc.year){

            if (compareYear(start.getTime(), new Date()) == 0){
                end = new TimeUnit("今天", timeNormalizer);
            }
            else {
                calendar.add(Calendar.YEAR, 1);
                calendar.add(Calendar.DAY_OF_MONTH, -1);

                end = new TimeUnit(
                        formatDate(calendar.getTime()),
                        timeNormalizer,
                        start._tp
                );
            }
        }
        else if (timeAcc==TimeAcc.month){
            if (compareYear(start.getTime(), new Date()) == 0 &&
                    compareMonth(start.getTime(), new Date()) == 0){
                end = new TimeUnit("今天", timeNormalizer);
            }
            else {
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                end = new TimeUnit(
                        formatDate(calendar.getTime()),
                        timeNormalizer,
                        start._tp
                );
            }
        }
        else if (timeAcc==TimeAcc.day){
            Date endDay = DateUtil.dayEnd(calendar.getTime());
            end = new TimeUnit(
                    formatDate(endDay),
                    timeNormalizer
            );

        }

    }


    /**
    * @author LinZheng Chai
    * @date 2019/6/5 20:25
    * @param
    * @return
    * @description 将时间规范化成: yyyy年MM月dd日 HH时mm分ss秒 的格式.
     */
    public  String formatDate(Date date) {
        return DateUtil.formatDate(date, "yyyy年MM月dd日 HH时mm分ss秒");
    }


    @Override
    public String toString(){
        return DateUtil.formatDateDefault(start.getTime())
                +" - "+
                DateUtil.formatDateDefault(end.getTime())+"\n";
    }


}
