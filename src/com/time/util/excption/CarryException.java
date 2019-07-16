package com.time.util.excption;


/**
 * 携带异常，将某种异常场景下的环境对象携带到处理程序处
 * @since 2018年1月24日上午10:07:34
 * @author 刘俊杰
 */
public class CarryException extends RuntimeException {

  private static final long serialVersionUID = 7622949029142248702L;

  /**
   * 异常场景下的环境对象
   */
  private final Object obj;

  /**
   * 携带异常构造函数
   *
   * @param obj 异常场景下的环境对象
   */
  public CarryException(Object obj) {
    this.obj = obj;
  }

  /**
   * 获取异常场景下的环境对象
   *
   * @return 异常场景下的环境对象
   */
  public Object getObj() {
    return this.obj;
  }

}
