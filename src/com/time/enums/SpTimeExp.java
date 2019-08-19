package com.time.enums;

/**
 * 特殊时间表达转换(转换特殊时间表达为可解析的时间表达)
 *
 * @author Xianjie Wu wuxianjie@ai-strong.com
 * @date 2019年7月18日 下午6:06:05
 */
public enum SpTimeExp {

	// 大前天（上上上一天）
	LAST_LAST_LAST_DAY(new String[] { "大前天" }),

	// 前天（上上一天）
	LAST_LAST_DAY(new String[] { "前天", "上上日" }),

	// 昨天（上一天）
	LAST_DAY(new String[] { "昨天", "昨日", "前一天" }),

	// 上上上月（上上上一月）
//	LAST_LAST_LAST_MONTH(new String[] { "去年", "前一年", "上一年" }),	

	// 上上月（上上一月）
	LAST_LAST_MONTH(new String[] { "上上个月", "上上月" }),

	// 上月（上一月）
	LAST_MONTH(new String[] { "上个月", "上一个月", "上一月" }),
	
	// 大前年（上上上一年）
	LAST_LAST_LAST_YEAR(new String[] { "大前年" }),
	
	// 前年（上上一年）
	LAST_LAST_YEAR(new String[] { "前年", "上上一年" }),

	// 去年（上一年）
	LAST_YEAR(new String[] {"去年底", "去年", "前一年", "上一年" });

	private String[] exps;

	private SpTimeExp(String[] exps) {
		this.exps = exps;
	}

	public String[] getExps() {
		return exps;
	}

}
