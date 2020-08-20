package com.chen.service.impl;

import com.chen.common.CommonResponse;
import com.chen.common.Const;
import com.chen.common.TokenCache;
import com.chen.dao.UserMapper;
import com.chen.entity.User;
import com.chen.service.UserService;
import com.chen.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("userService")
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @Override
    public CommonResponse<User> login(String username, String password) {
        int result = userMapper.checkUsername(username);
        if (result ==0){
            return CommonResponse.createByErrorMessage("该用户名不存在");
        }
        //Todo MD5
        User user = userMapper.selectLogin(username, password);
        if (user ==null){
            return CommonResponse.createByErrorMessage("密码错误");
        }

        user.setPassword(StringUtils.EMPTY);//把密码置空
        return CommonResponse.createBySuccess("登录成功",user);
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @Override
    public CommonResponse<String> register(User user) {

        CommonResponse<String> validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()){
            return validResponse;
        }
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //设置角色
        user.setRole(Const.Role.ROLE_CUSTOMER);

        int count = userMapper.insertSelective(user);
        if (count==0){
            return CommonResponse.createByErrorMessage("注册失败");
        }

        return CommonResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 校验用户名或邮箱
     * @param str
     * @param type
     * @return
     */
    @Override
    public CommonResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            //开始校验
            if (Const.USERNAME.equals(type)){
                int result = userMapper.checkUsername(str);
                if (result > 0){
                    return CommonResponse.createByErrorMessage("该用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)){
                int result = userMapper.checkEmail(str);
                if (result > 0){
                    return CommonResponse.createByErrorMessage("该邮箱已存在");
                }
            }

        }else{
            return CommonResponse.createByErrorMessage("参数错误");
        }
        return CommonResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 获取找回密码问题
     * @param username
     * @return
     */
    @Override
    public CommonResponse<String> selectQuestion(String username) {
        CommonResponse<String> response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess()){ //用户不存在
            return CommonResponse.createByErrorMessage("用户不存在");
        }
        String question =  userMapper.selectQuestionByUsername(username);
       if (StringUtils.isNotBlank(question)){
           return CommonResponse.createBySuccess(question);
       }
        return CommonResponse.createByErrorMessage("当前用户的找回密码问题是空的");
    }

    /**
     * 检查找回密码问题的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public CommonResponse<String> checkAnswer(String username, String question, String answer) {
       int resultCount = userMapper.checkAnswer(username,question,answer);
       if (resultCount > 0){  //说明问题及问题答案是这个用户的,并且是正确的
          String forgetToken = UUID.randomUUID().toString();
           TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
           return CommonResponse.createBySuccess(forgetToken);
       }
        return CommonResponse.createByErrorMessage("问题的答案是错误的");
    }

    /**
     * 找回密码，重置密码
     * @param username
     * @param newPassword
     * @param forgetToken
     * @return
     */
    @Override
    public CommonResponse<String> forgetRestPassword(String username, String newPassword, String forgetToken) {
       if (StringUtils.isBlank(forgetToken)){
           return CommonResponse.createByErrorMessage("参数错误，token需要传递");
       }
        CommonResponse<String> response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess()){ //用户不存在
            return CommonResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)){
            return CommonResponse.createByErrorMessage("token无效或过期");
        }
        if (StringUtils.equals(forgetToken,token)){
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if (rowCount > 0 ) {
                return CommonResponse.createBySuccessMessage("修改密码成功");
            }
        }else {
            return CommonResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }

        return CommonResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 修改密码
     * @param oldPassword
     * @param newPassword
     * @param user
     * @return
     */
    @Override
    public CommonResponse<String> restPassword(String oldPassword, String newPassword, User user) {
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword),user.getId());
        if (resultCount == 0){
            return CommonResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0){
            return CommonResponse.createBySuccessMessage("密码更新成功");
        }

        return CommonResponse.createByErrorMessage("密码更新失败");
    }

    /**
     * 更新用户
     * @param user
     * @return
     */
    @Override
    public CommonResponse<User> updateUser(User user) {
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount > 0){
            return CommonResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0){
            return CommonResponse.createBySuccess("更新个人信息成功",updateUser);
        }

        return CommonResponse.createByErrorMessage("更新个人信息失败");
    }

    /**
     * 根据用户id获取用户个人信息
     * @param id
     * @return
     */
    @Override
    public CommonResponse<User> getUserInformation(Integer id) {
        User user= userMapper.selectByPrimaryKey(id);
        if(user ==null){
            return CommonResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return CommonResponse.createBySuccess(user);
    }
}
