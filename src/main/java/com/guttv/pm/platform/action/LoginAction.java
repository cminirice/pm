/**
 * 
 */
package com.guttv.pm.platform.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.guttv.pm.core.bean.AuthBean;
import com.guttv.pm.core.bean.RoleBean;
import com.guttv.pm.core.bean.UserBean;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.fp.AuthToZookeeper;
import com.guttv.pm.core.fp.RoleToZookeeper;
import com.guttv.pm.core.fp.UserToZookeeper;
import com.guttv.pm.utils.Constants;

/**
 * @author Peter
 *
 */
@Controller
@RequestMapping("/login")
public class LoginAction extends BaseAction {

	@Autowired
	private DefaultKaptcha defaultKaptcha = null;

	@RequestMapping(value = "", name = "登陆", method = RequestMethod.GET)
	public String login() throws Exception {
		return "index";
	}

	@RequestMapping("/checkUser")
	public String checkUser(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String userName = request.getParameter("username");
		String password = request.getParameter("password");
		String defUser = ConfigCache.getInstance().getProperty(Constants.USER_NAME_CONFIG, null);
		if (StringUtils.isNotBlank(defUser) && defUser.equalsIgnoreCase(userName)) {
			String defPwd = ConfigCache.getInstance().getProperty(Constants.USER_PWD_CONFIG, null);
			if (StringUtils.isBlank(defPwd)) {
				// 如果配置的登陆密码为空，则表示可以无密登陆
				UserBean user = new UserBean();
				user.setName(userName);
				request.getSession().setAttribute(Constants.CURRENT_USER, user);
				request.getSession().removeAttribute(Constants.INPUT_WRONG_COUNT);
				request.getSession().removeAttribute(Constants.CHECKKAPTCHA);
				request.removeAttribute("errMsg");
			} else if (defPwd.equalsIgnoreCase(password)) {
				// 验证正确
				UserBean user = new UserBean();
				user.setName(userName);
				request.getSession().setAttribute(Constants.CURRENT_USER, user);
				//把全部权限都放在admin中
				List<AuthBean> authList = AuthToZookeeper.getAllAuthFromZookeeper(0L);
				Set<Long> auths = new HashSet<>();
                List<AuthBean> authList2 = new ArrayList<>();
				if (authList != null) {
					for (AuthBean authBean : authList) {
                        auths.add(authBean.getId());
                        //二级权限
                        authList2 = AuthToZookeeper.getAllAuthFromZookeeper(authBean.getId());
                        if (authList2 != null) {
                            for (AuthBean authBean2 : authList2) {
                                auths.add(authBean2.getId());
                            }
                        }
                    }
				}
				request.getSession().setAttribute("auths", auths);
				request.getSession().removeAttribute(Constants.INPUT_WRONG_COUNT);
				request.getSession().removeAttribute(Constants.CHECKKAPTCHA);
				request.removeAttribute("errMsg");
			} else {
				addInputWrongCount(request);
				request.setAttribute("errMsg", "使用默认用户，但密码错误");
				return "login";
			}
		} else {
			//登陆验证
			UserBean zkuser = UserToZookeeper.getUserFromZookeeper(userName);
			if (zkuser != null) {
				String pwdMD5 = zkuser.getPassword();
				if (password != null && !"".equals(password) && pwdMD5.equals(password)) {
					// 验证正确
					request.getSession().setAttribute(Constants.CURRENT_USER, zkuser);
					RoleBean roleBean = RoleToZookeeper.getRoleFromZookeeper(zkuser.getRoleId());
					if (roleBean != null) {
						request.getSession().setAttribute("auths",roleBean.getAuths());
					}
					request.getSession().removeAttribute(Constants.INPUT_WRONG_COUNT);
					request.getSession().removeAttribute(Constants.CHECKKAPTCHA);
					request.removeAttribute("errMsg");
				} else {
					addInputWrongCount(request);
					request.setAttribute("errMsg", "密码错误");
					return "login";
				}
			} else {
				addInputWrongCount(request);
				request.setAttribute("errMsg", "用户不存在：" + userName);
				return "login";
			}
		}

		response.sendRedirect(request.getServletContext().getContextPath() + Constants.MAIN_URL);
		return null;
	}

	private void addInputWrongCount(HttpServletRequest request) {
		Integer wrongCount = (Integer) request.getSession().getAttribute(Constants.INPUT_WRONG_COUNT);
		if (wrongCount == null) {
			wrongCount = 0;
		}
		wrongCount = wrongCount + 1;
		int maxTime = ConfigCache.getInstance().getProperty(Constants.MAX_LOGIN_ERROR_TIME, 3);
		if(wrongCount>maxTime) {
			request.getSession().setAttribute(Constants.CHECKKAPTCHA, true);
		}
		
		request.getSession().setAttribute(Constants.INPUT_WRONG_COUNT, wrongCount);
	}

	@RequestMapping("/kaptcha")
	public void defaultKaptcha(HttpServletRequest request, HttpServletResponse response) throws Exception {
		byte[] captchaChallengeAsJpeg = null;
		ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
		ServletOutputStream responseOutputStream = null;
		try {
			// 生产验证码字符串并保存到session中
			String createText = defaultKaptcha.createText();
			request.getSession().setAttribute("j_captcha", createText);
			// 使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
			BufferedImage challenge = defaultKaptcha.createImage(createText);
			ImageIO.write(challenge, "jpg", jpegOutputStream);

			// 定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
			captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
			response.setHeader("Cache-Control", "no-store");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);
			response.setContentType("image/jpeg");
			responseOutputStream = response.getOutputStream();
			responseOutputStream.write(captchaChallengeAsJpeg);
			responseOutputStream.flush();

		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} finally {
			IOUtils.closeQuietly(jpegOutputStream);
			IOUtils.closeQuietly(responseOutputStream);
		}
	}

	@RequestMapping("/checkKaptcha")
	@ResponseBody
	public String checkKaptcha(HttpServletRequest request, HttpServletResponse response) {
		String captchaId = (String) request.getSession().getAttribute("j_captcha");
		String parameter = request.getParameter("j_captcha");

		if (StringUtils.isBlank(captchaId) || !captchaId.equalsIgnoreCase(parameter)) {
			return "error";
		} else {
			return "success";
		}
	}
}
