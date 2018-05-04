/**
 * 
 */
package com.guttv.pm.platform.listener;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.guttv.pm.utils.Constants;

/**
 * @author Peter
 *
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class LoginFilter implements Filter {
	protected final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

	@Override
	public void destroy() {

	}

	private static String[] expPath = null;

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		if (checkRequestLogin(request)) {
			chain.doFilter(request, res);
		} else {
			response.sendRedirect(contextPath + Constants.LOGINURL);
		}
	}

	public boolean checkRequestLogin(HttpServletRequest request) {
		String uri = request.getRequestURI();

		if (StringUtils.isBlank(uri) || uri.equals("/") || uri.equals(contextPath) || uri.equals(contextPath + "/")) {
			return true;
		}

		if (uri.startsWith(contextPath + "/theme")) {
			return true;
		}

		if (uri.endsWith(".ico") || uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".gif")
				|| uri.endsWith(".png") || uri.endsWith(".jpg")) {
			return true;
		}

		if (request.getSession().getAttribute(Constants.CURRENT_USER) != null || containPath(uri)) {
			return true;
		} else {
			logger.warn("地址被拦截：" + uri);
			return false;
		}
	}

	private boolean containPath(String uri) {
		if (expPath == null || expPath.length == 0) {
			return false;
		}
		for (String s : expPath) {
			if (uri.endsWith(s)) {
				return true;
			}
		}
		return false;
	}

	private String contextPath = "";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		expPath = new String[] { "login", "logout", "index.jsp", "login.jsp", "checkUser", "kaptcha", "checkKaptcha",
				"ping" };
		contextPath = filterConfig.getServletContext().getContextPath();
	}
}
