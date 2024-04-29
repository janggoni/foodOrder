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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import com.sharp.common.constant.CommonConstant;
import com.sharp.common.constant.ParamConstant;
import com.sharp.common.utils.CommonUtil;
import com.sharp.common.utils.JwtTokenUtils;

import io.jsonwebtoken.Claims;
import kr.msp.base.security.DefaultSecureAuth;
import kr.msp.base.security.SecureAuth;
import kr.msp.constant.Const;
import kr.msp.event.dto.RequestVo;
import kr.msp.event.manager.EventLogManager;

public class AppApiInterceptor extends HandlerInterceptorAdapter{
	private static final Logger LOGGER = LoggerFactory.getLogger(AppApiInterceptor.class);

	@Autowired 
    private JwtTokenUtils jwtTokenUtils;

	@Autowired
    private CommonUtil commonUtil;
	
	private SecureAuth secureAuth;
	
	@Value("${server.eventlog.save_path}")
	private String eventLogPath;
	  
	@Value("${server.eventlog.use}")
	private boolean eventLogUse;
	
	@Value("${token.useAt}")
	private String tokenUseAt;

	@Value("${localLang}")
	private String localLang;
	
	@Autowired(required = true)
	private MessageSource messageSource;
	
	@Autowired
	private ApplicationContext context;
  
	public void setSecureAuth(SecureAuth secureAuth) {
		this.secureAuth = secureAuth;
	}

	@Autowired
	LocaleResolver localeResolver;
		
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		LOGGER.info("[ # AppApiInterceptor - preHandle - 1 # ] " + this.eventLogUse);
	    Map pathVariables = (Map)request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

	    boolean tokenCheck = true;
	    
	    try {
	    		if (this.eventLogUse) {
	    			EventLogManager.getInstance().setEventLogPath(this.eventLogPath);
	    			
	    			if (!EventLogManager.getInstance().isStarted())
	    				EventLogManager.getInstance().start(); 
	    			
	    		} 
	    			    		
	    		
	    		Class<?> clazz = handler.getClass();
	    		LOGGER.info("[ # AppApiInterceptor - preHandle of " + clazz.getCanonicalName() + "." + Arrays.toString((Object[])clazz.getMethods()) + "# ]");
	    		String requestMethod = StringUtils.defaultString(request.getMethod());
	    		if (requestMethod.equalsIgnoreCase(HttpMethod.POST.toString()) || requestMethod.equalsIgnoreCase(HttpMethod.PUT.toString())) {
	    			
	    			Map<String, Object> bodyMap = new HashMap<>();
		        	
	    			DefaultSecureAuth defaultSecureAuth = new DefaultSecureAuth();
		        	bodyMap = defaultSecureAuth.requestHandle(request, response);
		        	
		        	//다국어 코드 입력받아서 다국어 코드 설정 처리
		        	LOGGER.info("[ # AppApiInterceptor - setLocale] ========================================= start");

		        	Map<String, Object> headerMap = new HashMap<>();
		        	headerMap.putAll( (Map<String, Object>)bodyMap.get("head") );
	    			
	    			Map<String, Object> bodyJosnMap = new HashMap<>();
	    			bodyJosnMap.putAll( (Map<String, Object>)bodyMap.get("body") );
	    			
		        	setLocale(request, response, headerMap);
	    			
	    			//다국어 코드 header-> body에 추가 “locale_langueage” 값으로 추가
	    			String localeLang = commonUtil.nvl(headerMap.get(CommonConstant.LOCALE_LANGUEAGE), localeResolver.resolveLocale(request).getLanguage());
	    			bodyJosnMap.put("LOCALE", localeLang);
	    			bodyMap.put("body", bodyJosnMap);

		        	LOGGER.info("[ # AppApiInterceptor - setLocale] ========================================= end");
		        	
		        	//로그인시 토큰 사용여부 확인 : native 작업 후 사용여부 확인 부분 삭제 예정
		        	if(tokenUseAt.equals(CommonConstant.AT_Y)) {
		    			String accessToken = StringUtils.defaultString(request.getHeader(ParamConstant.HEADER_ACCESS_TOKEN), "");
		    			String refreshToken = StringUtils.defaultString(request.getHeader(ParamConstant.HEADER_REFRESH_TOKEN), "");
		    					    			
		    			//accessToken 존재 검사
		    			if(!commonUtil.nullCheck(accessToken)) {
		    				headerMap.put(Const.RESULT_CODE,		"600");
		    				headerMap.put(Const.RESULT_MESSAGE, 	commonUtil.getMessage("600")	);		    				
				        	tokenCheck = false;
				        	
				        //refresh token 존재 검사	
				        }else if(!commonUtil.nullCheck(refreshToken)) {
		    				headerMap.put(Const.RESULT_CODE,		"610");
		    				headerMap.put(Const.RESULT_MESSAGE, 	commonUtil.getMessage("610")	);
				        	tokenCheck = false;
				        	
				        }else {
			    			// access token의 유효성 및 만료 기간 검사
			    			if(!jwtTokenUtils.validateToken(accessToken)) {
			    				headerMap.put(Const.RESULT_CODE,		"601");
			    				headerMap.put(Const.RESULT_MESSAGE, 	commonUtil.getMessage("601")	);
					        	tokenCheck = false;
					        
					        // refresh token의 유효성 및 만료 기간 검사
			    			}else if(!jwtTokenUtils.validateToken(refreshToken)) {
			    				headerMap.put(Const.RESULT_CODE,		"611");
			    				headerMap.put(Const.RESULT_MESSAGE, 	commonUtil.getMessage("611")	);
						        	tokenCheck = false;
						        	
			    			}else {
			    				try {
				    				// accessToken의 uuid와 body의 uuid와 일치하는지 확인
				    				Claims claims = jwtTokenUtils.getInformation(accessToken);
				    				
				    				LOGGER.info("[ # preHandle - 2 # ] claims uuid =" + claims.get("uuid"));
				    				String uuid = commonUtil.nvlString(claims.get("uuid"));
				    				
				    				if(!commonUtil.nullCheck(uuid)) {
					    				headerMap.put(Const.RESULT_CODE,		"600");
					    				headerMap.put(Const.RESULT_MESSAGE, 	commonUtil.getMessage("600")	);
							        	tokenCheck = false;
				    				}else {
				    					tokenCheck = true;
				    				}
			    				}catch(Exception e) {
			    			    	LOGGER.error("error", e);
			    					tokenCheck = false;
			    				}
			    			}
				        }
		    				    			
		    			bodyMap.put("head", headerMap);
	    			}
		        	
			        if(!tokenCheck) {
			    		JSONObject resultJSON = new JSONObject();
			    		resultJSON.put("head", bodyMap.get("head"));
			    		resultJSON.put("body", bodyMap.get("body"));
			    		response.setContentType(CommonConstant.CONTENT_TYPE_TEXT_JSON);	//"text/json; charset=utf-8"

			    		try {
			    			response.getWriter().write(resultJSON.toString());
			    		} catch (Exception e) {
					    	LOGGER.error("error", e);
			    		}
			        	
			        	return false;
			        }else {
				        JSONObject mspData = new JSONObject(bodyMap);
				        request.setAttribute("http-body", 	bodyMap);
				        request.setAttribute("head",		bodyMap.get("head"));
				        request.setAttribute("body",		bodyMap.get("body"));
				        request.setAttribute("rest_uri_path_att", pathVariables);			        	
			        }
	    		} 
		    } catch (Exception e) {
		    	LOGGER.error("error", e);
		      return false;
		} 
	    LOGGER.info("[ # preHandle - 2 # ]");
		return true;

		// TODO Auto-generated method stub
		//return super.preHandle(request, response, handler);
	}

	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
		LOGGER.info("[ # AppApiInterceptor - postHandle - 1 # ]");
	    Map<String, Object> requestMap = null;
	    Map<String, Object> requestHeadMap = null;
	    String requestMethod = StringUtils.defaultString(request.getMethod());
	    if (requestMethod.equalsIgnoreCase(HttpMethod.POST.toString()) || requestMethod.equalsIgnoreCase(HttpMethod.PUT.toString())) {
	    	requestMap = (Map<String, Object>)request.getAttribute("http-body");
	    } else {
	    	requestMap = new HashMap<>();
	    	requestMap.put("head", new HashMap<>());
	    } 
	    
	    requestHeadMap = MapUtils.getMap(requestMap, "head");
	    Map<String, Object> responseMap = new HashMap<>();
	    
	    if (modelAndView != null)
	    	responseMap = modelAndView.getModel(); 
	    
	    Map<String, Object> responseHeadMap = MapUtils.getMap(responseMap, "head");
	    Object resultCodeObj = MapUtils.getObject(responseHeadMap, "result_code");
	    String resultCode = "";
	    
	    if (resultCodeObj == null) {
	    	resultCode = String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value());
	    	if (MapUtils.isEmpty(responseHeadMap))
	    		responseHeadMap = new HashMap<>(); 
			responseHeadMap.put("result_code", resultCode);
			responseHeadMap.put("result_msg", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
			responseMap.put("head", responseHeadMap);
	    } else {
	    	resultCode = (String)resultCodeObj;
	    }
	    if (requestMethod.equalsIgnoreCase(HttpMethod.POST.toString()) || requestMethod.equalsIgnoreCase(HttpMethod.PUT.toString())) {
			ObjectMapper objectMapper = new ObjectMapper();
			String bodyString = objectMapper.writeValueAsString(requestMap);
			LOGGER.info("[ # AppApiInterceptor - postHandle - DEVICE REQUEST# ]=[" + bodyString + "]");
			if (MapUtils.isNotEmpty(requestHeadMap) && this.eventLogUse) {
				RequestVo requestVo = new RequestVo(bodyString, request);
				requestVo.setUserCompCode(request.getRequestURI().replaceAll(request.getContextPath(), ""));
				requestVo.getHead().setAppId((String)requestHeadMap.get("appid"));
				EventLogManager.getInstance().access(request, requestVo);
			} 
	    } 
	    
	    try {
	    	this.secureAuth.responseHandle(request, response, responseMap);
	    } catch (Exception e) {
	    	response.sendError(417);
	    	LOGGER.error(e.getMessage());
	    } 
	    LOGGER.info("[ # AppApiInterceptor - postHandle - 2 # ]");		
		// TODO Auto-generated method stub
		//super.postHandle(request, response, handler, modelAndView);
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
		super.afterCompletion(request, response, handler, ex);
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
