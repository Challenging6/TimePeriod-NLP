package com.time.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.time.util.excption.ExceptionUtils;


/**
 * properties文件配置加载器
 *
 * @author Xianjie Wu wuxianjie@ai-strong.com
 * @date 2018年12月28日 上午10:00:51
 */
public class PropFileLoader {

	/**
	 * 从指定路径加载配置文件
	 * 
	 * @author Xianjie Wu wuxianjie@ai-strong.com
	 * @date 2018年12月28日 上午11:27:38
	 * @param path
	 * @return
	 */
	public static Properties load(String path) throws Exception {
		Properties properties = new Properties();
		File file = new File(path);
		if (file.exists()) {
			InputStream inputStream = new FileInputStream(file);
			// 通过Files获取文件输入流

			// 从输入流中读取属性列表
			properties.load(inputStream);
		} else {
			ExceptionUtils.wrapBusinessException("文档位置不正确：" + path);
		}
		return properties;
	}

}
