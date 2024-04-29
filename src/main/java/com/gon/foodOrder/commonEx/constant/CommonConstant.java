package com.sharp.common.constant;

/**
 * 공통 상수 Class
 */
public class CommonConstant {

	/**
	 * HTTP Method 값( POST )
	 */
	public static final String HTTP_POST 						= "POST";

	/**
	 * HTTP Method 값( GET )
	 */
	public static final String HTTP_GET 						= "GET";

	/**
	 * UTF8 값
	 */
	public static final String UTF8 							= "UTF-8";

	/**
	 * Content Type ( application/json;charset=UTF-8 )
	 */
	public static final String CONTENT_TYPE_APPLICATION_JSON 	= "application/json";

	/**
	 * Content Type ( application/json;charset=UTF-8 )
	 */	
	public static final String CONTENT_TYPE_TEXT_JSON 			= "text/json; charset=utf-8";
	
	/**
	 * Content Type ( application/x-www-form-urlencoded )
	 */
	public static final String CONTENT_TYPE_FORM_URLENCODED 	= "application/x-www-form-urlencoded";
	
	/**
	 * AUTHORIZATION
	 */
	public static final String AUTHORIZATION 	= "Authorization";
	
	/**
	 * Controller 기본
	 */
	public static final String CONTROLLER_DEFAULT_PRODUCES = "application/json; charset=utf8";

	/**
	 * ModelAndView json view 기본
	 */
	public static final String MODELVIEW_DEFAULT_JSON_VIEW = "defaultJsonView";	
	
	/**
	 * request headers
	 */
	public static final String REQUEST_HEADERS = "headers";
	
	/**
	 * request head
	 */
	public static final String REQUEST_HEAD = "head";
	
	/**
	 * request body
	 */
	public static final String REQUEST_BODY  = "body";
	
	/**
	 * response result key
	 */
	//public static final String RESULT = "result";	 
	
	/**
	 * success ok code
	 */
	public static final String OK_CODE = "0000";	
	
	/**
	 * fail code
	 */
	public static final String FAIL_CODE = "9999";	
	
	/**
	 * locale langueage
	 */	
	public static final String LOCALE_LANGUEAGE = "locale_langueage";	
	
	/**
	 * null
	 */
	public static final String EMPTY_SPACE = "";	
	
	/**
	 * zero
	 */
	public static final int CNT_ZERO = 0;	
	
	/**
	 * -1
	 */
	public static final int MINUS_ONE = -1;	

	/**
	 * 1
	 */
	public static final int NUM_ONE = 1;

	/**
	 * 10
	 */
	public static final int NUM_TEN = 10;	
	
	/**
	 * 20
	 */
	public static final int NUM_TWENTY = 20;	
	
	/**
	 * 사용여부 : Y
	 */
	public static final String AT_Y = "Y";	
	

	/**
	 * 사용여부 : N
	 */
	public static final String AT_N = "N";	
	
	/**
	 * 인증코드 길이 : 8
	 */
	public static final int AUTH_CODE_LENGTH = 6;
	
	/**
	 * 인증코드 타입 : 이메일
	 */
	public static final String AUTH_CODE_TYPE_EMAIL = "E";	
	

	/**
	 * 증코드 타입 : SMS
	 */
	public static final String AUTH_CODE_TYPE_SMS = "S";	
		

	/**
	 * 서버 모드: 운영
	 */
	public static final String SERVER_MODE_PROD = "prod";	
		

	/**
	 * 날짜 타입
	 */
	public static final String CALENDAR_FORMAT = "yyyy-MM-dd";	
	
	
	/**
	 * 날짜 타입
	 */
	public static final String CALENDAR_FORMAT_YYYYMMDD = "yyyyMMdd";	

	
	/**
	 * 날짜 timestamp 타입
	 */
	public static final String TIMESTAMP_DATE = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 자원타입 : 진단
	 */
	public static final String DIAGNOSIS_TYPE = "D";
	
	/**
	 * 자원타입 : 활동
	 */
	public static final String ACTIVITY_TYPE = "A";	

	/**
	 * 자원타입 : 진단
	 */
	public static final String DIAGNOSIS_TYPE_CODE = "01";
	
	/**
	 * 자원타입 : 활동
	 */
	public static final String ACTIVITY_TYPE_CODE = "02";	
	
	/**
	 * OS 타입 : 안드로이드
	 */
	public static final String ANDROID_OS_TYPE = "A";	

	/**
	 * OS 타입 : IOS
	 */
	public static final String IOS_OS_TYPE = "I";	

	/**
	 * 앱형 자원 로그인
	 */
	public static final String RSRC_LOGIN = "RSRC_LOGIN";		
	
	/**
	 * 마음건강앱 로그인
	 */
	public static final String LOGIN_TYPE_MIND = "M";		
	
	/**
	 * 마음건강앱 REFRESH_TOKEN 로그인
	 */
	public static final String LOGIN_TYPE_REFRESH_TOKEN = "R";
	
	/**
	 * 앱형 로그인
	 */
	public static final String LOGIN_TYPE_APP = "A";		

	/**
	 * INTERVAL_TYPE : Day
	 */
	public static final String INTERVAL_TYPE_D = "D";

	/**
	 * INTERVAL_TYPE : Hour
	 */
	public static final String INTERVAL_TYPE_H = "H";	
	
	/**
	 * INTERVAL_TYPE : min
	 */
	public static final String INTERVAL_TYPE_M = "M";
	
	/**
	 * 가입 타입 : 이메일 인증 가입
	 */
	public static final String JOIN_TYPE_E = "E";	
	
	/**
	 * 가입 타입 : 페이스북 인증 가입
	 */
	public static final String JOIN_TYPE_F = "F";	
	
	/**
	 * 가입 타입 : 지메일 인증 가입
	 */
	public static final String JOIN_TYPE_G = "G";	
	
	/**
	 * 가입 타입 : 카카오톡 인증 가입
	 */
	public static final String JOIN_TYPE_K = "K";	
		
	/**
	 * 치료 진행 타입 : 인지훈련
	 */
	//public static final String PRC_TYPE_C = "C";	
	
	/**
	 * 치료 진행 타입 : 부스트세션
	 */
	//public static final String PRC_TYPE_B = "B";	

	/**
	 * 감정일지 : 긍정정서
	 */
	//public static final String EMOTION_P = "P";

	/**
	 * 감정일지 : 부정정서
	 */
	//public static final String EMOTION_N = "N";
	
	/**
	 * 심리검사 타입 : 우울검사
	 */
	//public static final String PSYCH_D = "D";	

	/**
	 * 심리검사 타입 : 불안척도검사
	 */
	//public static final String PSYCH_A = "A";

	/**
	 * 심리검사 타입 : 긍정/부정 정서 검사
	 */
	//public static final String PSYCH_P = "P";
	
	
	
}
