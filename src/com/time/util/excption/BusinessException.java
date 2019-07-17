package com.time.util.excption;


/**
 * 通用框架业务异常
 * @since 2018年1月24日上午10:06:28
 * @author 刘俊杰
 */
public class BusinessException extends RuntimeException {

  private static final long serialVersionUID = -5506475841265255052L;

  /**
   * 出现异常的额外业务定位信息
   */
  private final String location;

  /**
   * 通用业务异常的构造函数
   *
   * @param message 业务异常信息
   */
  public BusinessException(String message) {
    super(message);
    this.location = null;
  }

  /**
   * 通用业务异常的构造函数
   *
   * @param message 业务异常信息
   * @param location 出现异常的额外业务定定位信息
   */
  public BusinessException(String message, String location) {
    super(message);
    this.location = location;
  }

  /**
   * 获取额外业务定位信息
   *
   * @return 额外业务定位信息
   */
  public String getLocation() {
    return this.location;
  }

}
