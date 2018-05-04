package com.guttv.pm.platform.action.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.guttv.pm.core.bean.AuthBean;
import com.guttv.pm.core.fp.AuthToZookeeper;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.utils.JsonUtil;

/**
 * 管理员页面Action
 */
@Controller
@RequestMapping("/Auth")
public class AuthAction extends BaseAction{

    @RequestMapping("/list")
    public ModelAndView list(){
        ModelAndView mav = new ModelAndView();
        mav.setViewName("auth/auth_list");
        List<AuthBean> authList = AuthToZookeeper.getAllAuthFromZookeeper(0L);
        mav.addObject("authList",authList);
        return mav;
    }

    @RequestMapping("/getSecondAuth")
    @ResponseBody
    public String getSecondAuth(String parentAuthId){
        List<AuthBean> secondAuthList = AuthToZookeeper.getAllAuthFromZookeeper(Long.valueOf(parentAuthId));
        return JsonUtil.toJson(secondAuthList);
    }

    @RequestMapping(value = "/add",method = RequestMethod.POST)
    @ResponseBody
    public String add(@RequestBody AuthBean auth){
        Map<String, Object> result = new HashMap<>();
        if(auth.getId() == null || AuthToZookeeper.getAuthFromZookeeper(auth.getId(),auth.getParentAuthId()) != null){
            result.put("status",false);
            result.put("message","id为空或者已经被使用");
        }else {
            if (AuthToZookeeper.persistanceToZookeeper(auth)) {
                result.put("status",true);
                result.put("message","新增权限成功");
            } else {
                result.put("status",false);
                result.put("message","新增权限失败");
            }
        }
        return JsonUtil.toJson(result);
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ResponseBody
    public String update(@RequestBody AuthBean auth){
        Map<String, Object> result = new HashMap<>();
        if(auth.getId() == null || AuthToZookeeper.getAuthFromZookeeper(auth.getId(), auth.getParentAuthId()) == null){
            result.put("status",false);
            result.put("message","没有找到该权限");
        }else {
            if (AuthToZookeeper.persistanceToZookeeper(auth)) {
                result.put("status",true);
                result.put("message","更新权限成功");
            } else {
                result.put("status",false);
                result.put("message","更新权限失败");
            }
        }
        return JsonUtil.toJson(result);
    }

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    public ModelAndView get(String id, String parentAuthId) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("auth/auth_view");
        Long pid;
        if (parentAuthId != null && !parentAuthId.equals("")){
            pid = Long.valueOf(parentAuthId);
        } else {
            pid = 0L;
        }
        AuthBean auth = null;
        if (id != null) {
            auth = AuthToZookeeper.getAuthFromZookeeper(Long.valueOf(id), pid);
        }
        mav.addObject("auth", auth);
        return mav;
    }

    @RequestMapping(value = "/del",method = RequestMethod.GET)
    @ResponseBody
    public String del(String id, String parentAuthId){
        Map<String, Object> result = new HashMap<>();
        Long pid;
        if (id != null && !id.equals("") && parentAuthId != null && !parentAuthId.equals("")){
            pid = Long.valueOf(parentAuthId);
        } else {
            pid = 0L;
        }
        if (id != null && AuthToZookeeper.deleteFromZookeeper(Long.valueOf(id), pid)) {
            result.put("status",true);
            result.put("message","删除权限成功");
        } else {
            result.put("status",false);
            result.put("message","删除权限失败");
        }
        return JsonUtil.toJson(result);
    }
}
