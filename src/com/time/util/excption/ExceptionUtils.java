package com.time.util.excption;

/**
 * 异常处理工具类
 * @since 2018年1月24日上午10:07:55
 * @author 刘俊杰
 */
public class ExceptionUtils {

  private ExceptionUtils() {
    // 缺省
  }

  /**
   * 抛出携带异常
   *
   * @param obj 异常场景下的环境对象
   */
  public static void carryExceptionObject(Object obj) {
    CarryException exception = new CarryException(obj);
    throw exception;
  }

  /**
   * 抛出未实现异常
   */
  public static void notImplement() {
    String message = "还没有实现此功能";
    BusinessException ex = new BusinessException(message);
    throw ex;
  }

  /**
   * 将最底层的异常解析抛出
   *
   * @param ex 要处理的异常
   * @return 底层异常
   */
  public static Throwable unmarsh(Throwable ex) {
    Throwable cause = ex.getCause();
    if (cause != null) {
      cause = ExceptionUtils.unmarsh(cause);
    }
    else {
      cause = ex;
    }
    return cause;
  }

  /**
   * 抛出不支持异常
   */
  public static void unSupported() {
    String message = "不支持此种业务，请检查?";
    BusinessException ex = new BusinessException(message);
    throw ex;
  }

  /**
   * 抛出业务异常
   *
   * @param message 异常信息
   */
  public static void wrapBusinessException(String message) {
    BusinessException ex = new BusinessException(message);
    throw ex;
  }

  /**
   * 抛出业务异常
   *
   * @param message 异常信息
   * @param location 出现异常的位置
   */
  public static void wrapBusinessException(String message, String location) {
    BusinessException ex = new BusinessException(message, location);
    throw ex;
  }

  /**
   * 将异常装载到快速通道向上传递
   *
   * @param ex 要装载的异常
   */
  public static void wrapException(Throwable ex) {
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    throw new TransferException(ex);
  }

}
