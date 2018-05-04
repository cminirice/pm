/**
 * 
 */
package com.guttv.pm.platform.listener;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.guttv.pm.core.bean.UserBean;
import com.guttv.pm.utils.Constants;

/**
 * @author Peter
 *
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLogFilter implements Filter{

	protected final Logger logger = LoggerFactory.getLogger("request");
	
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		request.setCharacterEncoding(Constants.ENCODING);
		HttpSession session = request.getSession();
		UserBean user = (UserBean)session.getAttribute(Constants.CURRENT_USER);
		String userName = null;
		if(user != null) {
			userName = user.getName();
		}
		
		StringBuilder sb = new StringBuilder();
		Enumeration<String> keys = request.getParameterNames();
		if(keys != null) {
			String key = null;
			while(keys.hasMoreElements()) {
				key = keys.nextElement();
				sb.append(key).append("=").append(request.getParameter(key)).append("&");
			}
		}
		
		logger.info(request.getRequestURI()+"\tqueryParam:"+sb+"\tfromip:"+request.getRemoteHost()+"\t loginname:" + userName);
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
