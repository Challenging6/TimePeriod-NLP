package com.time.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import com.time.util.excption.ExceptionUtils;


/**
 * 读写文件工具类
 *
 * @author Xianjie Wu wuxianjie@ai-strong.com
 * @date 2018年12月12日 下午3:33:07
 */
public class FileUtil {
	/**
	 * 按行读取文件文本
	 * 
	 * @date 2018年12月7日 下午2:06:34
	 * @param path
	 * @return
	 */
	public static List<String> readAllLinesOrExit(Path path) {
		try {
			return Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {
			ExceptionUtils.wrapException(e);
			// System.err.println("Failed to read [" + path + "]: " + e.getMessage());
			// System.exit(0);
		}
		return null;

	}

	/**
	 * 按行写文件文本
	 * 
	 * @author Xianjie Wu wuxianjie@ai-strong.com
	 * @date 2018年12月12日 下午4:08:54
	 * @param lines
	 * @param path
	 */
	public static void writeAllLinesorExit(Iterable<? extends CharSequence> lines, Path path) {
		try {

			Files.write(path, lines, StandardOpenOption.WRITE);
		} catch (IOException e) {
			ExceptionUtils.wrapException(e);
//			System.err.println("Failed to wirte [" + path + "]: " + e.getMessage());
//			System.exit(0);
		}
	}

	/**
	 * 获取路径下所有文件名（不包含文件夹）
	 * 
	 * @author Xianjie Wu wuxianjie@ai-strong.com
	 * @date 2019年3月6日 下午5:01:14
	 * @param path
	 * @return
	 */
	public static List<String> getAllFileName(String path) {
		List<String> fileNames = new ArrayList<String>();
		File file = new File(path);
		for (File childFile : file.listFiles()) {
			if (childFile.isFile()) {
				fileNames.add(childFile.getName());
			}
		}
		return fileNames;
	}

	/**
	 * 获取resource目录绝对路径（MacOS）
	 * 
	 * @author Xianjie Wu wuxianjie@ai-strong.com
	 * @date 2019年3月6日 下午11:59:06
	 * @return
	 */
	public static String getResourceAbsPath() {
		return FileUtil.class.getClassLoader().getResource("").getPath();
	}

	/**
	 * 获取resource目录下文件的绝对路径
	 * 
	 * @author Xianjie Wu wuxianjie@ai-strong.com
	 * @date 2019年3月7日 上午11:59:26
	 * @param path 输入为路径名
	 * @return
	 */
	public static String getResFilesAbsPath(String path) {
		// 获取resource绝对路径
		String resAbsPath = FileUtil.getResourceAbsPath();
		// windows系统标志位
		boolean flag = false;
		// 判断是否为windows系统
		if (System.getProperty("file.separator").equals("\\")) {
			flag = true;
		}
		// windows系统获取到的路径需要做修改
		if (flag) {
			resAbsPath = resAbsPath.substring(1);
		}
		// 判断路径结尾是否有多余的/
		if (!resAbsPath.endsWith("/")) {
			return resAbsPath.concat("/").concat(path);
		} else {
			return resAbsPath.concat(path);
		}
	}

	public static void main(String[] args) {
		System.out.println(System.getProperty("file.separator"));
	}
}
