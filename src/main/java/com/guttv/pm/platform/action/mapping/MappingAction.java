package com.guttv.pm.platform.action.mapping;

import com.guttv.pm.core.bean.ServerScriptMapping;
import com.guttv.pm.core.fp.ServerScriptMappingToZookeeper;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.utils.JsonUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author donghongchen
 * @create 2018-02-07 16:30
 **/
@Controller
@RequestMapping("/mapping")
public class MappingAction extends BaseAction {
    //ServerScriptMapping 的code目前策略是和Script 的code相同

    /**
     * @param code  script 的code
     * @return
     */
    @RequestMapping("/detail")
    public ModelAndView detail(@RequestParam("code") String code) {
        ModelAndView modelAndView = new ModelAndView("/mapping/detail");
        if (!"0".equals(code) && !"".equals(code)) {
            try {
                ServerScriptMapping entity = ServerScriptMappingToZookeeper.getFromZookeeper(code);
                modelAndView.addObject("entity", entity);
            } catch (Exception e) {
            	logger.error("获取数据异常：" + e.getMessage(), e);
            }
        }
        return modelAndView;
    }


    @PostMapping("/saveOrUpdate")
    @ResponseBody
    public String add(ServerScriptMapping bean) {
        Map<String, Object> data = new HashMap<>(8);
        bean.setCode(bean.getScriptCode());
        bean.setCreateTime(new Date());
        bean.setUpdateTime(new Date());
        try {
            ServerScriptMappingToZookeeper.persistanceToZookeeper(bean);
            data.put("status", true);
            data.put("message", "success");
        } catch (Exception e) {
            logger.error("saveOrUpdate数据异常：" + e.getMessage(), e);
            data.put("status", false);
            data.put("message", e.getMessage());
        }
        return JsonUtil.toJson(data);
    }


}
