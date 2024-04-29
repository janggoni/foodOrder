package com.sharp.common.interceptor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.sharp.common.constant.CommonConstant;
import com.sharp.common.utils.CommonUtil;

import kr.msp.base.security.DefaultSecureAuth;
import kr.msp.base.security.SecureAuth;
import kr.msp.event.dto.RequestVo;
import kr.msp.event.manager.EventLogManager;

public class AppDefaultInterceptor extends HandlerInterceptorAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AppDefaultInterceptor.class);
	
	private SecureAuth secureAuth;
	
	@Value("${server.eventlog.save_path}")
	private String eventLogPath;
	
	@Value("${server.eventlog.use}")
	private boolean eventLogUse;
		
	@Value("${localLang}")
	private String localLang;
	
	@Autowired
	private ApplicationContext context;

	@Autowired
	LocaleResolver localeResolver;
	
	@Autowired
    private CommonUtil commonUtil;
	
	public void setSecureAuth(SecureAuth secureAuth) {
		this.secureAuth = secureAuth;
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		LOGGER.info("[ # preHandle - 1 # ] " + this.eventLogUse);
		Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		try {
			if (this.eventLogUse) {
				EventLogManager.getInstance().setEventLogPath(this.eventLogPath);
				if (!EventLogManager.getInstance().isStarted()) {
					EventLogManager.getInstance().start();
				}
			}

			Class clazz = handler.getClass();
			LOGGER.info(
					"[ # preHandle of " + clazz.getCanonicalName() + "." + Arrays.toString(clazz.getMethods()) + "# ]");
			String requestMethod = StringUtils.defaultString(request.getMethod());
			if (requestMethod.equalsIgnoreCase(HttpMethod.POST.toString())
					|| requestMethod.equalsIgnoreCase(HttpMethod.PUT.toString())) {

				String encyn = StringUtils.defaultString(request.getHeader("user_data_enc"), "n");
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Encryption : [" + encyn + "]");
				}

    			Map<String, Object> bodyMap = new HashMap<>();
    			
				if (StringUtils.equals(encyn, "y")) {
					String encmodule = request.getHeader("user_enc_name");
					if (StringUtils.equals(encmodule, "UracleSE")) {
						bodyMap = this.secureAuth.requestHandle(request, response);
					} else {
						bodyMap = this.secureAuth.requestHandle(request, response);
					}
				} else {
					DefaultSecureAuth defaultSecureAuth = new DefaultSecureAuth();
					bodyMap = defaultSecureAuth.requestHandle(request, response);
				}

				//다국어 코드 입력받아서 다국어 코드 설정 처리
	        	LOGGER.info("[ # AppDefaultInterceptor - setLocale] ========================================= start");	     
				LOGGER.info("[ # AppDefaultInterceptor - setLocale] 1. localeResolver.getLanguage(): " + localeResolver.resolveLocale(request).getLanguage() );	   	
	        	
				Map<String, Object> headerMap = new HashMap<>();
				headerMap.putAll( (Map<String, Object>)bodyMap.get("head") );
				
    			Map<String, Object> bodyJosnMap = new HashMap<>();
    			bodyJosnMap.putAll( (Map<String, Object>)bodyMap.get("body") );
    			
				setLocale(request, response, headerMap);
				
    			//다국어 코드 header-> body에 “locale_langueage” 값으로 추가
    			String localeLang = commonUtil.nvl(headerMap.get(CommonConstant.LOCALE_LANGUEAGE), localeResolver.resolveLocale(request).getLanguage());
    			bodyJosnMap.put("LOCALE", localeLang);
    			bodyMap.put("body", bodyJosnMap);
	        	
	        	JSONObject mspData = new JSONObject(bodyMap);
		        request.setAttribute("http-body", 	bodyMap);
		        request.setAttribute("head",		bodyMap.get("head"));
		        request.setAttribute("body",		bodyMap.get("body"));
		        request.setAttribute("rest_uri_path_att", pathVariables);
			}
		} catch (Exception var10) {
			LOGGER.error("error", var10);
			response.sendError(500);
			return false;
		}

		LOGGER.info("[ # preHandle - 2 # ]");
		return true;
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		LOGGER.info("[ # postHandle - 1 # ]");
		Map<String, Object> requestMap = null;
		Map<String, Object> requestHeadMap = null;
		String requestMethod = StringUtils.defaultString(request.getMethod());
		if (!requestMethod.equalsIgnoreCase(HttpMethod.POST.toString())
				&& !requestMethod.equalsIgnoreCase(HttpMethod.PUT.toString())) {
			requestMap = new HashMap();
			((Map) requestMap).put("head", new HashMap());
		} else {
			requestMap = (Map) request.getAttribute("http-body");
		}

		requestHeadMap = MapUtils.getMap((Map) requestMap, "head");
		Map<String, Object> responseMap = new HashMap();
		if (modelAndView != null) {
			responseMap = modelAndView.getModel();
		}

		Map<String, Object> responseHeadMap = MapUtils.getMap((Map) responseMap, "head");
		Object resultCodeObj = MapUtils.getObject((Map) responseHeadMap, "result_code");
		String resultCode = "";
		if (resultCodeObj == null) {
			resultCode = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());
			if (MapUtils.isEmpty((Map) responseHeadMap)) {
				responseHeadMap = new HashMap();
			}

			((Map) responseHeadMap).put("result_code", resultCode);
			((Map) responseHeadMap).put("result_msg", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
			((Map) responseMap).put("head", responseHeadMap);
		} else {
			resultCode = (String) resultCodeObj;
		}

		if (requestMethod.equalsIgnoreCase(HttpMethod.POST.toString())
				|| requestMethod.equalsIgnoreCase(HttpMethod.PUT.toString())) {
			ObjectMapper objectMapper = new ObjectMapper();
			String bodyString = objectMapper.writeValueAsString(requestMap);
			LOGGER.info("[ # postHandle - DEVICE REQUEST# ]=[" + bodyString + "]");
			if (MapUtils.isNotEmpty(requestHeadMap) && this.eventLogUse) {
				RequestVo requestVo = new RequestVo(bodyString, request);
				requestVo.setUserCompCode(request.getRequestURI().replaceAll(request.getContextPath(), ""));
				requestVo.getHead().setAppId((String) requestHeadMap.get("appid"));
				EventLogManager.getInstance().access(request, requestVo);
			}
		}

		try {
			this.secureAuth.responseHandle(request, response, (Map) responseMap);
		} catch (Exception var15) {
			response.sendError(417);
			LOGGER.error("Exception occurred.", var15);
		}

		LOGGER.info("[ # postHandle - 2 # ]");
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		LOGGER.info("[ # afterCompletion # ]");
	}


	public void setLocale(HttpServletRequest request, HttpServletResponse response, Map<String, Object> headerMap) {
		
		if(headerMap != null && !commonUtil.isEmpty(headerMap)){

			String localeLang = commonUtil.nvl(headerMap.get(CommonConstant.LOCALE_LANGUEAGE),	localeResolver.resolveLocale(request).getLanguage());
			
			if(localeLang != null && !commonUtil.isEmpty(localeLang)){
				
				Locale locale = localeResolver.resolveLocale(request);
				String langCode = locale.getLanguage();
				
				if(!langCode.equals(localeLang)) {
										
					locale = new Locale(localeLang);    
					localeResolver.setLocale(request, response, locale);
					
				}
			}
		}
		
	}
}