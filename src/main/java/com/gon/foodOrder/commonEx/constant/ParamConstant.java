package com.sharp.common.constant;

/**
 * 파라미터 상수 Class
 */
public class ParamConstant {

	//┌────────────────────────[ headers  ]────────────────────────┐
	// header 상수.
	/**
	 * access_token
	 */
	public static final String HEADER_ACCESS_TOKEN = "access_token";
	/**
	 * refresh_token
	 */
	public static final String HEADER_REFRESH_TOKEN = "refresh_token";


	//┌────────────────────────[ 공통  ]────────────────────────┐
	// 응답 상수.

	/**
	 * 응답 상수 코드.
	 */
	public static final String RESULT_CODE = "resultCode";
	/**
	 * 응답 상수 메세지.
	 */
	public static final String RESULT_MESSAGE = "resultMessage";
	/**
	 * 응답 상수 개수
	 */
	public static final String RESULT_COUNT = "resultCount";
	/**
	 * 응답 데이터
	 */
	public static final String RESULT_DATA = "resultData";
	/**
	 * 응답 데이터
	 */
	public static final String RESULT = "result";


	/**
	 * 오류 메세지.
	 */
	public static final String ERROR_MESSAGE = "errorMessage";

	
	
	//┌────────────────────────[ 샘플 상수  ]────────────────────────┐	
	
	public static final String SMP_PARAM1 = "param1";
	public static final String SMP_PARAM2 = "param2";
	


	//┌────────────────────────[ head  ]────────────────────────┐
	/**
	 * OS 명
	 */	
	public static final String SYSTEM_NAME = "system_name";	
	
	
	//┌────────────────────────[ body  ]───────-─────────────────┐
	/**
	 * OS 명
	 */	
	public static final String SYSTEM_NM = "systemName";	
	

	
	
	
	// ############# 송도 더샾 ################################
	
	//┌────────────────────────[ 회원가입 & 로그인  ]────────────────────────┐
		/**
		 * 회원 이메일
		 */	
		public static final String EMAIL = "email";	

		/**
		 * 가입 이메일 여부
		 */	
		public static final String EMAIL_YN = "emailYn";
		/**
		 * 회원 이름
		 */	
		public static final String NAME = "name";
		/**
		 * 회원 비밀번호
		 */	
		public static final String MEMBER_PWD = "memberPw";
		
		/**
		 * 회원 이전 비밀번호
		 */	
		public static final String PRV_PASSWD = "prvPasswd";
		
		/**
		 * 회원 cuid
		 */	
		public static final String CUID = "cuid";
		/**
		 * 회원 마스터cuid
		 */	
		public static final String MST_CUID = "mstCuid";
		/**
		 * 회원 부사용자cuid
		 */	
		public static final String SLAV_CUID = "slavCuid";
		
		
		/**
		 * 회원 약관동의 번호 리스트
		 */	
		public static final String TERMS_SEQ = "termsSeq";
		/**
		 * 회원 약관동의 번호 리스트
		 */	
		public static final String TERMS_SEQ_LIST = "termsSeqList";
		
		
		/**
		 * 회원 이전 비밀번호
		 */	
		public static final String USER_TYPE = "userType";
		
		
		/**
		 * 회원 전화번호
		 */	
		public static final String MOBILE_NUMBER = "mobileNum";
		
		/**
		 * 성별
		 */	
		public static final String GENDER = "gender";
		
		/**
		 * 회원 전화번호
		 */	
		public static final String JOIN_CHECK = "joinCheck";
		
		/**
		 * 생년월일
		 */	
		public static final String BIRTH_DAY = "birthDay";
		
		/**
		 * 신장
		 */	
		public static final String HT = "ht";
		
		/**
		 * 최초로그인 여부
		 */	
		public static final String USE_YN = "useYn";

}
