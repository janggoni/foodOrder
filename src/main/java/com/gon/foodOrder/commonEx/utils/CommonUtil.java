package com.sharp.common.utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sharp.common.constant.CommonConstant;
import com.sharp.common.constant.ParamConstant;


@Component
public class CommonUtil {

	private  Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private @Value("${serverMode}")		String SERVER_MODE;
	
	private @Value("${localLang}")		String LOCALE_LANG;

	@Autowired(required = true)
	private MessageSource messageSource;
	

	/**
	 * 다국어 메세지 가져오기 
	 * 
	 * @param code : 메세지코드
	 * @return String : 메세지
	 */
	public String getMessage(String code) {		
		if(code == null || isEmpty(code) ) {
			return "";
		}
		
		return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
	}

	

	/**
	 * 다국어 메세지 가져오기 
	 * 
	 * @param code : 메세지코드
	 * @return String : 메세지
	 */
	public String getMessage(String code, String localeLang) {		
		
		logger.debug("util.getMessage code:{} localeLang:{}", code, localeLang);
		String msg = "";
		
		if(code == null || isEmpty(code) ) {
			return "";
		}
		if(localeLang == null || isEmpty(localeLang) ) {
			localeLang = LOCALE_LANG;
		}

		Locale locale = new Locale(localeLang);
		
		logger.debug("util.getMessage msg:{} localeLang:{}", messageSource.getMessage(code, null, locale));
		
		return messageSource.getMessage(code, null, locale);
	}
	
	/**
	 * 서버타입 
	 * @return boolean	운영설정일 경우 true;
	 */
	public boolean isProdServer() {
		boolean resultBoolean = false;				
		if(SERVER_MODE.equals(CommonConstant.SERVER_MODE_PROD)) {
			resultBoolean = true;
		}		
		return resultBoolean;
	}
			
			
	/**
	 * 필수 파라메터 체크
	 * 
	 * @param inputParam 	요청 데이터
	 * @param keys			채크 키 리스트
	 * @return boolean
	 */
	public boolean validParams(Map<String, Object> inputParam, String[][] keys) {
		boolean resultBoolean = false;
		for(String[] keyItems : keys ) {
			String requiredKeyItem = keyItems[0];
			if(inputParam.get(requiredKeyItem) instanceof List) {
				if(null == inputParam.get(requiredKeyItem)) {
					resultBoolean = true;
					break;
				}
			} else {
				String param = nvlString(inputParam.get(requiredKeyItem));
				if("".equals(param)) {
					resultBoolean = true;
					break;
				}
			}
		}
		return resultBoolean;
	}

	
	/**
	 * 필수 파라메터 오류 데이터 설정
	 * @param inputParam	데이터
	 * @param keys			키 리스트
	 * @return Map
	 */
	public Map<String, Object> validResult(Map<String, Object> inputParam, String[][] keys) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for(String[] keyItems : keys ) {
			String requiredKeyItem = keyItems[0];
			String requiredKeyCode = keyItems[1];
			//String requiredKeyMessage = keyItems[2];

			String param = nvlString(inputParam.get(requiredKeyItem));
			if("".equals(param)) {
				resultMap.put(ParamConstant.RESULT_CODE, 	requiredKeyCode);
				//resultMap.put(ParamConstant.RESULT_MESSAGE, requiredKeyMessage);
				resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage(requiredKeyCode)	);
				break;
			}
		}
		return resultMap;
	}


	/**
	 * 오류 리턴 값 처리
	 * 
	 * @return Map
	 */
	public Map<String, Object> failResult() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.FAIL_CODE	);
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage(CommonConstant.FAIL_CODE)	);		
		return resultMap;
	}

	
	/**
	 * DB 연동 오류 리턴 값 처리
	 * 
	 * @return Map
	 */
	public Map<String, Object> dbFailResult() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ParamConstant.RESULT_CODE, 	"9000"	);
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage("9000")	);
		return resultMap;
	}

	
	/**
	 * CMS 연동 오류 리턴 값 처리 
	 * 
	 * @return Map
	 */
	public Map<String, Object> cmsFailResult() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ParamConstant.RESULT_CODE, 	"9010"	);
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage("9010")	);	
		return resultMap;
	}

	
	/**
	 * CMS 연동 오류 리턴 값 처리 
	 * 
	 * @param code	: 메세지 코드
	 * @return Map
	 */
	public Map<String, Object> cmsFailResult(String code){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ParamConstant.RESULT_CODE, 	code);
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage(code)	);
		return resultMap;
	}
	
	
	/**
	 * CMS 연동 오류 리턴 값 처리 : body 추가해서 리턴함
	 * 
	 * @param resultMap
	 * @return Map
	 */
	public Map<String, Object> addCmsFailResult(Map<String, Object> resultMap) {
		resultMap.put(ParamConstant.RESULT_CODE, 	"9010"	);
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage("9010")	);
		return resultMap;
	}	

	
	/**
	 * CMS 검사 해석 정보 없을 때 리턴 값 처리
	 * 
	 * @return Map
	 */
	public Map<String, Object> addCmsFailAnalysisResult() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ParamConstant.RESULT_CODE, 	"9011");
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage("9011")	);		 
		return resultMap;
	}	
	
	
	/**
	 * CMS 검사 해석 정보 없을 때 리턴 값 처리 : body 추가해서 리턴함
	 * 
	 * @param resultMap
	 * @return Map
	 */
	public Map<String, Object> addCmsFailAnalysisResult(Map<String, Object> resultMap) {
		resultMap.put(ParamConstant.RESULT_CODE, 	"9011");
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage("9011")	);	 
		return resultMap;
	}	

	
	/**
	 * 성공 리턴 값 처리
	 * 
	 * @return Map
	 */
	public Map<String, Object> successResult() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.OK_CODE	);
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage(CommonConstant.OK_CODE)	);
		return resultMap;
	}	
	
	
	/**
	 * 성공 리턴 값 처리
	 * 
	 * @param resultMap
	 * @return Map
	 */
	public Map<String, Object> successResult(Map<String, Object> resultMap) {
		resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.OK_CODE	);
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage(CommonConstant.OK_CODE)	);
		return resultMap;
	}
	
	
	/**
	 * 리턴 값 처리
	 * 
	 * @param requiredKeyCode : 메세지 코드
	 * @return Map
	 */
	public Map<String, Object> messageResult(String requiredKeyCode) {
		Map<String, Object> resultMap = new HashMap<String, Object>();		
		if(isEmpty(requiredKeyCode)) {
			return failResult();
		}		
		resultMap.put(ParamConstant.RESULT_CODE, 	requiredKeyCode);
		resultMap.put(ParamConstant.RESULT_MESSAGE, getMessage(requiredKeyCode)	);
		return resultMap;
	}	

	
	/**
	 * 기본 실패 응답 body 설정.
	 * 
	 * @return Map
	 */
	public Map<String, Object> setFailResponseBody() {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		setResponseBody(resultMap, CommonConstant.FAIL_CODE, getMessage(CommonConstant.FAIL_CODE)	);
		return resultMap;
	}

	
	/**
	 * 기본 응답 body 설정.	 
	 * 
	 * @param body
	 */
	public void setSuccessResponseBody(Map<String, Object> body) {
		setResponseBody(body,CommonConstant.OK_CODE, getMessage(CommonConstant.OK_CODE)	);
	}

	
	/**
	 * 기본 실패 응답 body 설정.
	 * 
	 * @param body
	 */
	public void setFailResponseBody(Map<String, Object> body) {
		setResponseBody(body, CommonConstant.FAIL_CODE, getMessage(CommonConstant.FAIL_CODE)	);
	}
	
	
	/**
	 * 응답 Body 설정
	 * 
	 * @param reqMap
	 * @param code
	 * @param message
	 */
	public void setResponseBody(Map<String, Object> reqMap, String code, String message) {
		reqMap.put(ParamConstant.RESULT_CODE, 	 code);			// 응답 코드
		reqMap.put(ParamConstant.RESULT_MESSAGE, message);		// 응답 메세지
	}
	
	/**
	 * 응답 Body 설정
	 * 
	 * @param reqMap
	 * @param code
	 * @param message
	 */
	public void setResponseBody(Map<String, Object> reqMap, String code) {
		reqMap.put(ParamConstant.RESULT_CODE, 	 code);			// 응답 코드
		reqMap.put(ParamConstant.RESULT_MESSAGE, getMessage(code));		// 응답 메세지
	}	
	
	/**
	 * 문자열  ==> null 채크/
	 * 
	 * @param str
	 * @return boolean
	 */
    public boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    
	/**
	 * Object ==> null 채크/
	 * 
	 * @param obj
	 * @return boolean
	 */
    public boolean isEmpty(final Object obj) {
        if(obj == null) return true;
        else return false;        
    } 	
		
    
	/**
	 * 문자열  ==> null 채크/
	 * 
	 * @return String
	 */
	public String nvlString(String value) {
		String resultString = "";
		if(null != value) {
			resultString = value.trim();
		}
		return resultString;
	}
    
	
    /**
     * object가 null일경우 지정한 숫자를 반환하고 아니면 obejct를 숫자로 변환하여 반환
     *
     * @param obj
     * @param val
     * @return int
     */
    public int nvl(final Object obj, final int val) {
        if(obj == null)return val;
        return Integer.parseInt(obj.toString());
    }
    
    
	/**
	 * 소수점 문자열  ==> int 
	 * 
     * @param obj
     * @param val
     * @return int
	 */   
    public int nvlPoint(final Object obj, final int val) {
        if(obj == null)return val;
        String str = String.valueOf(obj).trim();
        double di =  Double.parseDouble(str);
        return (int) Math.floor(di);
    }
	    
    
    /**
     * obejct 가 null 이면 지정한 문자열을 반환하고 아니면 object 를 문자열로 변환하여 반환
     *
     * @param obj
     * @param val
     * @return String
     */
    public String nvl(final Object obj, final String val) {
        if(obj == null || obj.equals("") || isEmpty(obj)){
            return val;
        }
        return obj.toString();
    }

    
	/**
	 * Object ==> null 채크/
	 * 
     * @param value
     * @return String
	 */
	public String nvlString(Object value) {
		String resultString = "";
		if(null != value) {
			resultString = String.valueOf(value).trim();
		}
		return resultString;
	}
	
	
	/**
	 * Object ==> null 채크/
	 * 
     * @param value
     * @return int
	 */
	public int nvlInt(Object value) {
		int resultInt = 0;
		if(null != value) {
			resultInt = Integer.parseInt(nvlString(value), 0);
		}
		return resultInt;
	}

	
	/**
	 * Object ==> null 채크/
	 * 
     * @param value
     * @return boolean
	 */
	/*public boolean nvlBoolean(Object value) {
		boolean resultBoolean = false;
		if(null != value) {
			resultBoolean = Boolean.parseBoolean(nvlString(value));
		}
		return resultBoolean;
	}*/

	
	/**
	 * Object ==> null 채크/
	 * 
     * @param value
     * @return boolean
	 */
	public boolean nullCheck(Object value) {
		boolean resultBoolean = false;
		if(null != value) {
			String str = nvlString(value);
			if(str != null && !"".equals(str)) {
				resultBoolean = true;
			}
		}
		return resultBoolean;
	}
	
	
	/**
	 * Ojbect ==> Map 채크.
	 * 
     * @param value
     * @return Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> nvlMap(Object value) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if(null != value && value instanceof HashMap) {
			resultMap = (Map<String, Object>)value;
		} else if(null != value && value instanceof String) {
			ObjectMapper mapper = new ObjectMapper();
        	try {
				resultMap = mapper.readValue(nvlString(value), Map.class);
    		} catch (com.fasterxml.jackson.core.JsonParseException jpe) {
    			logger.error("[converterStringToJson] {} ", value, jpe.getMessage());
    		} catch (com.fasterxml.jackson.databind.JsonMappingException jme) {
    			logger.error("[converterStringToJson] {} ", value, jme.getMessage());
    		} catch (IOException e) {
    			logger.error("[converterStringToJson] {} ", value, e.getMessage());
    		}
		}
		return resultMap;
	}
		
	
	/**
	 * Ojbect ==> Map 채크.
	 * 
     * @param value
     * @return Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> nvlMap(String value) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		ObjectMapper mapper = new ObjectMapper();
		try {
			resultMap = mapper.readValue(nvlString(value), Map.class);
		} catch (com.fasterxml.jackson.core.JsonParseException jpe) {
			logger.error("[converterStringToJson] {} ", value, jpe.getMessage());
		} catch (com.fasterxml.jackson.databind.JsonMappingException jme) {
			logger.error("[converterStringToJson] {} ", value, jme.getMessage());
		} catch (IOException e) {
			logger.error("[converterStringToJson] {} ", value, e.getMessage());
		}
		return resultMap;
	}
	
	
	/**
	 * String을 JSONObject로 변환.
	 * 
     * @param data
     * @return JSONObject
	 */
	public JSONObject nvlJson(Object data){
		JSONObject resultJson = new JSONObject();		
		if(null != data) {
			if(data instanceof String) {
				resultJson = nvlJson(data);
			} else if(data instanceof JSONObject) {
				resultJson = (JSONObject)data;
			}
		}
		return resultJson;
	}
	
	
	/**
	 * String을 JSONObject로 변환.
	 * 
     * @param data
     * @return JSONObject
	 */
	public JSONObject nvlJson(String data){
		JSONObject resultJson = new JSONObject();
		JSONParser parser = new JSONParser();	
		try {
			String dataString = nvlString(data);
			resultJson = (JSONObject)parser.parse(dataString);
		} catch (ParseException e) {
			logger.error("CommonUtil.converterStringToJson {} ", data, e);
		}
		return resultJson;
	}

	
	/**
	 * Ojbect ==> List<Object> 채크.
	 * 
     * @param data
     * @return List<Object>
	 */
	@SuppressWarnings("unchecked")
	public List<Object> nvlListObject(Object value) {
		List<Object> resultList = new ArrayList<Object>();
		if(null != value && value instanceof List) {
			resultList = (List<Object>)value;
		}
		return resultList;
	}
	
	
	/**
	 * String ==> List<Map> 변환.
	 * 
     * @param value
     * @return List<Map>
	 */
	public List<Map<String, Object>> nvlListMap(String value) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		try {
			resultList = new ObjectMapper().readValue(value, new TypeReference<List<Map<String, Object>>>(){});
		} catch (com.fasterxml.jackson.core.JsonParseException e) {
			logger.error("[nvlListMap] {} ", value, e.getMessage());
		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			logger.error("[nvlListMap] {} ", value, e.getMessage());
		} catch (IOException e) {
			logger.error("[nvlListMap] {} ", value, e.getMessage());
		}		
		return resultList;
	}
	
	
	/**
	 * String ==> List<Object> 변환.
	 * 
     * @param value
     * @return List<Object>
	 */
	public List<Object> nvlListObject(String value) {
		List<Object> resultList = new ArrayList<Object>();
		try {
			resultList = new ObjectMapper().readValue(value, new TypeReference<List<Object>>(){});
		} catch (com.fasterxml.jackson.core.JsonParseException e) {
			logger.error("[nvlListObject] {} ", value, e.getMessage());
		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			logger.error("[nvlListObject] {} ", value, e.getMessage());
		} catch (IOException e) {
			logger.error("[nvlListObject] {} ", value, e.getMessage());
		}		
		return resultList;
	}
	
	
	/**
	 * Ojbect ==> List<Object> 채크.
	 * 
     * @param value
     * @return List<String>
	 */
	@SuppressWarnings("unchecked")
	public List<String> nvlListString(Object value) {
		List<String> resultList = new ArrayList<String>();
		if(null != value && value instanceof List) {
			resultList = (List<String>)value;
		}
		return resultList;
	}
	
	
	/**
	 * Ojbect ==> List<Object> 채크.
	 * 
     * @param value
     * @return List<Map>
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> nvlListMap(Object value) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		if(null != value && value instanceof List) {
			resultList = (List<Map<String, Object>>)value;
		}
		return resultList;
	}

	
	/**
	 * Ojbect ==> List<Object> 채크.
	 * 
     * @param dataMap
     * @param beforeKey
     * @param afterKey
	 */
	public void moveData(Map<String, Object> dataMap, String beforeKey, String afterKey) {
		if(dataMap != null) {
			Object moveObject = dataMap.remove(beforeKey);
			dataMap.put(afterKey, moveObject);
		}
	}

	
	/**
	 * Ojbect ==> List<Object> 채크.
	 * 
     * @param dataMap
     * @param beforeKey
     * @param afterKey
	 */
	public void copyData(Map<String, Object> dataMap, String beforeKey, String afterKey) {
		if(dataMap != null) {
			Object moveObject = dataMap.get(beforeKey);
			dataMap.put(beforeKey, moveObject);
		}
	}
	

	/**
	 * JSONObject를 Map<String,Object>로 변환.
	 * 
     * @param data
     * @return Map
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> converterStringToMap(String data){
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if(null != data) {
			JSONParser parser = new JSONParser();
			try {
				Gson gson = new Gson();
				JSONObject json = (JSONObject)parser.parse(data);
				resultMap = gson.fromJson(json.toJSONString(), Map.class);
			} catch (ParseException e) {
    			logger.error("[converterStringToMap] {} ", data, e.getMessage());
			}
		}
		return resultMap;
	}
	
	
    /**
     * 전달된 파라미터에 맞게 난수를 생성한다
     * @param len : 생성할 난수의 길이
     * @param dupCd : 중복 허용 여부 (1: 중복허용, 2:중복제거)
     * 
     * @param len
     * @return String
     */
    public String numberGen(int len) {
    	return numberGen(len, 2);
    }
    
    
    /**
     * 전달된 파라미터에 맞게 난수를 생성한다
     * @param len : 생성할 난수의 길이
     * @param dupCd : 중복 허용 여부 (1: 중복허용, 2:중복제거)
     * 
     * @param len
     * @param dupCd
     * @return String
     */    
    public String numberGen(int len, int dupCd ) {    	
    	SecureRandom rand = new SecureRandom();
        String numStr = ""; //난수가 저장될 변수        
        for(int i=0;i<len;i++) {            
            //0~9 까지 난수 생성
            String ran = Integer.toString(rand.nextInt(10));            
            if(dupCd==1) {
                //중복 허용시 numStr에 append
                numStr += ran;
            }else if(dupCd==2) {
                //중복을 허용하지 않을시 중복된 값이 있는지 검사한다
                if(!numStr.contains(ran)) {
                    //중복된 값이 없으면 numStr에 append
                    numStr += ran;
                }else {
                    //생성된 난수가 중복되면 루틴을 다시 실행한다
                    i-=1;
                }
            }
        }
        return numStr;
    }
	

    /**
     * 입력날짜기준으로 한주의 시작(일요일)날짜와 마지막(토요일)날짜 계산
     * 
     * @param dateString
     * @return Map
     */  
    public Map<String, Object> getWeekDays(String dateString) throws java.text.ParseException {    	
    	Map<String, Object> returnMap = new HashMap<String, Object>();    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    	Date date = sdf.parse(dateString);    	
    	Calendar cal = Calendar.getInstance(Locale.KOREA);
    	cal.setTime(date);    	
    	//logger.debug("입력한 날짜 : "+sdf.format(cal.getTime()));
		cal.add(Calendar.DATE, 1 - cal.get(Calendar.DAY_OF_WEEK));
		returnMap.put("startDate", sdf.format(cal.getTime()));
		//logger.debug("첫번째 요일(일요일)날짜:"+sdf.format(cal.getTime()));
		cal.setTime(date);
		cal.add(Calendar.DATE, 7 - cal.get(Calendar.DAY_OF_WEEK));
		returnMap.put("endDate", sdf.format(cal.getTime()));
		//logger.debug("마지막 요일(토요일)날짜:"+sdf.format(cal.getTime()));		
		return returnMap;
    }


    /**
     * 입력날짜기준으로 한주의 시작(일요일)날짜와 마지막(토요일)날짜 계산
     * 
     * @param inDate
     * @return Map
     */  
    public Map<String, Object> getWeekDays(Date inDate) throws java.text.ParseException {    	
    	Map<String, Object> returnMap = new HashMap<String, Object>();    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");    	
    	Calendar cal = Calendar.getInstance(Locale.KOREA);
    	cal.setTime(inDate);     	
    	//logger.debug("입력한 날짜 : "+sdf.format(cal.getTime()));
		cal.add(Calendar.DATE, 1 - cal.get(Calendar.DAY_OF_WEEK));
		returnMap.put("startDate", sdf.format(cal.getTime()));
		//logger.debug("첫번째 요일(일요일)날짜:"+sdf.format(cal.getTime()));
		cal.setTime(inDate);
		cal.add(Calendar.DATE, 7 - cal.get(Calendar.DAY_OF_WEEK));
		returnMap.put("endDate", sdf.format(cal.getTime()));
		//logger.debug("마지막 요일(토요일)날짜:"+sdf.format(cal.getTime()));		
		return returnMap;
    }


    /**
     * 입력날짜기준으로 일주일전 날짜와 마지막날짜 계산
     * 
     * @param inDate
     * @return Map
     */  
    public Map<String, Object> getSevenDays(Date inDate) throws java.text.ParseException {    	
    	Map<String, Object> returnMap = new HashMap<String, Object>();    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");    	
    	Calendar cal = Calendar.getInstance(Locale.KOREA);
    	cal.setTime(inDate);     	
    	//logger.debug("입력한 날짜 : "+sdf.format(cal.getTime()));
		cal.add(Calendar.DATE, - 6);
		returnMap.put("startDate", sdf.format(cal.getTime()));
		//logger.debug("일주일 전 날짜:"+sdf.format(cal.getTime()));
		cal.setTime(inDate);
		cal.add(Calendar.DATE, 1);
		returnMap.put("endDate", sdf.format(cal.getTime()));
		//logger.debug("마지막 날짜:"+sdf.format(cal.getTime()));		
		return returnMap;
    }
	
    
    /**
     * OTP 데이터
     * 6자리 랜덤 숫자 생성
     * 
     * @return String
     */  
	public String randomOtp() {	
		return randomOtp(6);
	}
	
	
    /**
     * OTP 데이터
     * 6자리 랜덤 숫자 생성
     * 
     * @param digits
     * @return String
     */  
	public String randomOtp(int digits) {		
		if(isEmpty(digits)) {
			digits = 6;
		}		
		char pwCol[] = new char[] {
				'1','2','3','4','5','6','7','8','9','0'
		};
		String ranPw = "";
		try {
			Random r = SecureRandom.getInstance("SHA1PRNG");
			for(int i=0;i<digits;i++) {
				//int selRandomPw =  (int) (Math.random() * (pwCol.length));
				int selRandomPw = (r.nextInt(pwCol.length));
				if( selRandomPw > pwCol.length ) {
					selRandomPw = selRandomPw%(pwCol.length);
				}
				ranPw += pwCol[selRandomPw];
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return ranPw;
	}

	
	/**
	 * 비밀번호 초기화 데이터
	 * 10자리 랜덤 숫자,영문,특수문자 생성
     * 
     * @return String
	 */  
	public String randomCode() {
		return randomCode(10);
	}
	
	
	/**
	 * 비밀번호 초기화 데이터
	 * 자리 수 만큼 랜덤 숫자,영문,특수문자 생성
	 */    
	public String randomCode(int digits) {		
		if(isEmpty(digits)) {
			digits = 10;
		}		
		char pwCol[] = new char[] {
				 '1','2','3','4','5','6','7','8','9','0'
				,'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'
				,'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'
				,'!','@','#','$','%','^','&','*','(',')'
		};	
		String ranPw = "";
		try {
			SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
			for(int i=0;i<digits;i++) {
				//int selRandomPw =  (int) (Math.random() * (pwCol.length));
				int selRandomPw = (r.nextInt(pwCol.length));
				if( selRandomPw > pwCol.length ) {
					selRandomPw = selRandomPw%(pwCol.length);
				}
				ranPw += pwCol[selRandomPw];
			}
		} catch (NoSuchAlgorithmException ne) {
			logger.error("[randomCode] {} ", digits, ne.getMessage());
		} catch (Exception e) {
			logger.error("[randomCode] {} ", digits, e.getMessage());
		} 		
		return ranPw;
	}
	

	/**
	 * Xss 처리
	 * (object -> string)
	 */	
	public String removeXss(Object obj) {		
        if(obj == null || "".equals(obj)){
            return "";
        }        
		return removeXss( nvl(obj, "") );
	}
	
	
	/**
	 * Xss 처리
	 * (string -> string)
	 */
	public String removeXss(String str){
		if( str == null || "".equals(str) || isEmpty(str) ) {
			return "";
		}
		//str = str.trim();		
		// 특수 문자 제거
		//str = str.replaceAll("&", 		"&amp;");
		str = str.replaceAll("<", 		"&lt;");
		str = str.replaceAll(">", 		"&gt;");
		str = str.replaceAll("%00", 	"");
		str = str.replaceAll("\"", 		"&quot;");
		str = str.replaceAll("\'", 		"&#39;");
		str = str.replaceAll("%2F", 	"");
		//str = str.replaceAll("%", 		"&#37;");
		return str;
	}
	
	
	/**
	 * map xss 처리
	 * (Map -> Map)
	 */
	public Map<String, Object> removeXss(Map<String, Object> map){
		if( map == null) {
			return map;
		}		
		Set keys = map.keySet();
		Iterator it = keys.iterator();
		
		while(it.hasNext()) {
			Object key = it.next();
			Object value = map.get(key);
			
			if( value == null ) {
				String skey = String.valueOf(key);
				map.put(skey, value);
			}else {
				if(value.getClass().equals(String.class)) {
					String skey = String.valueOf(key);
					Object obj = removeXss(String.valueOf(value));
					map.put(skey, obj);
				}
			}
		}		
		return map;
	}
	
	
	/**
	 * Xss 복원
	 * (object -> string)
	 */
	public String recoverXss(Object obj) {		
        if(obj == null || "".equals(obj) || isEmpty(obj) ){
            return "";
        }        
		return recoverXss( nvl(obj, "") );
	}
	
	
	/**
	 * Xss 복원
	 * (string -> string)
	 */
	public String recoverXss(String str){
		//str = str.trim();
		if( str == null || "".equals(str) || isEmpty(str) ) {
			return "";
		}		
		// 특수 문자 제거
		str = str.replaceAll("&lt;",	"<");
		str = str.replaceAll("&gt;",	">");
		str = str.replaceAll("&quot;",	"\"");
		str = str.replaceAll("&#39;",	"\'");
		//str = str.replaceAll("&amp;", 	"&");
		//str = str.replaceAll("&#37;",	"%");
		return str;
	}
	
	
	/**
	 *map xss 복원
	 * (Map -> Map)
	 */
	public Map<String, Object> recoverXss(Map<String, Object> map){
		if( map == null) {
			return map;
		}		
		Set keys = map.keySet();
		Iterator it = keys.iterator();
		
		while(it.hasNext()) {
			Object key = it.next();
			Object value = map.get(key);
			
			if( value == null ) {
				String skey = String.valueOf(key);
				map.put(skey, value);
			}else {
				if(value.getClass().equals(String.class)) {
					String skey = String.valueOf(key);
					Object obj = recoverXss(String.valueOf(value));
					map.put(skey, obj);
				}
			}
		}		
		return map;
	}
	

	/**
	 * List xss 복원
	 * (List<Map> -> List<Map>)
	 */
	public List<Map<String, Object>> recoverXss(List<Map<String, Object>> list){
		List<Map<String, Object>> rtnList = new ArrayList<Map<String, Object>>();		
		for(Map<String, Object> map : list) {			
			if( map == null) {
				continue;
			}			
			Set keys = map.keySet();
			Iterator it = keys.iterator();			
			while(it.hasNext()) {
				Object key = it.next();
				Object value = map.get(key);				
				if( value == null ) {
					String skey = String.valueOf(key);
					map.put(skey, value);
				}else {
					if(value.getClass().equals(String.class)) {
						String skey = String.valueOf(key);
						Object obj = recoverXss(String.valueOf(value));
						map.put(skey, obj);
					}
				}
			}			
			rtnList.add(map);
		}
		return rtnList;
	}
		
	
	/**
	 * 숫자인지 확인
	 * 
	 * @param item
	 * @return boolean
	 */
	public boolean isNumeric(String item) {
		try {
			item = item.replaceAll("-", "").trim();
			Double.parseDouble(item);	
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	
	/**
	 *  유선전화인지 확인
	 * 
	 * @param item
	 * @return boolean
	 */
	public boolean isTelNumeric(String item) {
		try {
			item = item.replaceAll("-", "").trim();
			Double.parseDouble(item);
			
			if(item.length() >= 9 && item.length() <= 11){
				Pattern ipPattern = Pattern.compile("^\\d{2,3}[.-]?\\d{3,4}[.-]?\\d{4}$");
				return ipPattern.matcher(item).matches();
			}else{
				return false;
			}			
		} catch (NumberFormatException e) {
			logger.error("[isTelNumeric] {} ", item, e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("[isTelNumeric] {} ", item, e.getMessage());
			return false;
		}
	}
	
	
	/**
	 *  휴대전화인지 확인
	 * 
	 * @param item
	 * @return boolean
	 */
	public boolean isPhoneNumeric(String item) {
		try {
			item = item.replaceAll("-", "").trim();
			Double.parseDouble(item);
			
			if(item.length() == 11 || item.length() == 10){
				Pattern ipPattern = Pattern.compile("^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$");				
				return ipPattern.matcher(item).matches();
			}else{
				return false;
			}
		} catch (NumberFormatException e) {
			logger.error("[isPhoneNumeric] {} ", item, e.getMessage());
			return false;
		} catch (Exception e) {
			logger.error("[isPhoneNumeric] {} ", item, e.getMessage());
			return false;
		}
	}
	
	
	/**
	 *  이메일 확인
	 * 
	 * @param item
	 * @return boolean
	 */
	public boolean isEmail(String item) {
		try {
		    boolean isPattern = false;
			Pattern ipPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9._%+-])+\\.[a-zA-Z]{2,6}$");
	
			isPattern = ipPattern.matcher(item).matches();
			if(!isPattern) {
				Pattern ipPattern2 = Pattern.compile("^[a-zA-Z0-9._%+-]+@([a-zA-Z0-9._%+-])+\\.([a-zA-Z0-9._%+-])+\\.[a-zA-Z]{2,6}$");
				isPattern = ipPattern2.matcher(item).matches();
			}
			return isPattern;			
		} catch (Exception e) {
			logger.error("[isEmail] {} ", item, e.getMessage());
			return false;
		}
	}
	
	
	/**
	 * 전화번호 특수문자 제거 (예: 하이픈, 점)
	 * 
	 * @param obj : 전화번호
	 * @return String
	 */
	public String getStringByOnlyNumber(Object obj) {
		if (obj == null) {
			return "";
		}		
		String match = "[^0-9]";
		String str = nvl(obj, "");		
		str = str.replaceAll(match, "");
        return str;
	}

	
	/**
	 * 전화번호 특수문자 제거 (예: 하이픈, 점)
	 * 
	 * @param str : 전화번호
	 * @return String
	 */	
	public String getStringByOnlyNumber(String str) {
		if (str == null) {
			return "";
		}		
		String match = "[^0-9]";		 
		str = str.replaceAll(match, "");
        return str;
	}	
	
	
	/**
	 * 전화번호 특수문자 추가 (예: 하이픈, 점)
	 * 
	 * @param src : 전화번호
	 * @param sp : 	특수문자
	 * @return String
	 */
	public String convPhoneFormat(String src, String sp) {
		if (src == null) {
			return "";
		}
		if (src.length() == 8) {
			return src.replaceFirst("^([0-9]{4})([0-9]{4})$", "$1" + sp + "$2");
		} else if (src.length() == 12) {
			return src.replaceFirst("(^[0-9]{4})([0-9]{4})([0-9]{4})$", "$1" + sp + "$2" + sp + "$3");
		}
		return src.replaceFirst("(^02|[0-9]{3})([0-9]{3,4})([0-9]{4})$", "$1" + sp + "$2" + sp + "$3");
	}
	
	
	/**
	 * 년월 체크
	 * 
	 * @param src : 생년월일
	 * @return boolean
	 */	
	//생년월일 체크
	public boolean isYearMonth(String str) {
		boolean result = false;
		str = getStringByOnlyNumber(str);		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
			sdf.setLenient(false);
			sdf.parse(str);
			result = true;
		} catch(Exception e) {
			logger.error("[isYearMonth] {} ", str, e.getMessage());
			result = false;
		}
		return result;
	}

	
	/**
	 * 생년월일 체크
	 * 
	 * @param src : 생년월일
	 * @return boolean
	 */	
	public boolean isBirth(String str) {
		boolean result = false;
		if(isEmpty(str)) return false;		
		str = getStringByOnlyNumber(str);
		str = str.substring(0,8);		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			sdf.setLenient(false);
			sdf.parse(str);
			result = true;
		} catch(Exception e) {
			logger.error("[isBirth] {} ", str, e.getMessage());
			result = false;
		}
		return result;
	}

	
	/**
	 * 년월일시분초 체크
	 * yyyyMMddHHmmss
	 * @param src : 생년월일
	 * @return boolean
	 */	
	public boolean isDateTime(String str) {
		boolean result = false;
		if(isEmpty(str)) return false;		
		str = getStringByOnlyNumber(str);		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			sdf.setLenient(false);
			sdf.parse(str);
			result = true;
		} catch(Exception e) {
			logger.error("[isDateTime] {} ", str, e.getMessage());
			result = false;
		}
		return result;
	}

	
	/**
	 * 데이터타입 체크
	 * yyyy-MM-dd'T'HH:mm:ss.SSSXXX
	 * @param 
	 * @return boolean
	 */	
	public boolean isDateTimeByGmt(String str) {
		boolean result = false;
		if(isEmpty(str)) return false;		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			sdf.setLenient(false);
			sdf.parse(str);
			result = true;
		} catch(Exception e) {
			logger.error("[isDateTimeByGmt] {} ", str, e.getMessage());
			result = false;
		}
		return result;
	}
	
	
	/**
	 * 현재날짜를 반환한다
	 * YYYYMMDD
	 */
	public String getToday() {
		DecimalFormat df = new DecimalFormat("00");
	    Calendar currentCalendar = Calendar.getInstance();
	    currentCalendar.add(currentCalendar.DATE, 0);
	    String strDay   = Integer.toString(currentCalendar.get(Calendar.YEAR));
	    strDay  += df.format(currentCalendar.get(Calendar.MONTH) + 1);
	    strDay  += df.format(currentCalendar.get(Calendar.DATE));
	    return strDay;
	}	
	
	
	/**
	 * 현재날짜시간를 반환한다
	 * yyyyMMddHHmmss
	 */
	public String getNow() {
        // 현재 날짜/시간
        LocalDateTime now = LocalDateTime.now();	// 2021-06-17T06:43:21.419878100  
        // 포맷팅
        String formatedNow = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")); 	    
	    return formatedNow;
	}

	
	/**
	 * 현재날짜시간를 반환한다
	 * yyyyMMddHHmmss
	 */
	public String getNow(String format) {
		if(isEmpty(format)) format = "yyyy-MM-dd HH:mm:ss";
        // 현재 날짜/시간
        LocalDateTime now = LocalDateTime.now();	// 2021-06-17T06:43:21.419878100  
        // 포맷팅
        String formatedNow = now.format(DateTimeFormatter.ofPattern("format")); 	    
	    return formatedNow;
	}
	
	
	/**
	 *  날짜 형식으로 변경
	 * yyyymmdd -> yyyy-mm-dd
	 * @param item
	 * @return boolean
	 */
	public String convertCal(String day, String format) {
		
		if(!isBirth(day)) return "";
		
		format = nvl(format, "yyyy-MM-dd");
		String transDate = "";

		day = getStringByOnlyNumber(day);
		try {
			java.util.Date d = new java.text.SimpleDateFormat( "yyyyMMdd" ).parse(day);
			
	        // Date로 변경하기 위해서는 날짜 형식으로로 변경해야 한다.
	        SimpleDateFormat afterFormat = new SimpleDateFormat(format);			
	        transDate = afterFormat.format(d);
			
		} catch (Exception e) {
			logger.error("[convertCal] {} ", day, e.getMessage());
		}
				
		return transDate;
	}
	
	
	/**
	 *  날짜 형식으로 변경
	 * yyyyMMddHHmmss -> yyyymmdd
	 * @param item
	 * @return boolean
	 */
	public String convertTimeToCal(String day, String format) {
		
		if(!isDateTime(day)) return "";
		
		format = nvl(format, "yyyy-MM-dd");
		String transDate = "";

		day = getStringByOnlyNumber(day);
		try {
			java.util.Date d = new java.text.SimpleDateFormat( "yyyyMMddHHmmss" ).parse(day);
			
	        // Date로 변경하기 위해서는 날짜 형식으로로 변경해야 한다.
	        SimpleDateFormat afterFormat = new SimpleDateFormat(format);			
	        transDate = afterFormat.format(d);
			
		} catch (Exception e) {
			logger.error("[convertTimeToCal] {} ", day, e.getMessage());
		}
				
		return transDate;
	}
	
	
	/**
	 *  비밀번호 확인
	 * 
	 * @param item
	 * @return boolean
	 */
	public boolean checkPasswordRule(String password, String id) {
		if(isEmpty(password))	return false;
		
		String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		String number = "1234567890";
		String sChar = "-_=+\\|()*&^%$#@!~`?></;,.:'";

		boolean returnBoolean = false;
		
		boolean sChar_Count = false;
		boolean alphaCheck = false;
		boolean numberCheck = false;
		boolean ruleCheck = true;
		if(8 <= password.length() || password.length() <= 15){
			int dualCheckCnt = 0;
			String dualCheckStr = "";			
			
			for(int i=0; i<password.length(); i++){
				String temp = password.substring(i,i+1);
				/*
				//특수문자 체크
				if(sChar.indexOf(temp) != -1){
					sChar_Count = true;
				}
				*/		
				if(alpha.indexOf(temp) != -1){
					alphaCheck = true;
				}
				if(number.indexOf(temp) != -1){
					numberCheck = true;
				}
				
				//반복문자확인 3자 이상 확인
				if( dualCheckStr.equals(temp)){
					dualCheckCnt++;
					if(dualCheckCnt > 2){
						ruleCheck = false;
					}
				}else{
					dualCheckCnt = 1;
					dualCheckStr = temp;
				}
				
			}//for
			
			//비밀번호에 연속 문자만 확인
			if(Pattern.matches("/(w)\1\1\1/", password)){
				ruleCheck = false;
			}
			
			//아이디 유사한 비밀번호 체크
			int chrCnt = 4;	//비교 자리수
			if(!isEmpty(id)) {
				if(id.length() > chrCnt){
					
					for(int i=0; i<= id.length()-chrCnt; i++){
						
						String chkStr = id.substring(i, i+chrCnt);
						if(password.indexOf(chkStr) > -1){
							ruleCheck = false;
						}
						
					}
					
				}else{		
					if(password.indexOf(id) > -1){
						ruleCheck = false;
					}
				}			
			}
			
			//결과 확인
			/* if(sChar_Count != true || alphaCheck != true || numberCheck != true || ruleCheck != true ){  */
			if(alphaCheck != true || numberCheck != true || ruleCheck != true ){	
				returnBoolean = false;
			}else{
				returnBoolean = true;
			}//if			
			
		}else {
			returnBoolean = false;
		}
		
	    return returnBoolean;
	}	


	/**
	 *  Y,N 값 타입 체크
	 * 
	 * @param item
	 * @return boolean
	 */
	public boolean checkAt(String str) {
		if(isEmpty(str))	return false;
		
		String alpha = "YNyn";

		boolean alphaAt = false;
		for(int i=0; i<str.length(); i++){
			String temp = str.substring(i,i+1);
		
			if(alpha.indexOf(temp) != -1){
				alphaAt = true;
			}
			
		}//for
		
	    return alphaAt;
	}	
	
	
	/**
	 *  OS type 체크
	 * 
	 * @param item
	 * @return boolean
	 */
	public String checkOs(Object obj ) {
		
		String str = nvlString(obj).toUpperCase();
				
		if(isEmpty( str ))	return "";
		String checkOs = "";
		
		if("ANDROID".equals( str )){
			checkOs = "A";
		}
		if("IOS".equals( str )){
			checkOs = "I";
		}		
	    return checkOs;
	}
	
	
	/**
	 * 한글 입력 체크 
	 * 
	 * @param item
	 * @return boolean
	 */
	public boolean isKorean(String str){       
		boolean chk = true;
		String match = "[^\uAC00-\uD7A3]";
		String filter = "";
		if(StringUtils.isNotEmpty(str)) {
	    	filter = str.replaceAll(match, "");
	    	if(filter.equals(str)) {
	    		chk = false;
	    	}else {
	    		chk = true;
	    	}
	    }else {
	    	chk = false;
	    }
		
		return chk;
	}

	
	
	/**
    * 바이트를 체크한다. 기준보다 크면 false, 작거나 같으면 true
    * 
    * @param txt 체크할 text
    * @return 
    */
   public int byteCheck(String txt) {
       if (isEmpty(txt)) { return 0; }

       // 바이트 체크 (영문 1, 한글 2, 특문 1)
       int en = 0;
       int ko = 0;
       int etc = 0;

       char[] txtChar = txt.toCharArray();
       for (int j = 0; j < txtChar.length; j++) {
           if (txtChar[j] >= 'A' && txtChar[j] <= 'z') {
               en++;
           } else if (txtChar[j] >= '\uAC00' && txtChar[j] <= '\uD7A3') {
               ko++;
               ko++;
           } else {
               etc++;
           }
       }

       return en + ko + etc;
   }
   
	 /**
     * 바이트를 체크한다. 기준보다 크면 false, 작거나 같으면 true
     * 
     * @param txt 체크할 text
     * @param standardByte 기준 바이트 수
     * @return 
     */
    public boolean byteCheck(String txt, int standardByte) {
        if (isEmpty(txt)) { return true; }
 
        // 바이트 체크 (영문 1, 한글 2, 특문 1)
        int en = 0;
        int ko = 0;
        int etc = 0;
 
        char[] txtChar = txt.toCharArray();
        for (int j = 0; j < txtChar.length; j++) {
            if (txtChar[j] >= 'A' && txtChar[j] <= 'z') {
                en++;
            } else if (txtChar[j] >= '\uAC00' && txtChar[j] <= '\uD7A3') {
                ko++;
                ko++;
            } else {
                etc++;
            }
        }
 
        int txtByte = en + ko + etc;
        if (txtByte > standardByte) {
            return false;
        } else {
            return true;
        }
 
    }
    
	/**
	  * String 문자열, 숫자 글자수 
	  * 
	  * @param str 체크할 text
	  * @return 
	 */
	public static int getStringLength(String str) {
		char ch[] = str.toCharArray();
		int max = ch.length;
		int count = 0;

		for (int i = 0; i < max; i++) {
			// 0x80: 문자일 경우 +2
			if (ch[i] > 0x80) {
				count++;
			}
			count++;
		}
		return count;
	}   
    
	/**
	 *  해당 월의 마지막 날짜 가져오기
	 * 
	 * @param str
	 * @return String
	 */
	public String nowMonthLastDay(String date) {
		
//		if(date.length() != 6) {
//			return "";
//		}
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
			sdf.setLenient(false);
			sdf.parse(date);
		} catch (Exception e) {
			return "";
		}
		
		String year = date.substring(0,4);				//년
		String month = date.substring(4,6);				//월
		
		Calendar cal = Calendar.getInstance();
		cal.set(Integer.parseInt(year),Integer.parseInt(month)-1,1);
		
		String lastDay = year+month+Integer.toString(cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		
		lastDay = lastDay.substring(6);
		
		return lastDay;
	}

	/**
	 *  String -> GMT(IS8601)
	 * 
	 * @param item
	 * @return boolean
	 */
	public String getDateToGmt(String str) {
		
		String gmtDate = "";
		str = getStringByOnlyNumber(str);
		
		if(!isDateTime(str)) return "";
				
        try {
        	
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
			Date date = format.parse(str);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

			gmtDate = sdf.format(date);
            
        } catch (Exception e) {
			return "";
		}
        
		return gmtDate;
	}
	
	
	/**
	 *  GMT(IS8601) -> String(yyyy-MM-dd HH:mm:ss)
	 * 
	 * @param item
	 * @return boolean
	 */
	public String getGmtToDate(String str) {
		
		String gmtDate = "";
		if(isEmpty(str)) return "";
		
		if(!isDateTimeByGmt(str)) {
			return "";
		}
		
        try {
        	
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
			Date date = format.parse(str);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
			
			gmtDate = sdf.format(date);
            
        } catch (Exception e) {
			return "";
		}
        
		return gmtDate;
	}
	
	
	/**
	 *  Url 유효성 체크
	 * 
	 * @param item
	 * @return String
	 */
	public boolean urlCheck(String url)
    {
		String URL_REGEX =
	            "^((((https?|ftps?|gopher|telnet|nntp)://)|(mailto:|news:))" +
	            "(%[0-9A-Fa-f]{2}|[-()_.!~*';/?:@&=+$,A-Za-z0-9])+)" +
	            "([).!';/?:,][[:blank:]])?$";
	 
	    Pattern URL_PATTERN = Pattern.compile(URL_REGEX);
		
	    Matcher matcher = URL_PATTERN.matcher(url);
        return matcher.matches();
    }
	
	
	/**
	 *  비밀번호 복호화(base64)
	 * 
	 * @param item
	 * @return String
	 */
	public String pwDecode(String pw) {
		
		String password = CommonConstant.EMPTY_SPACE;
		
		Decoder decoder = Base64.getDecoder();
		byte[] decoded = decoder.decode(pw);
		
		password = new String(decoded);
		
		return password;
	}
	
	/**
	 *  쿠콘 복호화(base64)
	 * 
	 * @param item
	 * @return String
	 */
	public String pwDecodeCoocon(String pw) {
		
		String password = CommonConstant.EMPTY_SPACE;
		
		byte[] decoded = Base64Utils.decodeFromUrlSafeString(pw);
		
		password = new String(decoded);
		
		return password;
	}
	
	
	/**
	 *  학생나이 계산
	 * 
	 * @param item
	 * @return String
	 */
	public int getAge(String schoolYear) {
		
		schoolYear = schoolYear.toUpperCase();
		int startAge = 7;
		int rtn = 0;
		/*
		 * e1~e6 : 8~13 
		 * m1~m3 : 14~16 
		 * h1~h3 : 17~19
		 * 
		 */	
		if(schoolYear.indexOf("E") > -1) {
			String tmp = schoolYear.replace("E", "");
			if(isNumeric(tmp))	rtn = startAge + Integer.parseInt(tmp);
		}
		
		if(schoolYear.indexOf("M") > -1) {
			String tmp = schoolYear.replace("M", "");
			if(isNumeric(tmp))	rtn = startAge + 3 + Integer.parseInt(tmp);
		}
		
		if(schoolYear.indexOf("H") > -1) {
			String tmp = schoolYear.replace("H", "");
			if(isNumeric(tmp))	rtn = startAge + 6 + Integer.parseInt(tmp);
		}

		return rtn;
	}	
	
	// 2023-04-05 국방부 관련 Api 추가
	/**
	 * HashMap을 HashMap로 변경 [ Key값을 카멜 표기법으로 변경 ]
	 */
	public Map<String, Object> converterMapToMapForCamel( Map<String, Object> map ) {
		Map<String, Object> converterMap = new HashMap<String, Object>();
		if(null != map) {
			for( String key : map.keySet() ) {
				Object value = map.get(key);
				converterMap.put(JdbcUtils.convertUnderscoreNameToPropertyName(key), value);
			}
		}
		return converterMap;
	}

	/**
	 * HashMap을 HashMap로 변경 [ Key값을 카멜 표기법으로 변경 ]
	 */
	public Map<String, Object> converterMapToMapForAllCamel( Map<String, Object> map )  {
		Map<String, Object> converterMap = new HashMap<String, Object>();
		if(null != map) {
			for( String key : map.keySet() ) {
				Object value = map.get(key);
				if(value instanceof Map) {
					converterMap.put(JdbcUtils.convertUnderscoreNameToPropertyName(key), converterMapToMapForCamel(nvlMap(value)));
				} else {
					converterMap.put(JdbcUtils.convertUnderscoreNameToPropertyName(key), value);
				}
			}
		}
		return converterMap;
	}

	/**
	 * JSONObject를 Map<String,Object>로 변환.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> converterJsonToMap(JSONObject json){
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if(null != json) {
			Iterator<String> iterator = json.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Object value = json.get(key);

				resultMap.put(key, value);
			}
		}
		return resultMap;
	}

	/**
	 * JSONObject를 Map<String,Object>로 변환.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> converterStringToMap(Object data){
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if(null != data) {
			JSONParser parser = new JSONParser();
			try {
				Gson gson = new Gson();
				if(data instanceof String) {
					JSONObject json = (JSONObject)parser.parse(nvlString(data));
					resultMap = gson.fromJson(json.toJSONString(), Map.class);
				}
			} catch (ParseException e) {
				logger.error("[converterStringToMap] {} ", data, e.getMessage());
			}
		}
		return resultMap;
	}
	
	/**
	 * Map Object를 JSONObject로 변환.
	 */
	public JSONObject converterMapToJson(Object value) {
		Map<String, Object> map = nvlMap(value);
		JSONObject resultJson = new JSONObject();
		if(map != null) {
			resultJson = new JSONObject(map);
		}
		return resultJson;
	}

	/**
	 * Map Object를 JSONObject로 변환.
	 */
	public JSONObject converterMapToJson(Map<String, Object> map){
		JSONObject resultJson = new JSONObject();
		if(map != null) {
			resultJson = new JSONObject(map);
		}
		return resultJson;
	}

	/**
	 * Map Object를 JSONObject로 변환.
	 */
	public JSONArray converterListToJson(List<String> list){
		JSONArray resultJson = new JSONArray();
		if(list != null) {
			resultJson = new JSONArray(list);
		}
		return resultJson;
	}
	
	/**
	 * Map Object를 JSONObject로 변환.
	 */
	public JSONArray converterListMapToJson(List<Map<String, Object>> list){
		JSONArray resultJson = new JSONArray();
		if(list != null) {
			resultJson = new JSONArray(list);
		}
		return resultJson;
	}
	
	/**
	 * String 데이터를 List<Map> 형태로 변환.
	 */
	public List<Map<String, Object>> converterStringToList(String listString){
		ObjectMapper objectMapper = new ObjectMapper();
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		
		if(isNull(listString)){
			return resultList;
		}
		
		try {
			resultList = objectMapper.readValue(listString, new TypeReference<List<Map<String, Object>>>(){});
		} catch (com.fasterxml.jackson.core.JsonParseException jpe) {
			logger.error("[converterStringToList] {} ", listString, jpe.getMessage());
		} catch (com.fasterxml.jackson.databind.JsonMappingException jme) {
			logger.error("[converterStringToList] {} ", listString, jme.getMessage());
		} catch (IOException ioe) {
			logger.error("[converterStringToList] {} ", listString, ioe.getMessage());
		}

		if(resultList == null) {
			resultList = new ArrayList<Map<String,Object>>();
		}
		return resultList;
	}
	
	/**
	 * Object -> Map 변환
	 * @param objectString
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> convertObjectToMap(Object object) throws Exception {
		
		Map<String, Object> resultMap = new HashMap<>();
		
		String objectString = object.toString();
		
		ObjectMapper objectMapper = new ObjectMapper();
    	resultMap = objectMapper.readValue(objectString, new TypeReference<Map<String,Object>>(){});
    	
		return resultMap;

	}
	
	/**
	 * String[] 배열 복사
	 */
	public String[] copyStringArray(String[] originalArray) {
		String[] resultArray = null;
		if(null != originalArray) {
			resultArray = new String[originalArray.length];
			for (int i = 0; i < originalArray.length ; i++) {
				resultArray[i] = originalArray[i];
			}
		}
		return resultArray;
	}
	
	
	public Map<String, Object> getRequestData(Map<String, Object> requestData){
		Object data = requestData.get("data");
		Map<String, Object> resultMap = nvlMap(data);
		
		return resultMap;
	}

	public Map<String, Object> getRequestData(Map<String, Object> requestData, String Key) {
		Object data = requestData.get(Key);
		Map<String, Object> resultMap = nvlMap(data);
		
		return resultMap;
	}
	
	/**
	 * 세션 정보 가져오기(Object)
	 */
	public Object getSessionAttribute(String key) {
		ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession httpSession = servletRequestAttribute.getRequest().getSession();

		return httpSession.getAttribute(key);
	}

	/**
	 * 세션 정보 가져오기(String)
	 */
	public String getSessionAttributeToString(String key) {
		return nvlString(getSessionAttribute(key));
	}

	/**
	 * 세션 정보 가져오기(boolean)
	 */
	public boolean getSessionAttributeToBoolean(String key) {
		return nvlBoolean(getSessionAttribute(key));
	}

	/**
	 * 세션 정보 가져오기(Map)
	 */
	public Map<String, Object> getSessionAttributeToMap(String key) {
		return nvlMap(getSessionAttribute(key));
	}

	/**
	 * 세션 정보 가져오기(회원 CI)
	 */
	public String getMilCi(){
		String mberCi = null;
		if(isMberInfo()) {
			Map<String, Object> mberInfo = getMberInfo();
			mberCi = nvlString(mberInfo.get("milCi"));
		}
		return mberCi;
	}
	

	/**
	 * 세션 정보 저장하기.
	 */
	public void setSessionAttribute(String key, Object value) {
		ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession httpSession = servletRequestAttribute.getRequest().getSession();

		httpSession.setAttribute(key, value);;
	}

	/**
	 * 세션 정보 삭제.
	 */
	public void removeSessionAttribute(String key) {
		ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpSession httpSession = servletRequestAttribute.getRequest().getSession();

		httpSession.removeAttribute(key);
	}

	/**
	 * HttpServletRequest 얻기.
	 */
	public HttpServletRequest getHttpServletRequest() {
		ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest httpServletRequest = servletRequestAttribute.getRequest();

		return httpServletRequest;
	}
	
	/**
	 * HttpServletRequest 얻기.
	 */
	public HttpServletRequest getServletWebRequest() {
		ServletWebRequest servletContainer = (ServletWebRequest)RequestContextHolder.getRequestAttributes();
		HttpServletRequest httpServletRequest = servletContainer.getRequest();

		return httpServletRequest;
	}
	
	/**
	 * HttpServletResponse 얻기.
	 */
	public HttpServletResponse getHttpServletResponse() {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		HttpServletResponse httpServletResponse = servletRequestAttributes.getResponse();
		
		return httpServletResponse;
	}
	
	/**
	 * HttpServletResponse 얻기.
	 */
	public HttpServletResponse getServletWebResponse() {
		ServletWebRequest servletContainer = (ServletWebRequest)RequestContextHolder.getRequestAttributes();
		HttpServletResponse response = servletContainer.getResponse();
		
		return response;
	}

	
	/**
	 * 세션 만료 처리.
	 */
	public boolean invalidateSession() {
		boolean resultboolean = true;
		ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest httpServletRequest = servletRequestAttribute.getRequest();
		HttpSession session = httpServletRequest.getSession();
		session.invalidate();

		return resultboolean;
	}

	public HttpSession getHttpSession() {
		ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest httpServletRequest = servletRequestAttribute.getRequest();
		HttpSession session = httpServletRequest.getSession();

		return session;
	}

	public boolean isHttpServletMultipartRequest() {
		ServletRequestAttributes servletRequestAttribute = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest httpServletRequest = servletRequestAttribute.getRequest();

		boolean isMultipart = ServletFileUpload.isMultipartContent(httpServletRequest);

		return isMultipart;
	}

	/**
	 * 세션에 멤버 정보가 있는지 확인.
	 */
	public boolean isMberInfo() {
		Map<String, Object> mberInfo = nvlMap(getSessionAttributeToMap("milmhSession"));
		
		if(mberInfo.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 세션에 있는 멤버 정보 가져오기.
	 */
	public Map<String, Object> getMberInfo(){
		Map<String, Object> mberInfo = new HashMap<String, Object>();
		if(isMberInfo()) {
			mberInfo = nvlMap(getSessionAttributeToMap("milmhSession"));
		}
		return mberInfo;
	}

	public int nvlIntDelete(Object value) {
		int resultInt = 0;
		if(null != value && !value.equals("")) {
			resultInt = Integer.parseInt(nvlString(value));
		}
		return resultInt;
	}

	/**
	 * Object ==> null 채크/
	 */
	public boolean nvlBoolean(Object value) {
		boolean resultBoolean = false;
		if(null != value) {
			resultBoolean = Boolean.parseBoolean(nvlString(value));
		}
		return resultBoolean;
	}
	
	/**
	 * Object ==> boolean 채크.
	 */
	public boolean isNull(String string) {
		if(nvlString(string).equals("")) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getRequestIp() {
		HttpServletRequest request = getHttpServletRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP"); // 웹로직
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }

        //logger.info("# IP Address [ " + ip + " ]");

        return ip;
	}
	
	public boolean isEquals(String[] list, String equalsValue) {
		boolean resultBoolean = false;
		
		// logger.info("############[Interceptor Uri Check]############");
		
		for (String value : list) {
		
			/* logger.info("# " + value + " [ " + equalsValue + " ]"); */

			if(value.equals(equalsValue)) {
				resultBoolean = true;
				break;
			}
		}
		return resultBoolean;
	}
	
	/**
	 *  날짜 형식으로 변경
	 * yyyymmdd -> yyyy-mm-dd
	 * @param item
	 * @return boolean
	 */
	public java.util.Date convertStringToDate(String day, String format) {
		
		java.util.Date ret = new java.util.Date();
		
		if(!isBirth(day)) return ret;
		
		format = nvl(format, "yyyyMMddHHmmss");
		String transDate = "";

		day = getStringByOnlyNumber(day);
		try {
			ret = new java.text.SimpleDateFormat( format ).parse(day);
		} catch (Exception e) {
			logger.error("[convertStringToDate] {} ", day, e.getMessage());
		}
		return ret;
	}
}
