package com.guttv.pm.platform.action.system;

import com.guttv.pm.core.bean.RoleBean;
import com.guttv.pm.core.bean.UserBean;
import com.guttv.pm.core.fp.RoleToZookeeper;
import com.guttv.pm.core.fp.UserToZookeeper;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.MD5Util;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * 管理员页面Action
 */
@Controller
@RequestMapping("/User")
public class UserAction extends BaseAction{

    @RequestMapping("/list")
    public ModelAndView list(){
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user/user_list");
        List<UserBean> userList = UserToZookeeper.getAllUserFromZookeeper();
        List<RoleBean> roleList = RoleToZookeeper.getAllRoleFromZookeeper();
        mav.addObject("userList",userList);
        mav.addObject("roleList",JsonUtil.toJson(roleList));
        return mav;
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String add(@RequestBody UserBean user){
        Map<String, Object> result = new HashMap<>();
        if(user.getName() == null || UserToZookeeper.getUserFromZookeeper(user.getName()) != null){
            result.put("status",false);
            result.put("message","用户名为空或者已经被使用");
        }else {
            //密码MD5加密
            user.setPassword(MD5Util.string2MD5(user.getPassword()).toUpperCase());
            user.setCreateTime(new Date());
            if (UserToZookeeper.persistanceToZookeeper(user)) {
                result.put("status",true);
                result.put("message","新增管理员成功");
            } else {
                result.put("status",false);
                result.put("message","新增管理员失败");
            }
        }
        return JsonUtil.toJson(result);
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ResponseBody
    public String update(@RequestBody UserBean user){
        Map<String, Object> result = new HashMap<>();
        UserBean oldUser = UserToZookeeper.getUserFromZookeeper(user.getName());
        if(user.getName() == null || oldUser == null){
            result.put("status",false);
            result.put("message","没有找到该管理员");
        }else {
            user.setCreateTime(oldUser.getCreateTime());
            user.setUpdateTime(new Date());
            if (UserToZookeeper.persistanceToZookeeper(user)) {
                result.put("status",true);
                result.put("message","更新成功");
            } else {
                result.put("status",false);
                result.put("message","更新失败");
            }
        }
        return JsonUtil.toJson(result);
    }

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    public ModelAndView get(String name) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user/user_view");
        UserBean user = UserToZookeeper.getUserFromZookeeper(name);
        mav.addObject("user", user);
        return mav;
    }

    @RequestMapping(value = "/del",method = RequestMethod.GET)
    @ResponseBody
    public String del(String name){
        Map<String, Object> result = new HashMap<>();
        if (UserToZookeeper.deleteFromZookeeper(name)) {
            result.put("status",true);
            result.put("message","删除管理员成功");
        } else {
            result.put("status",false);
            result.put("message","删除管理员失败");
        }
        return JsonUtil.toJson(result);
    }

    @RequestMapping(value = "/userCenter",method = RequestMethod.GET)
    public ModelAndView userCenter(String name) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("user/user_center");
        UserBean user = UserToZookeeper.getUserFromZookeeper(name);
        mav.addObject("user", user);
        return mav;
    }

    @RequestMapping(value = "/updatePassword",method = RequestMethod.POST)
    @ResponseBody
    public String updatePassword(String name, String oldPassword, String newPassword){
        Map<String, Object> result = new HashMap<>();
        UserBean user = UserToZookeeper.getUserFromZookeeper(name);
        if (user == null) {
            result.put("status",false);
            result.put("message","请确认管理员名称");
        } else {
            //查到了用户，验证用户输入的旧密码是否正确
            if (MD5Util.string2MD5(oldPassword).toUpperCase().equals(user.getPassword())) {
                //验证密码通过，修改用户密码
                user.setPassword(MD5Util.string2MD5(newPassword).toUpperCase());
                if (UserToZookeeper.persistanceToZookeeper(user)) {
                    result.put("status",true);
                    result.put("message","修改密码成功");
                } else {
                    result.put("status",false);
                    result.put("message","修改密码失败");
                }
            } else {
                result.put("status",true);
                result.put("message","旧密码不正确");
            }
        }
        return JsonUtil.toJson(result);
    }
}
