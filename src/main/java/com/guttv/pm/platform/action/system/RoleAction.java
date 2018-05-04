package com.guttv.pm.platform.action.system;

import com.guttv.pm.core.bean.AuthBean;
import com.guttv.pm.core.bean.RoleBean;
import com.guttv.pm.core.fp.AuthToZookeeper;
import com.guttv.pm.core.fp.RoleToZookeeper;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.utils.JsonUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.management.relation.Role;
import java.util.*;


/**
 * 管理员页面Action
 */
@Controller
@RequestMapping("/Role")
public class RoleAction extends BaseAction{

    @RequestMapping("/list")
    public ModelAndView list(){
        ModelAndView mav = new ModelAndView();
        mav.setViewName("role/role_list");
        List<RoleBean> roleList = RoleToZookeeper.getAllRoleFromZookeeper();
        mav.addObject("roleList",roleList);
        return mav;
    }

    @RequestMapping("/getAllRole")
    @ResponseBody
    public String getAllRole(){
        List<RoleBean> roleList = RoleToZookeeper.getAllRoleFromZookeeper();
        return JsonUtil.toJson(roleList);
    }

    @RequestMapping(value = "/addView",method = RequestMethod.GET)
    public ModelAndView addView(){
        ModelAndView mav = new ModelAndView();
        List<AuthBean> authList = AuthToZookeeper.getAllAuthFromZookeeper(0L);
        List<AuthBean> authList2 = new ArrayList<>();
        if (authList != null && authList.size() > 0 ) {
            for (AuthBean authBean : authList) {
                List<AuthBean> auths = AuthToZookeeper.getAllAuthFromZookeeper(authBean.getId());
                authList2.add(authBean);
                if (auths != null && auths.size() > 0) {
                    authList2.addAll(auths);
                }
            }
        }
        mav.addObject("authList",authList2);
        mav.setViewName("role/role_add");
        return mav;
    }

    @RequestMapping(value = "/add")
    @ResponseBody
    public String add(@RequestBody RoleBean role){
        Map<String, Object> result = new HashMap<>();
        if(role.getId() == null || RoleToZookeeper.getRoleFromZookeeper(role.getId()) != null){
            result.put("status",false);
            result.put("message","id为空或者已经被使用");
        }else {
            role.setCreateTime(new Date());
            if (RoleToZookeeper.persistanceToZookeeper(role)) {
                result.put("status",true);
                result.put("message","新增角色成功");
            } else {
                result.put("status",false);
                result.put("message","新增角色失败");
            }
        }
        return JsonUtil.toJson(result);
    }

    @RequestMapping(value = "/updateView",method = RequestMethod.GET)
    public ModelAndView updateView(String id){
        ModelAndView mav = new ModelAndView();
        mav.setViewName("role/role_view");

        RoleBean role = RoleToZookeeper.getRoleFromZookeeper(Long.valueOf(id));
        mav.addObject("role", role);

        List<AuthBean> authList = AuthToZookeeper.getAllAuthFromZookeeper(0L);
        List<AuthBean> authList2 = new ArrayList<>();
        if (authList != null && authList.size() > 0 ) {
            for (AuthBean authBean : authList) {
                List<AuthBean> auths = AuthToZookeeper.getAllAuthFromZookeeper(authBean.getId());
                authList2.add(authBean);
                if (auths != null && auths.size() > 0) {
                    authList2.addAll(auths);
                }
            }
        }
        mav.addObject("authList",authList2);
        mav.setViewName("role/role_view");
        return mav;
    }

    @RequestMapping(value = "/update", method = {RequestMethod.POST})
    @ResponseBody
    public String update(@RequestBody RoleBean role){
        Map<String, Object> result = new HashMap<>();
        RoleBean oldRole = RoleToZookeeper.getRoleFromZookeeper(role.getId());
        if(role.getId() == null || oldRole == null){
            result.put("status",false);
            result.put("message","没有找到该角色");
        }else {
            role.setCreateTime(oldRole.getCreateTime());
            role.setUpdateTime(new Date());
            if (RoleToZookeeper.persistanceToZookeeper(role)) {
                result.put("status",true);
                result.put("message","更新角色成功");
            } else {
                result.put("status",false);
                result.put("message","更新角色失败");
            }
        }
        return JsonUtil.toJson(result);
    }

    @RequestMapping(value = "/get",method = RequestMethod.GET)
    public ModelAndView get(String id) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("role/role_view");
        RoleBean role = RoleToZookeeper.getRoleFromZookeeper(Long.valueOf(id));
        mav.addObject("role", role);
        return mav;
    }

    @RequestMapping(value = "/del",method = RequestMethod.GET)
    @ResponseBody
    public String del(String id){
        Map<String, Object> result = new HashMap<>();
        if (RoleToZookeeper.deleteFromZookeeper(Long.valueOf(id))) {
            result.put("status",true);
            result.put("message","删除角色成功");
        } else {
            result.put("status",false);
            result.put("message","删除角色失败");
        }
        return JsonUtil.toJson(result);
    }
}
