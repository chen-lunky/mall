package com.chen.controller.portal;

import com.chen.common.CommonResponse;
import com.chen.common.Const;
import com.chen.common.ResponseCode;
import com.chen.entity.User;
import com.chen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@ResponseBody
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    public CommonResponse<User> login(String username, String password, HttpSession session){
        CommonResponse<User> response = userService.login(username,password);
        if (response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }
    /**
     * 退出登录
     * @param session
     * @return
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.GET)
    public CommonResponse<String> logout( HttpSession session){
       session.removeAttribute(Const.CURRENT_USER);
        return CommonResponse.createBySuccessMessage("退出成功");
    }
    /**
     * 用户注册
     * @param user
     * @return
     */
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    public CommonResponse<String> register(User user){
        return userService.register(user);
    }
    /**
     * 校验用户名或邮箱
     * @param str
     * @param type
     * @return
     */
    @RequestMapping(value = "checkValid.do",method = RequestMethod.POST)
    public CommonResponse<String> checkValid(String str,String type){
        return userService.checkValid(str,type);
    }

    /**
     * 获取用户信息
     * @param session
     * @return
     */
    @RequestMapping(value = "getUserInfo.do",method = RequestMethod.POST)
    public CommonResponse<User> getUserInfo(HttpSession session){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if (user !=null)
            return CommonResponse.createBySuccess(user);
        return CommonResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
    }

    /**
     * 找回密码的问题
     * @param username
     * @return
     */
    @RequestMapping(value = "forgetQuestion.do",method = RequestMethod.POST)
    public CommonResponse<String> forgetQuestion(String username){
        return userService.selectQuestion(username);
    }

    /**
     * 检查找回密码问题的答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @RequestMapping(value = "forgetCheckAnswer.do",method = RequestMethod.POST)
    public CommonResponse<String> forgetCheckAnswer(String username,String question,String answer){
        return userService.checkAnswer(username,question,answer);
    }

    /**
     * 找回密码，重置密码
     * @param username
     * @param newPassword
     * @param forgetToken
     * @return
     */
    @RequestMapping(value = "forgetRestPassword.do",method = RequestMethod.POST)
    public CommonResponse<String> forgetRestPassword(String username,String newPassword,String forgetToken){
        return userService.forgetRestPassword(username,newPassword,forgetToken);
    }

    /**
     * 修改密码
     * @param session
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @RequestMapping(value = "restPassword.do",method = RequestMethod.POST)
    public CommonResponse<String> restPassword(HttpSession session,String oldPassword,String newPassword){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null)
            return CommonResponse.createByErrorMessage("用户未登录");
        return userService.restPassword(oldPassword,newPassword,user);
    }

    /**
     * 更新用户
     * @param session
     * @param user
     * @return
     */
    @RequestMapping(value = "updateUser.do",method = RequestMethod.POST)
    public CommonResponse<User> updateUser(HttpSession session,User user){
        User currentUser =(User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser ==null)
            return CommonResponse.createByErrorMessage("用户未登录");
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        CommonResponse<User> response = userService.updateUser(user);
        if (response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }
    /**
     * 根据用户id获取用户个人信息
      @param session
     * @return
     */
    @RequestMapping(value = "getUserInformation.do",method = RequestMethod.POST)
    public CommonResponse<User> getUserInformation(HttpSession session){
        User user =(User) session.getAttribute(Const.CURRENT_USER);
        if (user ==null)
            return CommonResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");
        return userService.getUserInformation(user.getId());
    }
}
