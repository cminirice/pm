/**
 * 
 */
package com.guttv.pm.core.task;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ErrorsMethodArgumentResolver;
import org.springframework.web.method.annotation.ExpressionValueMethodArgumentResolver;
import org.springframework.web.method.annotation.MapMethodProcessor;
import org.springframework.web.method.annotation.ModelAttributeMethodProcessor;
import org.springframework.web.method.annotation.ModelMethodProcessor;
import org.springframework.web.method.annotation.RequestHeaderMapMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestParamMapMethodArgumentResolver;
import org.springframework.web.method.annotation.RequestParamMethodArgumentResolver;
import org.springframework.web.method.annotation.SessionStatusMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.CallableMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.DeferredResultMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.HttpHeadersReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.MatrixVariableMapMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.MatrixVariableMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ModelAndViewMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMapMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RedirectAttributesMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestAttributeMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestPartMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ServletCookieValueMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.ServletModelAttributeMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestDataBinderFactory;
import org.springframework.web.servlet.mvc.method.annotation.ServletRequestMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletResponseMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.SessionAttributeMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBodyReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.UriComponentsBuilderMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ViewMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.ViewNameMethodReturnValueHandler;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UrlPathHelper;

import com.google.gson.Gson;
import com.guttv.pm.support.ann.Writeable;
import com.guttv.pm.utils.Constants;

/**
 * 调度处理
 * 
 * @author Peter
 *
 */
public class RestTaskHandler {
	private static Logger logger = LoggerFactory.getLogger(RestTaskHandler.class);
	private static HandlerMethodReturnValueHandlerComposite returnValueHandlers;
	private static HandlerMethodArgumentResolverComposite argumentResolvers;
	private static ContentNegotiationManager contentNegotiationManager = new ContentNegotiationManager();
	private static List<HttpMessageConverter<?>> messageConverters;
	private static List<Object> requestResponseBodyAdvice = new ArrayList<Object>();
	private static ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
	private static WebDataBinderFactory binderFactory = new ServletRequestDataBinderFactory(null, null);
	private static UrlPathHelper urlPathHelper = new UrlPathHelper();

	static {
		StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(
				Charset.forName(Constants.ENCODING));
		stringHttpMessageConverter.setWriteAcceptCharset(false); // see SPR-7316
		messageConverters = new ArrayList<HttpMessageConverter<?>>(4);
		messageConverters.add(new ByteArrayHttpMessageConverter());
		messageConverters.add(stringHttpMessageConverter);
		messageConverters.add(new SourceHttpMessageConverter<Source>());
		messageConverters.add(new AllEncompassingFormHttpMessageConverter());
		messageConverters.add(new JsonHttpMessageConverter());

		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>();

		resolvers.add(new RequestParamMethodArgumentResolver(null, false));
		resolvers.add(new RequestParamMapMethodArgumentResolver());
		resolvers.add(new PathVariableMethodArgumentResolver());
		resolvers.add(new PathVariableMapMethodArgumentResolver());
		resolvers.add(new MatrixVariableMethodArgumentResolver());
		resolvers.add(new MatrixVariableMapMethodArgumentResolver());
		resolvers.add(new ServletModelAttributeMethodProcessor(false));
		resolvers.add(new RequestResponseBodyMethodProcessor(messageConverters, requestResponseBodyAdvice));
		resolvers.add(new RequestPartMethodArgumentResolver(messageConverters, requestResponseBodyAdvice));
		resolvers.add(new RequestHeaderMethodArgumentResolver(null));
		resolvers.add(new RequestHeaderMapMethodArgumentResolver());
		resolvers.add(new ServletCookieValueMethodArgumentResolver(null));
		resolvers.add(new ExpressionValueMethodArgumentResolver(null));
		resolvers.add(new SessionAttributeMethodArgumentResolver());
		resolvers.add(new RequestAttributeMethodArgumentResolver());

		// Type-based argument resolution
		resolvers.add(new ServletRequestMethodArgumentResolver());
		resolvers.add(new ServletResponseMethodArgumentResolver());
		resolvers.add(new HttpEntityMethodProcessor(messageConverters, requestResponseBodyAdvice));
		resolvers.add(new RedirectAttributesMethodArgumentResolver());
		resolvers.add(new ModelMethodProcessor());
		resolvers.add(new MapMethodProcessor());
		resolvers.add(new ErrorsMethodArgumentResolver());
		resolvers.add(new SessionStatusMethodArgumentResolver());
		resolvers.add(new UriComponentsBuilderMethodArgumentResolver());

		// Catch-all
		resolvers.add(new RequestParamMethodArgumentResolver(null, true));
		resolvers.add(new ServletModelAttributeMethodProcessor(true));
		argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);

		List<HandlerMethodReturnValueHandler> handlers = new ArrayList<HandlerMethodReturnValueHandler>();

		// 发送到框架消息队列
		handlers.add(new QueueReturnValueHandler());

		// Single-purpose return value types
		handlers.add(new ModelAndViewMethodReturnValueHandler());
		handlers.add(new ModelMethodProcessor());
		handlers.add(new ViewMethodReturnValueHandler());
		handlers.add(new StreamingResponseBodyReturnValueHandler());
		handlers.add(new HttpHeadersReturnValueHandler());
		handlers.add(new CallableMethodReturnValueHandler());
		handlers.add(new DeferredResultMethodReturnValueHandler());

		// Annotation-based return value types
		handlers.add(new RequestResponseBodyMethodProcessor(messageConverters, contentNegotiationManager,
				requestResponseBodyAdvice));

		// Multi-purpose return value types
		handlers.add(new ViewNameMethodReturnValueHandler());
		handlers.add(new MapMethodProcessor());

		// Catch-all
		handlers.add(new ModelAttributeMethodProcessor(true));
		returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
	}

	public static void handle(String uri, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ServletWebRequest webRequest = new ServletWebRequest(request, response);
		try {

			RestPathMapping.PathMappingInfo mapping = RestPathMapping.getInstance().getHandleMethod(uri);

			if (mapping == null) {
				response.setCharacterEncoding(Constants.ENCODING);
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "没有找到路径[" + uri + "]的处理方法");
				return;
			}

			if (mapping.getTask().isFinished()) {
				response.setCharacterEncoding(Constants.ENCODING);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, "请求路径[" + uri + "]已经结束");
				RestPathMapping.getInstance().uncacheHandlerMethod(mapping.getTask());
				return;
			}

			HandlerMethod handlerMethod = mapping.getHandlerMethod();

			ServletInvocableHandlerMethod invocableMethod = new ServletInvocableHandlerMethod(handlerMethod);
			invocableMethod.setHandlerMethodArgumentResolvers(argumentResolvers);
			invocableMethod.setHandlerMethodReturnValueHandlers(returnValueHandlers);
			invocableMethod.setDataBinderFactory(binderFactory);
			invocableMethod.setParameterNameDiscoverer(parameterNameDiscoverer);

			ModelAndViewContainer mavContainer = new ModelAndViewContainer();
			mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
			mavContainer.setView(mapping);

			// 处理用PathVariable注解的参数
			String bestPattern = mapping.getPath();
			Map<String, String> uriVariables = RestPathMapping.getInstance().getPathMatcher()
					.extractUriTemplateVariables(bestPattern, uri);
			if (uriVariables != null && uriVariables.size() > 0) {
				Map<String, String> decodedUriVariables = urlPathHelper.decodePathVariables(request, uriVariables);
				request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, bestPattern);
				request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, decodedUriVariables);
			}

			// 处理消费者数据类型
			String[] producers = mapping.getRequestMapping().produces();
			if (producers != null && producers.length > 0) {
				Set<MediaType> mediaTypes = new HashSet<MediaType>();
				for (String producer : producers) {
					mediaTypes.add(MediaType.parseMediaType(producer));
				}
				request.setAttribute(HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE, mediaTypes);
			}

			logger.debug("处理[" + request.getRemoteAddr() + "]请求：" + uri);
			invocableMethod.invokeAndHandle(webRequest, mavContainer);
		} finally {
			webRequest.requestCompleted();
		}
	}

	/**
	 * 往框架的队列里写返回值
	 * 
	 * @author Peter
	 *
	 */
	private static class QueueReturnValueHandler implements HandlerMethodReturnValueHandler {

		@Override
		public boolean supportsReturnType(MethodParameter returnType) {
			return returnType.getMethod().getAnnotation(Writeable.class) != null;
		}

		@Override
		public void handleReturnValue(Object returnValue, MethodParameter returnType,
				ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws Exception {
			if (returnValue == null) {
				return;
			}

			// 在taskhandler里执行的时候设置的此置
			RestPathMapping.PathMappingInfo mapping = (RestPathMapping.PathMappingInfo) mavContainer.getView();

			// 在任务缓存里按代理类找到任务
			AbstractTask task = mapping.getTask();
			logger.debug("准备写数据：" + returnValue);
			task.writeData(returnValue);
		}
	}

	/**
	 * 对象转json返回
	 * 
	 * @author Peter
	 *
	 */
	private static class JsonHttpMessageConverter implements HttpMessageConverter<Object> {

		private Gson gson = new Gson();

		/**
		 * 只处理非原始类和字符串
		 */
		@Override
		public boolean canWrite(Class<?> clazz, MediaType mediaType) {
			return !(clazz.isPrimitive() || clazz.equals(String.class));
		}

		@Override
		public List<MediaType> getSupportedMediaTypes() {
			return Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8);
		}

		@Override
		public boolean canRead(Class<?> clazz, MediaType mediaType) {
			return false;
		}

		@Override
		public Object read(Class<?> clazz, HttpInputMessage inputMessage)
				throws IOException, HttpMessageNotReadableException {
			return null;
		}

		@Override
		public void write(final Object t, MediaType contentType, HttpOutputMessage outputMessage)
				throws IOException, HttpMessageNotWritableException {

			final HttpHeaders headers = outputMessage.getHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			if (outputMessage instanceof StreamingHttpOutputMessage) {
				StreamingHttpOutputMessage streamingOutputMessage = (StreamingHttpOutputMessage) outputMessage;
				streamingOutputMessage.setBody(new StreamingHttpOutputMessage.Body() {
					@Override
					public void writeTo(final OutputStream outputStream) throws IOException {
						writeInternal(gson.toJson(t), new HttpOutputMessage() {
							@Override
							public OutputStream getBody() throws IOException {
								return outputStream;
							}

							@Override
							public HttpHeaders getHeaders() {
								return headers;
							}
						});
					}
				});
			} else {
				writeInternal(gson.toJson(t), outputMessage);
				outputMessage.getBody().flush();
			}
		}

		protected void writeInternal(String json, HttpOutputMessage outputMessage) throws IOException {
			Charset charset = MediaType.APPLICATION_JSON_UTF8.getCharset();
			StreamUtils.copy(json, charset, outputMessage.getBody());
		}
	}
}
