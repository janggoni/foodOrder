package com.sharp.common.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.sharp.common.constant.CommonConstant;
import com.sharp.common.constant.ParamConstant;


@Component
public class MailSender {

	private  static Logger LOGGER = LoggerFactory.getLogger(MailSender.class);
		
	private @Value("${mail.smtp.host}")				String MAIL_HOST;
	private @Value("${mail.smtp.port}") 			String MAIL_PORT;
	private @Value("${mail.smtp.auth}") 			String MAIL_AUTH;
	private @Value("${mail.smtp.starttls.enable}") 	String MAIL_STARTTLS_ENABLE;
	private @Value("${mail.smtp.ssl.trust}")		String MAIL_SSL_TRUST;
	
	private @Value("${mail.auth.id}")				String MAIL_AUTH_ID;
	private @Value("${mail.auth.key}")				String MAIL_AUTH_KEY;
	
	/*
	 * 발송 이메일 template 설정 정보
	 * 추가 방법
	 * 1. /src/main/resources/main/에 template 파일 추가
	 * 2. /src/main/resources/config/app-config.xml 에 설정 파일 위치 추가(<email><template>에 파일 위치 추가)
	 */
	private @Value("${email.template.joinAuthCode}")	String JOIN_AUTH_CODE;
	private @Value("${email.template.passwdInit}")		String PASSWD_INIT;
	
	@Autowired
    private CommonUtil commonUtil;

	@Autowired
    private CommonCryptoUtil commonCryptoUtil;
	
	@Autowired(required = true)
	private MessageSource messageSource;
	
	/*
	 * 가입인증코드 발송
	 */
	public Map<String,Object> sendAuthCode(String strEmail, String strAuthCode, String strTtitle) {
		
		LOGGER.info("[MailSender] sendAuthCode ----START !!");

		Map<String,Object> returnMap = commonUtil.successResult();
		
		try {

			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("TITLE", 		commonUtil.getMessage("0020")	);	//인증코드가 발급되었습니다.
			
			paramMap.put("CONST_TITLE", commonUtil.getMessage("mail.auth.title")	);
			paramMap.put("CONST", 		commonUtil.getMessage("mail.auth.code")		);
			paramMap.put("PARAM01", 	strAuthCode);

			String content = getMailTemplate(JOIN_AUTH_CODE, paramMap);
			
			if(commonUtil.isEmpty(content)) {
				//오류가 발생하였습니다.
	        	return commonUtil.failResult();
			}
			
			Map<String,Object> sendResultMap = sendMailByTemplate(strEmail, strAuthCode, strTtitle, content);

			if(commonUtil.isEmpty(sendResultMap)) {
				//오류가 발생하였습니다.
	        	return commonUtil.failResult();
			}
			//messageSource.getMessage(CommonConstant.OK_CODE, null, LocaleContextHolder.getLocale());
			if( (sendResultMap.get(ParamConstant.RESULT_CODE)).equals(CommonConstant.OK_CODE) ) {
				return sendResultMap;
			};
			
		} catch (Exception e) {
			LOGGER.error("[MailSender] sendAuthCode ERROR : "+ e.getMessage());
			//오류가 발생하였습니다.
        	return commonUtil.failResult();
		}
		
		return returnMap;
	}
	
	
	/*
	 * 비밀번호초기화 발송
	 */
	public Map<String,Object> sendPasswdInit(String strEmail, String strAuthCode, String strTtitle) {
		
		LOGGER.info("[MailSender] sendPasswdInit ----START !!");

		Map<String,Object> returnMap = commonUtil.successResult();
		
		try {

			Map<String,Object> paramMap = new HashMap<String,Object>();
			paramMap.put("TITLE", 		messageSource.getMessage("0021", null, LocaleContextHolder.getLocale())	);	//비밀번호가 초기화되었습니다.

			paramMap.put("CONST_TITLE", commonUtil.getMessage("mail.passwd.init.title")	);
			paramMap.put("CONST", 		commonUtil.getMessage("mail.passwd.init.code")	);
			paramMap.put("PARAM01", 	strAuthCode);
			
			String content = getMailTemplate(PASSWD_INIT, paramMap);
			
			if(commonUtil.isEmpty(content)) {
				//오류가 발생하였습니다.
	        	return commonUtil.failResult();
			}
			
			Map<String,Object> sendResultMap = sendMailByTemplate(strEmail, strAuthCode, strTtitle, content);
			
			if(commonUtil.isEmpty(sendResultMap)) {
				//오류가 발생하였습니다.
	        	return commonUtil.failResult();
			}
			
			if( (sendResultMap.get(ParamConstant.RESULT_CODE)).equals(CommonConstant.OK_CODE) ) {
				return sendResultMap;
			};
			
		} catch (Exception e) {
			LOGGER.error("[MailSender] sendPasswdInit ERROR : "+ e.getMessage());
			//오류가 발생하였습니다.
        	return commonUtil.failResult();
		}
		
		return returnMap;
	}
	
	/*
	 * type별 메일 template 발송 처리
	 */
	public Map<String,Object> sendMailByTemplate(String strEmail, String strAuthCode, String strTtitle, String strContent) {
		
		LOGGER.info("[MailSender] sendMailByTemplate ----START !!");
		
		if( commonUtil.isEmpty(strEmail) || commonUtil.isEmpty(strTtitle) || commonUtil.isEmpty(strContent)) {
			//오류가 발생하였습니다.
        	return commonUtil.failResult();
		}
		
		Map<String,Object> returnMap = commonUtil.successResult();
		
		Properties props = new Properties();
		props.put("mail.smtp.host", 			MAIL_HOST);
		props.put("mail.smtp.port", 			MAIL_PORT);
		props.put("mail.smtp.auth", 			MAIL_AUTH);
		props.put("mail.smtp.starttls.enable",	MAIL_STARTTLS_ENABLE);
		props.put("mail.smtp.ssl.trust", 		MAIL_SSL_TRUST);
		
		Session session = Session.getInstance(props, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				String decryptPwd = commonCryptoUtil.decode(MAIL_AUTH_KEY);				
				return new PasswordAuthentication( MAIL_AUTH_ID, decryptPwd);
			}
		});
		
		String receiver = strEmail; // 메일 받을 주소
		String title = strTtitle;	//"테스트 메일입니다.";
		String content = strContent;
		
		Message message = new MimeMessage(session);
		
		try {

			message.setFrom(new InternetAddress( MAIL_AUTH_ID, "관리자", "utf-8"));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
			message.setSubject(title);
			message.setContent(content, "text/html; charset=utf-8");

			Transport.send(message);
			
		} catch (Exception e) {
			LOGGER.error("[MailSender] sendMailByTemplate ERROR : "+ e.getMessage());
			return commonUtil.failResult();
		}
		
		return returnMap;
	}


	/*
	 * type별 template 메일 컨텐츠 생성
	 */
	public String getMailTemplate(String template, Map<String,Object> paramMap) {

		String tempStr = "";
		Map<String,Object> returnMap = commonUtil.successResult();
		
		if( !commonUtil.isEmpty(template) && !commonUtil.isEmpty(paramMap) ) {
			
			try {
				//ClassPathResource resource = new ClassPathResource("/mail/template_"+type+".mail");
				ClassPathResource resource = new ClassPathResource(template);
				
				Path path = Paths.get(resource.getURI());
				List<String> content = Files.readAllLines(path);
				StringBuffer strBuff = new StringBuffer();
				
				for(String cont:content) {
					strBuff.append(cont);
				}
				
				tempStr = strBuff.toString();
				
				//parameter 수 만큼 변경 처리
		    	Iterator<String> iter = paramMap.keySet().iterator();		    	
		    	while(iter.hasNext()) {
		    		String key = iter.next();
		    		String value = (String) paramMap.get(key);		    		
		    		tempStr = tempStr.replaceAll("[$]\\{"+key+"\\}", value);		    		
		    	}
								
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LOGGER.error("[MailSender] getMailTemplate ERROR : "+ e.getMessage());
				return "";
			}
			
		}
		
		return tempStr;		
	}
}
