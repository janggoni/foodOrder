package com.sharp.common.constant;

/**
 * validation check Class
 * @author uracle
 */
public class ValidConstant {
	
	
	//┌────────────────────────[ 샘플 ]────────────────────────┐	
	/**
	 * 샘플 필수값 채크.
	 * @see<tbody>
	 * param1 	: 파라미터1.<br>
	 * param1 	: 파라미터2.<br>
	 */
	public static final String[][] SAMPLE_KEYS = {
			 { ParamConstant.SMP_PARAM1			, "0001" }
			,{ ParamConstant.SMP_PARAM2 		, "0001" }
	};

	

	//┌────────────────────────[ 회원가입 & 로그인  ]────────────────────────┐
	/**
	 * 이메일 코드 발송
	 * @see<tbody>
	 * email 	: 이메일.<br>
	 */
	public static final String[][] EMAIL_CODE_PARAM_KEYS = {
			 { ParamConstant.EMAIL				, "0100" }
	};	

	/**
	 * 이메일 코드 확인
	 * @see<tbody>
	 * email 	: 이메일.<br>
	 */
	public static final String[][] EMAIL_CODE_AUTH_PARAM_KEYS = {
			 { ParamConstant.EMAIL				, "0100" }
			,{ ParamConstant.AUTH_CODE			, "0101" }
	};	
	
	/**
	 * 핸드폰 인증 코드 발송
	 * @see<tbody>
	 * email 	: 이메일.<br>
	 */
	public static final String[][] PHONE_CODE_PARAM_KEYS = {
			 { ParamConstant.PHONE_NUMBER		, "0103" }
	};
		
	/**
	 * 핸드폰 인증 코드 확인
	 * @see<tbody>
	 * email 	: 이메일.<br>
	 */
	public static final String[][] PHONE_CODE_AUTH_PARAM_KEYS = {
			 { ParamConstant.PHONE_NUMBER		, "0103" }
			,{ ParamConstant.AUTH_CODE			, "0101" }
	};	

	/**
	 * SNS 가입정보 확인
	 * @see<tbody>
	 * email 	: 이메일.<br>
	 */
	public static final String[][] SNS_CHECK_PARAM_KEYS = {
			 { ParamConstant.EMAIL				, "0100" }
			,{ ParamConstant.JOIN_TYPE			, "0119" }
	};	
	 
	/**
	 * 별명 중복 필수값 체크.
	 * @see<tbody>
	 * memberId 	: 회원Id.<br>
	 * nickName 	: 별명.<br>
	 */
	public static final String[][] NICK_NAME_CHECK_PARAM_KEYS = {
			 { ParamConstant.NICK_NAME			, "0301" }
	};	
	
	/**
	 * 별명 저장 필수값 체크.
	 * @see<tbody>
	 * memberId 	: 회원Id.<br>
	 * nickName 	: 별명.<br>
	 */
	public static final String[][] NICK_NAME_PARAM_KEYS = {
			 { ParamConstant.MEMBER_ID			, "0304" }
			,{ ParamConstant.NICK_NAME			, "0302" }
	};
	
	/**
	 * 회원가입 필수값 채크.
	 * @see<tbody>
	 * email 	: 이메일.<br>
	 * passwd 	: 비밀번호.<br>
	 */
	public static final String[][] JOIN_PARAM_KEYS = {
			 { ParamConstant.EMAIL				, "0100" }
			,{ ParamConstant.MEM_PASSWD			, "0305" }
			,{ ParamConstant.MEM_REPASSWD		, "0306" }
			,{ ParamConstant.JOIN_TYPE			, "0306" }
			//,{ ParamConstant.NICK_NAME			, "0302" }
			//,{ ParamConstant.GENDER				, "0106" }
			//,{ ParamConstant.BIRTH_DAY			, "0107" }
			//,{ ParamConstant.PHONE_NUMBER		, "0105" }
	};
	

}
