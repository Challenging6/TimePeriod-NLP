package com.period;

import com.time.nlp.TimeNormalizer;
import com.time.nlp.TimeUnit;
import com.time.nlp.stringPreHandlingModule;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
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

	private static final String REGEX_FILES = "/PeriodRegex.txt"; // 正则文件
	private static Map<String, Pattern> PATTERNS = null;
	private String modelPath;
	private TimeNormalizer timeNormalizer;
	private static volatile PeriodNormalizer periodNormalizer = null;

	/**
	 * @author LinZheng Chai
	 * @date 2019/6/5 16:56
	 * @param
	 * @return
	 * @description 构造函数
	 */
	private PeriodNormalizer(String modelPath) {
		try {
			this.modelPath = modelPath;
			PATTERNS = readModel(REGEX_FILES); // 加载正则
			// 初始化TimeNormalizer
			try {
				timeNormalizer = (TimeNormalizer) TimeNormalizer.getInstance(this.modelPath, false).clone();
				timeNormalizer.validateType = "cmt";
				// 初始化基准时间
				String timeBase = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
				timeNormalizer.setTimeBase(timeBase);
				timeNormalizer.setOldTimeBase(timeBase);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author LinZheng Chai
	 * @date 2019/6/10 16:53
	 * @param
	 * @return
	 * @description 实例函数， Double-Check 实现单例
	 */
	public static PeriodNormalizer getInstance(String modelPath) {
		if (periodNormalizer == null) {
			synchronized (PeriodNormalizer.class) {
				if (periodNormalizer == null) {
					periodNormalizer = new PeriodNormalizer(modelPath);
				}
			}
		}
		return periodNormalizer;
	}

	/**
	 * @author LinZheng Chai
	 * @date 2019/6/5 16:57
	 * @param
	 * @return
	 * @description 时间段抽取的入口, 返回包含时间段(PeriodUnit)的arrayList
	 */
	public List<PeriodUnit> parse(String target) throws URISyntaxException {

		List<PeriodUnit> periods = new ArrayList<>();

		System.out.println("Parsing: " + target);

		// 需要保持和time包预处理的结果相同，用来定位时间
		target = preHandling(target);
		periodExtract(target, periods);
		return periods;
	}

	/**
	 * @author LinZheng Chai
	 * @date 2019/6/4 0:13
	 * @param
	 * @return
	 * @description 抽取时间段
	 */
	private void periodExtract(String target, List<PeriodUnit> periods) throws URISyntaxException {
		List<TimeUnit> times;

		// 抽取函数, 可以自己定义相应规则和抽取块
		// 1.无需时间点的时间段抽取
		String maskStr = lastTimeExtract(target, periods);

		// 2. 需要时间点的时间段抽取
		// 先对时间进行抽取， 再对相应位置进行标记(time1, time2...)替换，方便后边正则匹配。
		times = parseTime(maskStr); // 抽取时间
		if (!timeNormalizer.getTarget().contains("sptime")) {
			target = timeNormalizer.getTarget();
		}
		maskStr = maskTime(target, times); // 标记时间

		// System.out.println(Arrays.toString(times.toArray()));

		timePointToNow(maskStr, periods, times);
		twoTimeExtract(maskStr, periods, times);
		oneTimePointExtract(maskStr, periods, times);

	}

	/**
	 * @author LinZheng Chai
	 * @date 2019/6/4 11:26
	 * @param
	 * @return
	 * @description 过去的一段时间点至今 (最近五月, 近3年)
	 */
	private String lastTimeExtract(String originStr, List<PeriodUnit> periods) {
		// 对最近半年做特殊处理
//		处理最近半年
		if (originStr.contains("最近半年")) {
			originStr = originStr.replace("最近半年", "最近6个月");
		}
		/**
		 * 先匹配不需要时间抽取的(近5年,近3月, 近一周)
		 */
		Pattern pattern = PATTERNS.get("lastTime");
		Matcher matcher;
		String maskStr = originStr;
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
				timeLen = Integer.valueOf(timeMarks.get(1)); // 时间长度

				PeriodUnit period = new PeriodUnit(timeLen, timeMarks.get(2), // 时间粒度
						timeNormalizer);
				periods.add(period);
				maskStr = matcher.replaceFirst("近xxx");
				timeNormalizer.parse(maskStr);
			}
		}
		return maskStr;
	}

	/**
	 * @author LinZheng Chai
	 * @date 2019/6/5 21:04
	 * @param
	 * @return
	 * @description
	 */
	public void timePointToNow(String maskStr, List<PeriodUnit> periods, List<TimeUnit> times) {
		/**
		 * 匹配需要时间抽取的(去年至今, 4月以来)
		 */
		Pattern pattern = PATTERNS.get("timePointToNow");
		Matcher matcher = pattern.matcher(maskStr);
		// System.out.println("patterns: "+ pattern);
		// System.out.println("Str: "+ maskStr);
		while (matcher.find()) {
			List<String> timeMarks = new ArrayList<>();
			for (int i = 1; i <= matcher.groupCount(); i++) {
				// System.out.println(i+" "+matcher.group(i));
				if (matcher.group(i) != null) {
					timeMarks.add(matcher.group(i));
				}
			}
			if (timeMarks.size() != 1 + 1) {
				System.out.println("时间匹配异常! 匹到的时间个数有错误");
			} else {
				int no1;
				no1 = Integer.valueOf(timeMarks.get(1));
				TimeUnit time = times.get(no1);
				if (time != null) {
					// System.out.println(time1.Time_Norm);

					PeriodUnit period = new PeriodUnit(time, timeNormalizer, true);
					// System.out.println(time1.Time_Expression+": "+period);
					periods.add(period);
					times.set(no1, null);
				}
			}
		}
	}

	/**
	 * @author LinZheng Chai
	 * @date 2019/6/4 11:20
	 * @param
	 * @return
	 * @description 双时间点类型抽取
	 */
	private void twoTimeExtract(String maskStr, List<PeriodUnit> periods, List<TimeUnit> times) {
		Matcher matcher;
		Pattern pattern = PATTERNS.get("twoTime");
		matcher = pattern.matcher(maskStr);
		// System.out.println("patterns: "+ pattern);
		// System.out.println("Str: "+ maskStr);
		while (matcher.find()) {

			List<String> timeMarks = new ArrayList<>();
			for (int i = 1; i <= matcher.groupCount(); i++) {
				// System.out.println(i+" "+matcher.group(i));
				if (matcher.group(i) != null) {
					timeMarks.add(matcher.group(i));
				}
			}
			if (timeMarks.size() != 1 + 2) {
				System.out.println("时间匹配异常!,匹到的时间个数有错误");
			} else {
				int no1, no2;
				no1 = Integer.valueOf(timeMarks.get(1));
				no2 = Integer.valueOf(timeMarks.get(2));
				try {
					TimeUnit timeStart = times.get(no1);
					TimeUnit timeEnd = times.get(no2);
					if (timeStart != null && timeEnd != null) {

						PeriodUnit period = new PeriodUnit(timeStart, timeEnd, timeNormalizer);
						// System.out.println(period);
						periods.add(period);

						times.set(no1, null);
						times.set(no2, null);
					}
				} catch (Exception e) {
					e.printStackTrace();
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
	private void oneTimePointExtract(String maskStr, List<PeriodUnit> periods, List<TimeUnit> times) {
		Matcher matcher;
		Pattern pattern = PATTERNS.get("oneTimePoint");
		matcher = pattern.matcher(maskStr);
		// System.out.println("patterns: "+ pattern);
		// System.out.println("Str: "+ maskStr);
		while (matcher.find()) {

			List<String> timeMarks = new ArrayList<>();
			for (int i = 1; i <= matcher.groupCount(); i++) {
				// System.out.println(i+" "+matcher.group(i));
				if (matcher.group(i) != null) {
					timeMarks.add(matcher.group(i));
				}
			}
			if (timeMarks.size() != 1 + 1) {
				System.out.println("时间匹配异常!,匹到的时间个数有错误");
			} else {
				int no1;
				no1 = Integer.valueOf(timeMarks.get(1));
				TimeUnit time = times.get(no1);

				if (time != null) {
					// System.out.println(time1.Time_Norm);

					PeriodUnit period = new PeriodUnit(time, timeNormalizer, false);

					// System.out.println(time1.Time_Expression+": "+period);

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
	private List<TimeUnit> parseTime(String target) {

		// timeNormalizer.setPreferFuture(true);
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
		// 时间表表达格式标准化
		String pattern = "(?<year>(19|20|21)[0-9]{2})[\\.\\。\\-\\/]?(?<month>(0[1-9]|1[0-1]))[\\.\\。\\-\\/]?(?<day>(0[1-9]|[1-2][0-9]|3[0-1]))";
		Matcher matcher = Pattern.compile(pattern).matcher(str);
		while (matcher.find()) {
			String matchedStr = matcher.group(0);
			String year = matcher.group("year");
			String month = matcher.group("month");
			if (month.startsWith("0")) {
				month = month.substring(1);
			}
			String day = matcher.group("day");
			if (day.startsWith("0")) {
				day = day.substring(1);
			}
			String repStr = year + "年" + month + "月" + day + "日";
			str = str.replace(matchedStr, repStr);
		}
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
	private String maskTime(String target, List<TimeUnit> times) {
		TimeUnit time;
		String timeExp;
		int timeStart;
		int timeEnd;
		String maskStr = target;
		for (int i = 0; i < times.size(); i++) {
			time = times.get(i);
			timeExp = time.Time_Expression;
			timeStart = maskStr.indexOf(timeExp);
			timeEnd = timeStart + timeExp.length() - 1;

			maskStr = maskStr.substring(0, timeStart) + "#time" + i + "#" + maskStr.substring(timeEnd + 1);
			// System.out.println(maskStr);
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
	private Map<String, Pattern> readModel(String path) {
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
				// System.out.println(lineTxt);
				if (lineTxt.trim().length() == 0)
					continue;
				if (lineTxt.substring(0, 2).equals("//"))
					continue; // 跳过注释
				if (lineTxt.substring(0, 4).equals("####")) {
					String regexName = lineTxt.substring(4);
					tempRegex = new ArrayList<>();
					while ((lineTxt = bufferedReader.readLine()) != null) {
						if (lineTxt.trim().length() == 0)
							continue;

						if (lineTxt.substring(0, 4).equals("####")) {
							pattern = Pattern.compile(StringUtils.join(tempRegex, "|"));
							regexMap.put(regexName, pattern);
							break;
						} else {
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

	public static void main(String[] args) {
		URL url = TimeNormalizer.class.getResource("/TimeExp.m");
		PeriodNormalizer periodNormalizer;
		try {
			periodNormalizer = PeriodNormalizer.getInstance(url.toURI().toString());
			List<PeriodUnit> periods;
			periods = periodNormalizer.parse("最近半年");
			System.out.println(periods.get(0).toString().trim());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

}
