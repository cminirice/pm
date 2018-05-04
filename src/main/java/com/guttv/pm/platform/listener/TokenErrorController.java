package com.guttv.pm.platform.listener;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenErrorController extends BasicErrorController {
	public TokenErrorController() {
		super(new DefaultErrorAttributes(), new ErrorProperties());
	}

	private static final String PATH = "/error";

	@RequestMapping(produces = { MediaType.APPLICATION_JSON_VALUE })
	public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
		Map<String, Object> body = getErrorAttributes(request, isIncludeStackTrace(request, MediaType.ALL));
		HttpStatus status = getStatus(request);
		if (StringUtils.isNotBlank((String) body.get("exception"))) {
			body.put("status", HttpStatus.FORBIDDEN.value());
			status = HttpStatus.FORBIDDEN;
		}
		return new ResponseEntity<Map<String, Object>>(body, status);
	}

	@Override
	public String getErrorPath() {
		return PATH;
	}
}
