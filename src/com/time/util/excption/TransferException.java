package com.time.util.excption;


/**
 * 传递异常，将异常迅速携带至上层
 * @since 2018年1月24日上午10:09:25
 * @author 刘俊杰
 */
public class TransferException extends RuntimeException {

  private static final long serialVersionUID = -1175141188691689620L;

  /**
   * 传递异常构造函数
   *
   * @param exception 底层异常
   */
  public TransferException(Throwable exception) {
    super(exception);
  }

}
