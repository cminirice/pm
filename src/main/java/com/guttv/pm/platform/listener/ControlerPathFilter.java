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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * 
 * 本filter是为了在打包执行的时候，系统会在路径 contextpath 和 prifix之间添加模块的路径， 例如
 * contextPath=pm,prefix=/view/
 * 在程序的list方法中返回/component/list，在打包运行的时候，会生成/pm/component/view/component/list.
 * jsp； 注意 /pm/component/view/ 中间的 component是多余的，本类就是要把这个东东去掉
 *
 * 
 * @author Peter
 *
 */
//@Component
public class ControlerPathFilter implements Filter {
	protected final Logger logger = LoggerFactory.getLogger("request");
	private String prefix = null;
	private boolean isBlankPrefix = true;
	private String contextPath = null;
	private boolean isBlankContextPath = true;
	private static final String pathSeperator = "/";

	//@Autowired
	private Environment env = null;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		prefix = env.getProperty("spring.mvc.view.prefix", "");
		isBlankPrefix = StringUtils.isBlank(prefix);
		contextPath = filterConfig.getServletContext().getContextPath();
		isBlankContextPath = StringUtils.isBlank(contextPath);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String uri = ((HttpServletRequest) request).getRequestURI();

		// 两个都是空的情况，暂时返回
		if (isBlankPrefix && isBlankContextPath) {
			chain.doFilter(request, response);
			return;
		}

		int indexBoth = uri.indexOf(contextPath + prefix);

		// 路径正常的情况
		if (indexBoth == 0) {
			chain.doFilter(request, response);
			return;
		}

		// 防止有路径前缀与contextPath一样，例如contextPath 是 /pm，有个路径是/pmlist
		int indexContextPath = uri.indexOf(contextPath + pathSeperator);

		// 还没有加上contextPath的时候，直接返回
		if (!isBlankContextPath && indexContextPath < 0) {
			chain.doFilter(request, response);
			return;
		}

		int indexPrefix = uri.indexOf(prefix);

		// 还没有加上prefix的时候，直接返回
		if (!isBlankPrefix && indexPrefix < 0) {
			chain.doFilter(request, response);
			return;
		}

		String[] paths = uri.split(pathSeperator);
		if (paths == null || paths.length < 3) {
			// 这种情况都可以直接返回了
			chain.doFilter(request, response);
			return;
		}

		StringBuilder sb = new StringBuilder(contextPath);
		boolean findPrefix = false;
		for (int i = 1; i < paths.length; i++) {
			if (findPrefix) {
				sb.append(pathSeperator).append(paths[i]);
				continue;
			}

			if (!findPrefix) {
				if (prefix.equals(pathSeperator + paths[i] + pathSeperator)) {
					findPrefix = true;
					sb.append(pathSeperator).append(paths[i]);
				} else {
					// 没有找到prefix前的都丢掉
					continue;
				}
			}
		}
		
		logger.info("请求[" + uri + "]修改后的路径：" + sb);
		request.getRequestDispatcher(sb.toString()).forward(request, response);
	}

	@Override
	public void destroy() {

	}

}
