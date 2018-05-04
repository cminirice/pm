/**
 * 
 */
package com.guttv.pm.core.task;

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
import org.springframework.stereotype.Component;

import com.guttv.pm.utils.Constants;

/**
 * @author Peter
 *
 */
@Component
public class RestTaskFilter implements Filter {
	protected static final Logger logger = LoggerFactory.getLogger(RestTaskFilter.class);
	private String pathPatter = null;

	private String contentPath = null;

	private boolean contentPathIsBlank = true;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		contentPath = filterConfig.getServletContext().getContextPath();
		contentPathIsBlank = StringUtils.isBlank(contentPath);
		pathPatter = contentPath + "/rest/";

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String uri = req.getRequestURI();

		if (uri.startsWith(pathPatter)) {
			if (!contentPathIsBlank) {
				uri = uri.substring(contentPath.length());
			}
			try {
				logger.debug("处理[" + req.getRemoteAddr() + "]请求：" + uri);
				RestTaskHandler.handle(uri, req, res);
			} catch (Throwable e) {
				logger.error("处理地址[" + uri + "]异常：" + e.getMessage(), e);
				response.setCharacterEncoding(Constants.ENCODING);
				res.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {

	}

}
