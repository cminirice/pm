package com.guttv.pm.platform.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.guttv.pm.utils.Constants;

@Controller
@RequestMapping("/logout")
public class LogoutAction {

	@RequestMapping(value = "", name = "退出", method = RequestMethod.GET)
	public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		request.getSession().removeAttribute(Constants.INPUT_WRONG_COUNT);
		request.getSession().removeAttribute(Constants.CURRENT_USER);
		response.sendRedirect(request.getServletContext().getContextPath() + Constants.LOGINURL);
		return null;
	}
}
