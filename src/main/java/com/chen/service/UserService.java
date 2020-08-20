package com.chen.service;

import com.chen.common.CommonResponse;
import com.chen.entity.User;

public interface UserService {


    //登录
    CommonResponse<User> login(String username, String password);

    //用户注册
    CommonResponse<String> register(User user);

    //校验用户名或邮箱
    CommonResponse<String> checkValid(String str, String type);

    //根据用户名查看忘记密码问题
    CommonResponse<String> selectQuestion(String username);

    //检查找回密码问题的答案是否正确
    CommonResponse<String> checkAnswer(String username, String question, String answer);

    //找回密码，重置密码
    CommonResponse<String> forgetRestPassword(String username, String newPassword, String forgetToken);

    //修改密码
    CommonResponse<String> restPassword(String oldPassword, String newPassword, User user);

    //更新用户
    CommonResponse<User> updateUser(User user);

    //根据用户id获取用户个人信息
    CommonResponse<User> getUserInformation(Integer id);
}
