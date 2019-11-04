package com.tanp.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * 保证序列化 JSon 的时候，如果是 null 的对象，可以也会消失
 *
 * @author PangT
 * @since 2018/12/19
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {

  private int status;
  private String msg;
  private T data;

  private ServerResponse(int status) {
    this.status = status;
  }

  private ServerResponse(int status, T data) {
    this.status = status;
    this.data = data;
  }

  private ServerResponse(int status, String msg, T data) {
    this.status = status;
    this.msg = msg;
    this.data = data;
  }

  private ServerResponse(int status, String msg) {
    this.status = status;
    this.msg = msg;
  }

  /**
   * 使之不在 json 序列化结果当中
   *
   * @return 返回是否成功
   */
  @JsonIgnore
  public boolean isSuccess() {
    return this.status == ResponseCode.SUCCESS.getCode();
  }

  public int getStatus() {
    return status;
  }

  public T getData() {
    return data;
  }

  public String getMsg() {
    return msg;
  }

  public static <T> ServerResponse<T> createBySuccess() {
    return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
  }

  public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
    return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
  }

  public static <T> ServerResponse<T> createBySuccess(T data) {
    return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
  }

  public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
    return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
  }


  public static <T> ServerResponse<T> createByError() {
    return new ServerResponse<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
  }

  public static <T> ServerResponse<T> createByErrorMessage(String errorMesage) {
    return new ServerResponse<T>(ResponseCode.ERROR.getCode(), errorMesage);
  }

  public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMesage) {
    return new ServerResponse<T>(errorCode, errorMesage);
  }

}
