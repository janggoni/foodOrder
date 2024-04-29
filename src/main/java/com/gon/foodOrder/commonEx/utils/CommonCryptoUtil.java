package com.sharp.common.utils;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CommonCryptoUtil {

	private  static Logger LOGGER = LoggerFactory.getLogger(CommonCryptoUtil.class);
	
	/*
	 * AES256 decode
	 */
	public static String decode(String str) {   	
    	String decStr = "";		
		try {
			AES256Util aes256 = AES256Util.getInstance();			 
	    	String IV = aes256.IV;
	    	decStr = aes256.AES_Decode(str);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("[CommonCryptoUtil] decode ERROR : "+e.getMessage() );
		}
		
		return decStr;
    }
	
	
	/*
	 * AES256 decode
	 */
	public static String decode(String str, String secretKey, String strIV) {
    	String decStr = "";		
		try {
			AES256Util aes256 = AES256Util.getInstance();
	    	decStr = aes256.AES_Decode(str, secretKey, strIV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("[CommonCryptoUtil] decode ERROR : "+e.getMessage() );
		}
		
		return decStr;
    }
	
	/*
	 *  AES256 encode
	 */
	public static String encode(String str) {
		String baseEncStr = "";
		try {
			AES256Util aes256 = AES256Util.getInstance();
	    	String IV = aes256.IV;
	    	baseEncStr = aes256.AES_Encode(str);			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("[CommonCryptoUtil] encode ERROR : "+e.getMessage() );
		}
		
		return baseEncStr;
    }

	
	/*
	 *  AES256 encode
	 */
	public static String encode(String str, String secretKey, String strIV) {
		String baseEncStr = "";
		try {
			AES256Util aes256 = AES256Util.getInstance();
	    	String IV = aes256.IV;
	    	baseEncStr = aes256.AES_Encode(str, secretKey, strIV);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("[CommonCryptoUtil] encode ERROR : "+e.getMessage() );
		}
		
		return baseEncStr;
    }
	
	/*
	 * URLEncoder
	 */
	public static String urlEncode(String str, String secretKey, String strIV) {
		String baseEncStr = encode(str, secretKey, strIV);
		
		try {
			baseEncStr = URLEncoder.encode(baseEncStr, "UTF-8");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("[CommonCryptoUtil] urlEncode ERROR : "+e.getMessage() );
		}
		return baseEncStr;
	}	
	
	/*
	 * URLDecoder 
	 */
	public static String urlDecode(String str, String secretKey, String strIV) {
		String decStr = "";
		try {

			decStr = decode(URLDecoder.decode(str, "UTF-8"), secretKey, strIV);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			LOGGER.error("[CommonCryptoUtil] urlDecode ERROR : "+e.getMessage() );
		}
		return decStr;
	}	
	
	/*
	public static void main(String[] args) {
		try {

			String secretKey = "rydbrqnakdmarjsrkddoqghkfehddoq1"; //32bit
		 	String iv = "ghkfehddoqdkdlqm";	 //16bit
						
	    	String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtZW1iZXJJZCI6ImpsYUNFZVh0UmxlSzFqbiIsImlhdCI6MTY4MTgzMzI5MCwiZXhwIjoxNjgxODQ0MDkwfQ.Y_s_KI2d6mxzeVJ-ekL2Gz7aZEsGFxVErjyA3uWWkdc";
	    	
	    	//AES256Util aes256 = AES256Util.getInstance();
	    	
	    	String encStr = urlEncode(token, secretKey, iv);
	    	
	    	String decStr =  urlDecode(encStr, secretKey, iv);

	    	System.out.println("token : "+ token);
	    	System.out.println("encode : "+ encStr);
	    	System.out.println("decode2 : "+ decStr );	

	    	
	    	System.out.println("============================ send email info ====================================");
	    	String appPassword ="hhbpaivglgkdhunt";
	    	
	    	encStr = encode(appPassword);
	    	
	    	decStr =  decode(encStr);

	    	System.out.println("token : "+ token);
	    	System.out.println("encode : "+ encStr);
	    	System.out.println("decode2 : "+ decStr );	
	    	
	    	
	    	
	    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
	}
	*/
}
