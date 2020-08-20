package com.chen.common;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
@Data
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL) //保证序列化json的时候,如果是null的对象,key也会消失
public class CommonResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    private CommonResponse(int status) {
        this.status = status;
    }

    private CommonResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private CommonResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private CommonResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
    @JsonIgnore  //使之不在json序列化结果当中
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public static <T> CommonResponse<T> createBySuccess(){
        return new CommonResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    public static <T> CommonResponse<T> createBySuccessMessage(String msg){
        return new CommonResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    public static <T> CommonResponse<T> createBySuccess(T data){
        return new CommonResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    public static <T> CommonResponse<T> createBySuccess(String msg,T data){
        return new CommonResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }

    public static <T> CommonResponse<T> createByError(){
        return new CommonResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }
    public static <T> CommonResponse<T> createByErrorMessage(String msg){
        return new CommonResponse<T>(ResponseCode.ERROR.getCode(),msg);
    }
    public static <T> CommonResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){
        return new CommonResponse<T>(errorCode,errorMessage);
    }
}
