package com.time.nlp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.time.enums.SpTimeExp;
import com.time.util.DateUtil;
import com.time.util.FileUtil;

/**
 * <p>
 * 新版时间表达式识别的主要工作类
 * <p>
 *
 * @author <a href="mailto:kexm@corp.21cn.com">kexm</a>
 * @since 2016年5月4日
 */
public class TimeNormalizer implements Serializable, Cloneable {

	private static final long serialVersionUID = 463541045644656392L;
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeNormalizer.class);

	private String timeBase;
	private String oldTimeBase;
	private static Pattern patterns = null;
	private String target;
	private TimeUnit[] timeToken = new TimeUnit[0];
	private boolean isPreferFuture = true;
	private static volatile TimeNormalizer timeNormalizer = null;
	// 校验系统类型
	public String validateType;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	protected TimeNormalizer() {
		if (patterns == null) {
			try {
				InputStream in = getClass().getResourceAsStream("/TimeExp.m");
				ObjectInputStream objectInputStream = new ObjectInputStream(
						new BufferedInputStream(new GZIPInputStream(in)));
				patterns = readModel(objectInputStream);
				// 默认为赢家系统调用
				this.validateType = "winner";
			} catch (Exception e) {
				e.printStackTrace();
				System.err.print("Read model error!");
			}
		}
	}

	/**
	 * 参数为TimeExp.m文件路径
	 *
	 * @param path
	 */
	protected TimeNormalizer(String path) {
		if (patterns == null) {
			try {
				patterns = readModel(path);
				URL url = TimeNormalizer.class.getResource("/time-config.properties");
				// 获取时间配置
//				this.validateType = PropFileLoader.load(url.getFile()).getProperty("name");
				// 默认为赢家系统调用
				this.validateType = "winner";
			} catch (Exception e) {
				e.printStackTrace();
				System.err.print("Read model error!");
			}
		}
	}

	/**
	 * 参数为TimeExp.m文件路径
	 *
	 * @param path
	 */
	protected TimeNormalizer(String path, boolean isPreferFuture) {
		this.isPreferFuture = isPreferFuture;
		if (patterns == null) {
			try {
				patterns = readModel(path);
				URL url = TimeNormalizer.class.getResource("/time-config.properties");
				// 获取时间配置
//				this.validateType = PropFileLoader.load(url.getFile()).getProperty("name");
				// 默认为赢家系统调用
				this.validateType = "winner";
				LOGGER.debug("loaded pattern:{}", patterns.pattern());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.print("Read model error!");
			}
		}
	}

	/**
	 * @author LinZheng Chai
	 * @date 2019/6/10 17:02
	 * @param
	 * @return
	 * @description Double-Check 实现单例
	 */
	public static TimeNormalizer getInstance(String modelPath) {
		if (timeNormalizer == null) {
			synchronized (TimeNormalizer.class) {
				if (timeNormalizer == null) {
					timeNormalizer = new TimeNormalizer(modelPath);
				}
			}
		}
		return timeNormalizer;
	}

	public static TimeNormalizer getInstance() {
		if (timeNormalizer == null) {
			synchronized (TimeNormalizer.class) {
				if (timeNormalizer == null) {
					timeNormalizer = new TimeNormalizer();
				}
			}
		}
		return timeNormalizer;
	}

	public static TimeNormalizer getInstance(String modelPath, boolean _isPreferFuture) {
		if (timeNormalizer == null) {
			synchronized (TimeNormalizer.class) {
				if (timeNormalizer == null) {
					timeNormalizer = new TimeNormalizer(modelPath, _isPreferFuture);
				}
			}
		}
		return timeNormalizer;
	}

	/**
	 * TimeNormalizer的构造方法，根据提供的待分析字符串和timeBase进行时间表达式提取 在构造方法中已完成对待分析字符串的表达式提取工作
	 *
	 * @param target   待分析字符串
	 * @param timeBase 给定的timeBase
	 * @return 返回值
	 */
	public TimeUnit[] parse(String target, String timeBase) {
		this.target = target;
		this.timeBase = timeBase;
		this.oldTimeBase = timeBase;
		// 字符串预处理
		preHandling();
		timeToken = TimeEx(this.target, timeBase);
		this.validateTime();
		return timeToken;
	}

	/**
	 * 同上的TimeNormalizer的构造方法，timeBase取默认的系统当前时间
	 *
	 * @param target 待分析字符串
	 * @return 时间单元数组
	 */
	public TimeUnit[] parse(String target) {
		this.target = target;
		System.out.println("Parsing: " + target);
		this.timeBase = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());// TODO
		this.oldTimeBase = timeBase;
		preHandling();// 字符串预处理
		timeToken = TimeEx(this.target, timeBase);
		this.validateTime();
		return timeToken;
	}

	/**
	 * 校验日期的真实性
	 * 
	 */
	private void validateTime() {
		Calendar c = Calendar.getInstance();
		// 获取当前日期
		Date curDate = new Date();
		for (TimeUnit timeUnit : this.timeToken) {
			// 修正当前时间
			switch (this.validateType) {
			// 赢家系统
			case "winner":
				// 修正比较日期
				c.setTime(curDate);
				c.add(Calendar.DATE, -1);
				curDate = c.getTime();
				break;
			// cmt系统
			case "cmt":
				curDate = new Date();
				break;
			default:
				break;
			}
			// 如果抽取时间大于当前时间
			if (timeUnit.getTime().after(curDate)) {
				// 不包含月月份信息
				if (!timeUnit.Time_Expression.contains("月")) {
					// 往前算一个月
					c.setTime(timeUnit.getTime());
					c.add(Calendar.MONTH, -1);
					// 修改时间值
					timeUnit.setTime(c.getTime());
					// 修改时间标准表达
					String timeNorm = timeUnit.Time_Norm;
					String pattern = "年(?<month>[0-9]+)月";
					Matcher matcher = Pattern.compile(pattern).matcher(timeNorm);
					while (matcher.find()) {
						String matchedStr = matcher.group("month");
						int month = Integer.parseInt(matchedStr) - 1;
						timeNorm = timeNorm.replace(matchedStr + "月", Integer.toString(month) + "月");
						timeUnit.Time_Norm = timeNorm;
					}
					// 修改时间修正标志位
					timeUnit.setModified(true);
					timeUnit.modifiedType = "month-1";
				} else if (!timeUnit.Time_Expression.contains("年")) {
					// 有月和日的，对年份进行调整
					// 往前算一个年
					c.setTime(timeUnit.getTime());
					c.add(Calendar.YEAR, -1);
					timeUnit.setTime(c.getTime());
					// 修改时间标准表达
					String timeNorm = timeUnit.Time_Norm;
					String pattern = "(?<year>[0-9]+)年";
					Matcher matcher = Pattern.compile(pattern).matcher(timeNorm);
					while (matcher.find()) {
						String matchedStr = matcher.group("year");
						int month = Integer.parseInt(matchedStr) - 1;
						timeNorm = timeNorm.replace(matchedStr + "年", Integer.toString(month) + "年");
						timeUnit.Time_Norm = timeNorm;
					}
					timeUnit.setModified(true);
					timeUnit.modifiedType = "year-1";
				}
			} else {
				// 不包含日的信息，仅包含月份，修改年
				// 月份相同
				c.setTime(curDate);
				c.add(Calendar.MONTH, -1);
				curDate = c.getTime();
				if (timeUnit.getTime().after(curDate) && timeUnit.Time_Expression.contains("月")
						&& !timeUnit.Time_Expression.contains("日") && !timeUnit.Time_Expression.contains("号")
						&& !timeUnit.Time_Expression.contains("年")) {
					// 往前算一个年
					if (this.validateType.equals("winner")) {
						c.setTime(timeUnit.getTime());
						c.add(Calendar.YEAR, -1);
						timeUnit.setTime(c.getTime());
						// 修改时间标准表达
						String timeNorm = timeUnit.Time_Norm;
						String pattern = "(?<year>[0-9]+)年";
						Matcher matcher = Pattern.compile(pattern).matcher(timeNorm);
						while (matcher.find()) {
							String matchedStr = matcher.group("year");
							int month = Integer.parseInt(matchedStr) - 1;
							timeNorm = timeNorm.replace(matchedStr + "年", Integer.toString(month) + "年");
							timeUnit.Time_Norm = timeNorm;
						}
						timeUnit.setModified(true);
						timeUnit.modifiedType = "month-1";
					}
				}
			}
		}

	}

	/**
	 * timeBase的get方法
	 *
	 * @return 返回值
	 */
	public String getTimeBase() {
		return timeBase;
	}

	/**
	 * oldTimeBase的get方法
	 *
	 * @return 返回值
	 */
	public String getOldTimeBase() {
		return oldTimeBase;
	}

	public boolean isPreferFuture() {
		return isPreferFuture;
	}

	public void setPreferFuture(boolean isPreferFuture) {
		this.isPreferFuture = isPreferFuture;
	}

	/**
	 * timeBase的set方法
	 *
	 * @param s timeBase
	 */
	public void setTimeBase(String s) {
		timeBase = s;
	}

	/**
	 * 重置timeBase为oldTimeBase
	 */
	public void resetTimeBase() {
		timeBase = oldTimeBase;
	}

	/**
	 * 时间分析结果以TimeUnit组的形式出现，此方法为分析结果的get方法
	 *
	 * @return 返回值
	 */
	public TimeUnit[] getTimeUnit() {
		return timeToken;
	}

	/**
	 * 待匹配字符串的清理空白符和语气助词以及大写数字转化的预处理
	 */
	private void preHandling() {
		target = stringPreHandlingModule.delKeyword(target, "\\s+"); // 清理空白符
		target = stringPreHandlingModule.delKeyword(target, "[的]+"); // 清理语气助词
		// 时间表表达格式标准化
		String pattern = "(?<year>(19|20|21)[0-9]{2})[\\.\\。\\-\\/]?(?<month>(0[1-9]|1[0-1]))[\\.\\。\\-\\/]?(?<day>(0[1-9]|[1-2][0-9]|3[0-1]))";
		Matcher matcher = Pattern.compile(pattern).matcher(target);
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
			target = target.replace(matchedStr, repStr);
		}
		// 缺失时间补全
		String informalTimePattern = "(?<year>(19|20|21)?[0-9]{2})(年|\\,|\\，)(?<month>([0-9]{1,2}))月(?<day>(0[1-9]|[1-2][0-9]|3[0-1])(日|号)?)";
		Matcher infornalTimeMatcher = Pattern.compile(informalTimePattern).matcher(target);
		while (infornalTimeMatcher.find()) {
			String matchedStr = infornalTimeMatcher.group(0);
			String year = infornalTimeMatcher.group("year");
			if (year.length() == 2) {
				// 长度为2的年份自动补全为20年
				year = "20" + year;
			}
			String month = infornalTimeMatcher.group("month");
			String day = infornalTimeMatcher.group("day");
			String repStr = "";
			if (matchedStr.endsWith("日") || matchedStr.endsWith("号")) {
				repStr = year + "年" + month + "月" + day;
			} else {
				repStr = year + "年" + month + "月" + day + "日";
			}
			if (!repStr.equals("")) {
				target = target.replace(matchedStr, repStr);
			}
		}
//		// 处理最近半年
//		if (target.contains("最近半年")) {
//			target = target.replace("最近半年", "最近6个月");
//		}
		// 处理特殊时间
		int i = 0;
		for (SpTimeExp spTimeExp : SpTimeExp.values()) {
			for (int j = 0; j < spTimeExp.getExps().length; j++) {
				String timeExp = spTimeExp.getExps()[j];
				if (target.contains(timeExp)) {
					String repStr = "#sptime" + i + "#" + j + "#";
					target = target.replaceAll(timeExp, repStr);
				}
			}
			i++;
		}
		// 大写数字转化
		target = stringPreHandlingModule.numberTranslator(target);
		// TODO 处理大小写标点符号

	}

	/**
	 * 有基准时间输入的时间表达式识别
	 * <p>
	 * 这是时间表达式识别的主方法， 通过已经构建的正则表达式对字符串进行识别，并按照预先定义的基准时间进行规范化 将所有别识别并进行规范化的时间表达式进行返回，
	 * 时间表达式通过TimeUnit类进行定义
	 *
	 * @param: String 输入文本字符串
	 * @param: String 输入基准时间
	 * @return TimeUnit[] 时间表达式类型数组
	 */
	private TimeUnit[] TimeEx(String tar, String timebase) {
		Matcher match;
		int startline = -1, endline = -1;

		String[] temp = new String[99];
		int rpointer = 0;// 计数器，记录当前识别到哪一个字符串了
		TimeUnit[] Time_Result = null;

		match = patterns.matcher(tar);
		boolean startmark = true;
		while (match.find()) {
			startline = match.start();
			if (endline == startline) // 假如下一个识别到的时间字段和上一个是相连的 @author kexm
			{
				rpointer--;
				temp[rpointer] = temp[rpointer] + match.group();// 则把下一个识别到的时间字段加到上一个时间字段去
			} else {
				if (!startmark) {
					rpointer--;
					rpointer++;
				}
				startmark = false;
				temp[rpointer] = match.group();// 记录当前识别到的时间字段，并把startmark开关关闭。这个开关貌似没用？
			}
			endline = match.end();
			rpointer++;
		}
		if (rpointer > 0) {
			rpointer--;
			rpointer++;
		}
		Time_Result = new TimeUnit[rpointer];
		/** 时间上下文： 前一个识别出来的时间会是下一个时间的上下文，用于处理：周六3点到5点这样的多个时间的识别，第二个5点应识别到是周六的。 */
		TimePoint contextTp = new TimePoint();
		for (int j = 0; j < rpointer; j++) {
			Time_Result[j] = new TimeUnit(temp[j], this, contextTp);
			contextTp = Time_Result[j]._tp;
		}
		/** 过滤无法识别的字段 */
		Time_Result = filterTimeUnit(Time_Result);
		return Time_Result;
	}

	/**
	 * 过滤timeUnit中无用的识别词。无用识别词识别出的时间是1970.01.01 00:00:00(fastTime=-28800000)
	 *
	 * @param timeUnit
	 * @return
	 */
	public static TimeUnit[] filterTimeUnit(TimeUnit[] timeUnit) {
		if (timeUnit == null || timeUnit.length < 1) {
			return timeUnit;
		}
		List<TimeUnit> list = new ArrayList<>();
		for (TimeUnit t : timeUnit) {
			if (t.getTime().getTime() != -28800000) {
				list.add(t);
			}
		}
		TimeUnit[] newT = new TimeUnit[list.size()];
		newT = list.toArray(newT);
		return newT;
	}

	private Pattern readModel(String file) throws Exception {
		ObjectInputStream in;
		if (file.startsWith("jar:file") || file.startsWith("file:")) {
			in = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new URL(file).openStream())));
		} else {
			in = new ObjectInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		}
		return readModel(in);
	}

	private Pattern readModel(ObjectInputStream in) throws Exception {
		Pattern p = (Pattern) in.readObject();
		LOGGER.debug("model pattern:{}", p.pattern());
		return Pattern.compile(p.pattern());
	}

	public static void writeModel(Object p, String path) throws Exception {
		ObjectOutputStream out = new ObjectOutputStream(
				new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(path))));
		out.writeObject(p);
		out.close();
	}

	/**
	 * 重写模型
	 * 
	 * @author Xianjie Wu wuxianjie@ai-strong.com
	 * @param pattern
	 */
	public static void reWriteModel() {
		// 获取resource文件夹目录
		String rootPath = FileUtil.getResourceAbsPath();
		// 全局配置的绝对路径
		String filePath = rootPath.concat("TimeExp.txt");
		String pattern = FileUtil.readAllLinesOrExit(Paths.get(filePath)).get(0);
		// 修改模型
		String path = TimeNormalizer.class.getResource("").getPath();
		String classPath = path.substring(0, path.indexOf("/com/time"));
		System.out.println(classPath + "/TimeExp.m");
		/** 写TimeExp */
		Pattern p = Pattern.compile(pattern);
		try {
			TimeNormalizer.writeModel(p, classPath + "/TimeExp.m");
			System.out.println("写入完成");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws Exception {
		// 重写模型
		TimeNormalizer.reWriteModel();
		// 测试
		URL url = TimeNormalizer.class.getResource("/TimeExp.m");
		TimeNormalizer normalizer = TimeNormalizer.getInstance(url.toURI().toString());
		normalizer.setPreferFuture(false);
		System.out.println("系统类型：" + normalizer.validateType);
		normalizer.parse("打印最近六个月流水账单");// 抽取时间
		TimeUnit[] unit = normalizer.getTimeUnit();
		if (unit.length > 0) {
			for (TimeUnit timeUnit : unit) {
				System.out.println(DateUtil.formatDateDefault(timeUnit.getTime()) + "-" + timeUnit.getIsAllDayTime()
						+ " " + timeUnit.Time_Norm + " " + timeUnit.Time_Expression);
			}
		} else {
			System.err.println("未抽取到时间！");
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		
		return super.clone();
	}

}
