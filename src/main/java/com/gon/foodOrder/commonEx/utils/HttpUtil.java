package com.sharp.common.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharp.common.constant.CommonConstant;
import com.sharp.common.constant.ParamConstant;

@Component
public class HttpUtil {

	private  static Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
		
	private @Value("${mail.smtp.host}")				String MAIL_HOST;
	private @Value("${mail.smtp.port}") 			String MAIL_PORT;
	private @Value("${mail.smtp.auth}") 			String MAIL_AUTH;
	private @Value("${mail.smtp.starttls.enable}") 	String MAIL_STARTTLS_ENABLE;
	private @Value("${mail.smtp.ssl.trust}")		String MAIL_SSL_TRUST;
	
	private @Value("${mail.auth.id}")			String MAIL_AUTH_ID;
	private @Value("${mail.auth.key}")			String MAIL_AUTH_KEY;
	
	/*
	 * 발송 이메일 template 설정 정보
	 * 추가 방법
	 * 1. /src/main/resources/main/에 template 파일 추가
	 * 2. /src/main/resources/config/app-config.xml 에 설정 파일 위치 추가(<email><template>에 파일 위치 추가)
	 */
	private @Value("${email.template.joinAuthCode}")	String JOIN_AUTH_CODE;
	private @Value("${email.template.passwdInit}")		String PASSWD_INIT;
	
	private int SOCKET_TIMEOUT	= 10;
	private int CONNECT_TIMEOUT	= 10;
	private int READ_TIMEOUT	= 10;
	
	@Autowired
    private CommonUtil commonUtil;
	
	@Autowired
	private MessageSource messageSource;

	
	/**
	 * String to JSON
	 */	
	public JSONObject getJson(String jsonString) throws Exception {
		if( jsonString == null || jsonString == "" || (jsonString).equals("")) {
			return null;
		} else {
			JSONParser parser = new JSONParser();
			JSONObject resultJson = null;
			try {
				resultJson = (JSONObject)parser.parse(jsonString);
			} catch (ParseException pe) {
				LOGGER.error("getJson {} ", pe);
				return null;
			}
			return resultJson;
		}
	}	
	
	/**
	 * request paramter -> string (POST)
	 */	
	public String getRequestrData(HttpServletRequest request) throws Exception{
		
		HttpRequestWrapper httpRequestWrapper = new HttpRequestWrapper(request);
		
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader( new InputStreamReader(httpRequestWrapper.getInputStream(), CommonConstant.UTF8));
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line );
		}
		br.close();
				
		return sb.toString();
	}

	/**
	 * CMS HTTP POST 통신
	 * @param domain	도메인
	 * @param httpUrl	요청 URL
	 * @param sendData	요청 데이터
	 */
	public Map<String, Object> CmsHttpPost(String domain, String httpUrl, Map<String, Object> parameterMap) throws Exception {

		Map<String, Object> resultMap = new HashMap<>();
		if(commonUtil.isEmpty(domain) || commonUtil.isEmpty(httpUrl) || commonUtil.isEmpty(parameterMap)) {
			return resultMap;
		}
		
		//서버가 수신 및 응답이 가능한 상태인지 확인
		if(!serverAlive(domain)) {
			return resultMap;
		};

		try {
	    	//1.headers token 설정
			LOGGER.debug("[CmsHttpPost]1. headers token setting");
        	String cmsAccessToken = "";
        	String cmsRefreshToken = "";
        	
	    	Map<String, Object> headersMap = ( Map<String, Object>) parameterMap.get(CommonConstant.REQUEST_HEADERS);
	    	if(!(commonUtil.isEmpty(headersMap))){
	        	cmsAccessToken = (String) headersMap.get(ParamConstant.HEADER_CMS_ACCESS_TOKEN);
	        	cmsRefreshToken = (String) headersMap.get(ParamConstant.HEADER_CMS_REFRESH_TOKEN);
	    	}
        	        	
        	//2. body map 설정
			LOGGER.debug("[CmsHttpPost] 2. body map setting");
	    	Map<String, Object> requestMap = new HashMap<>();
	    	requestMap.put(CommonConstant.REQUEST_HEAD, parameterMap.get(CommonConstant.REQUEST_HEAD));
	    	requestMap.put(CommonConstant.REQUEST_BODY, parameterMap.get(CommonConstant.REQUEST_BODY));
	    	
			JSONObject json = new JSONObject(requestMap);
			StringEntity params = new StringEntity(json.toString(), "UTF-8");
			HttpPost httpRequest = null;
			String uri = domain + httpUrl;

			LOGGER.debug("[CmsHttpPost] 2.params : "+ json.toString());
			
			httpRequest = new HttpPost(uri);
			httpRequest.addHeader("content-type", "application/json; charset=UTF-8");
			//httpRequest.addHeader(ParamConstant.HEADER_ACCESS_TOKEN, cmsAccessToken);
			//httpRequest.addHeader(ParamConstant.HEADER_REFRESH_TOKEN, cmsRefreshToken);
			httpRequest.addHeader(CommonConstant.AUTHORIZATION, "Bearer " + cmsAccessToken);
			
			httpRequest.setEntity(params);
			
			RequestConfig requestConfig = RequestConfig.custom()
					  .setSocketTimeout(SOCKET_TIMEOUT*1000)
					  .setConnectTimeout(CONNECT_TIMEOUT*1000)
					  .setConnectionRequestTimeout(READ_TIMEOUT*1000)
					  .build();
			httpRequest.setConfig(requestConfig);
					
			LOGGER.debug("----------------------------------------");
			LOGGER.debug("Http execute");
			LOGGER.debug("----------------------------------------");
			LOGGER.debug("params:" + json.toString());
	
			resultMap = CmsHttpConnection(uri, httpRequest);
		} catch (ConnectException e) {
			LOGGER.error("CmsHttpPost() e : [" + e.getClass().getName() + "] " +  e.getMessage());
		}
		return resultMap;
	}
	
	
	/**
	 * CMS HTTP POST 통신
	 * @param uri
	 * @param httpRequest
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private Map<String, Object> CmsHttpConnection(String uri, HttpPost httpRequest) throws Exception {

		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> headersMap = new HashMap<>();
		
		CloseableHttpClient httpClient = getHttpClient();

		StringEntity params = null;
		HttpResponse httpResponse = null;
		String httpPost = null;
		JSONParser parser = new JSONParser();
		
		try {
			httpResponse = httpClient.execute(httpRequest);

			// 통신결과
			LOGGER.debug("----------------------------------------");
			LOGGER.debug("Http Result");
			LOGGER.debug("----------------------------------------");
			LOGGER.debug("httpResponse:" + httpResponse);

			// 통신 성공
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

				ResponseHandler<String> handler = new BasicResponseHandler();
				String result = handler.handleResponse(httpResponse);
//				LOGGER.debug("result:" + result);

				JSONObject resultJson = (JSONObject)parser.parse(result);

				resultMap = commonUtil.converterStringToMap(resultJson.toString());
				
				commonUtil.setSuccessResponseBody(resultMap);
				
				 // 헤더에서 token 가져오기
		        Header[] hp = httpResponse.getAllHeaders();
		        for (Header headers : hp) {
		        	
		        	if(headers.getName().equals(ParamConstant.HEADER_ACCESS_TOKEN)) {
		        		headersMap.put(ParamConstant.HEADER_CMS_ACCESS_TOKEN, headers.getValue());
		        	}
		        	if(headers.getName().equals(ParamConstant.HEADER_REFRESH_TOKEN)) {
		        		headersMap.put(ParamConstant.HEADER_CMS_REFRESH_TOKEN, headers.getValue());
		        	}
		        	LOGGER.debug("headers==>"+headers.getName()+" : "+headers.getValue());
		        }				
				
				
			} else {	// 통신 실패
				String entity = EntityUtils.toString(httpResponse.getEntity());
				JSONObject entityObj = (JSONObject) parser.parse(entity);

				resultMap = commonUtil.converterStringToMap(entityObj.toString());

				commonUtil.setFailResponseBody(resultMap);
				LOGGER.error("Error " + uri + " connecting to http ["
						+ httpResponse.getStatusLine().getStatusCode() + "]" + entity);
			}
			
		} catch (ClientProtocolException e) {
			LOGGER.error("HttpConnection() - " + uri + ".ClientProtocolException {} ", e);
			resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.FAIL_CODE	);
			resultMap.put(ParamConstant.RESULT_MESSAGE, messageSource.getMessage(CommonConstant.FAIL_CODE, null, LocaleContextHolder.getLocale())	);
			httpRequest.releaseConnection();
		} catch (IOException e) {
			LOGGER.error("HttpConnection() - " + uri + ".IOException {} ", e);
			resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.FAIL_CODE	);
			resultMap.put(ParamConstant.RESULT_MESSAGE, messageSource.getMessage(CommonConstant.FAIL_CODE, null, LocaleContextHolder.getLocale())	);
			httpRequest.releaseConnection();
		} finally {
			httpClient.close();
		}

		LOGGER.info("End of HttpConnection >>> return:" + resultMap);

		resultMap.put(CommonConstant.REQUEST_HEADERS, 	headersMap);
		return resultMap;
	}


	/**
	 * timeout
	 * @param timeout	초
	 */
	public CloseableHttpClient getHttpClient() {
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(SOCKET_TIMEOUT)
				.setSocketTimeout(SOCKET_TIMEOUT)
				.setConnectTimeout(CONNECT_TIMEOUT)
				.build();
		HttpClientBuilder httpClientBuilder = HttpClients.custom().setDefaultRequestConfig(requestConfig);
		return httpClientBuilder.build();
		
	}
	
	/**
	 * coocon HTTP POST 통신
	 * @param domain	도메인
	 * @param httpUrl	요청 URL
	 * @param sendData	요청 데이터
	 */
	public Map<String, Object> CooconHttpPost2(String domain, String httpUrl, String parameterData) throws Exception {

		Map<String, Object> resultMap = new HashMap<>();
		if(domain == null || commonUtil.isEmpty(domain) 
				|| httpUrl == null	|| commonUtil.isEmpty(httpUrl) 
				|| parameterData == null	|| parameterData.isEmpty()) {
			return resultMap;
		}
		
		//서버가 수신 및 응답이 가능한 상태인지 확인
		if(!serverAlive(domain)) {
			return resultMap;
		};
		
		String uri = domain + httpUrl;
		
		byte[] resMessage = null;

		
		//https 통신
		HttpsURLConnection conn;
		
		try {
		    //https 통신
		    conn = (HttpsURLConnection) new URL(uri).openConnection();
		    
		    conn.setDoInput(true); 
		    conn.setDoOutput(true);
		    conn.setRequestMethod("POST");
		    conn.setUseCaches(false);
		    OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
		    
		    os.write(parameterData);
		    os.flush();
		    os.close();

		    DataInputStream in = new DataInputStream(conn.getInputStream());
		    ByteArrayOutputStream bout = new ByteArrayOutputStream();
		    int bcount = 0;
		    byte[] buf = new byte[2048];
		    while (true) {
		        int n = in.read(buf);
		        if (n == -1) break;
		        bout.write(buf, 0, n);
		    }

		    bout.flush(); 
		    resMessage = bout.toByteArray();
		    conn.disconnect(); 
		    JSONParser parser = new JSONParser();
		    
		    String temp = new String(resMessage, "UTF-8");
		    temp = temp.replaceAll("\r\n","");
		    temp = temp.replaceAll("\r","");
		    temp = temp.replaceAll("\n","");
		    
		    // 통신 성공
		    if ( conn.getResponseCode() == HttpStatus.SC_OK) {
		    	
//		    	ResponseHandler<String> handler = new BasicResponseHandler();
//		    	String result = handler.handleResponse((HttpResponse) conn.getContent());
		    	
//		 		LOGGER.debug("result:" + result);
		    	
//		    	JSONObject resultJson = (JSONObject)parser.parse(result);
//		    	resultMap = commonUtil.converterStringToMap(resultJson.toString());
		    	
		    	LOGGER.debug("result:" + temp);
		    	
		    	resultMap = commonUtil.convertObjectToMap(temp);
		    	
		    	commonUtil.setSuccessResponseBody(resultMap);		    	
		    	
		    } else {	// 통신 실패
//		    	String entity = EntityUtils.toString(httpResponse.getEntity());
//		    	JSONObject entityObj = (JSONObject) parser.parse(entity);
		    	
		    	resultMap = commonUtil.converterStringToMap(conn.getResponseMessage());
		    	
		    	commonUtil.setFailResponseBody(resultMap);
		    	LOGGER.error("Error " + uri + " connecting to http ["
		    			+ conn.getResponseCode() + "]");
		    }
		    
		} catch (ClientProtocolException e) {
			LOGGER.error("HttpConnection() - " + uri + ".ClientProtocolException {} ", e);
			resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.FAIL_CODE	);
			resultMap.put(ParamConstant.RESULT_MESSAGE, messageSource.getMessage(CommonConstant.FAIL_CODE, null, LocaleContextHolder.getLocale())	);

		} catch (IOException e) {
			LOGGER.error("HttpConnection() - " + uri + ".IOException {} ", e);
			resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.FAIL_CODE	);
			resultMap.put(ParamConstant.RESULT_MESSAGE, messageSource.getMessage(CommonConstant.FAIL_CODE, null, LocaleContextHolder.getLocale())	);
			
		} finally {
//			conn.disconnect();
		}

		return resultMap;
	}
	

	
	/**
	 * coocon HTTP POST 통신
	 * @param domain	도메인
	 * @param httpUrl	요청 URL
	 * @param sendData	요청 데이터
	 */
	public Map<String, Object> CooconHttpPost(String domain, String httpUrl, String parameterData) throws Exception {

		Map<String, Object> resultMap = new HashMap<>();
		if(domain == null || commonUtil.isEmpty(domain) 
				|| httpUrl == null	|| commonUtil.isEmpty(httpUrl) 
				|| parameterData == null	|| parameterData.isEmpty()) {
			return resultMap;
		}
		
		//서버가 수신 및 응답이 가능한 상태인지 확인
		if(!serverAlive(domain)) {
			return resultMap;
		};
		
		try {

//			JSONObject json = new JSONObject(parameterMap);
			StringEntity params = new StringEntity(parameterData, "UTF-8");			
	
			HttpPost httpRequest = null;
			String uri = domain + httpUrl;
			
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			
			list.add(new BasicNameValuePair("JSONData", parameterData));
			
			httpRequest = new HttpPost(uri);
			httpRequest.addHeader("content-type", "application/json; charset=UTF-8");
//			httpRequest.setEntity(params);
			httpRequest.setEntity(new UrlEncodedFormEntity(list));
			
			RequestConfig requestConfig = RequestConfig.custom()
					  .setSocketTimeout(SOCKET_TIMEOUT*1000)
					  .setConnectTimeout(CONNECT_TIMEOUT*1000)
					  .setConnectionRequestTimeout(READ_TIMEOUT*1000)
					  .build();
			httpRequest.setConfig(requestConfig);
	
			LOGGER.debug("----------------------------------------");
			LOGGER.debug("Http execute");
			LOGGER.debug("----------------------------------------");
			LOGGER.debug("params:" + parameterData);

			resultMap = CooConHttpConnection(uri, httpRequest);
		
		} catch (ConnectException e) {
			LOGGER.error("CmsHttpPost() e : [" + e.getClass().getName() + "] " +  e.getMessage());
		}
		return resultMap;
	}
	
	
	/**
	 * COOCON HTTP POST 통신
	 * @param uri
	 * @param httpRequest
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private Map<String, Object> CooConHttpConnection(String uri, HttpPost httpRequest) throws Exception {

		Map<String, Object> resultMap = new HashMap<>();
		
		CloseableHttpClient httpClient = getHttpClient();

		StringEntity params = null;
		HttpResponse httpResponse = null;
		String httpPost = null;
		JSONParser parser = new JSONParser();
		
		try {
			httpResponse = httpClient.execute(httpRequest);

			// 통신결과
			LOGGER.debug("----------------------------------------");
			LOGGER.debug("Http Result");
			LOGGER.debug("----------------------------------------");
			LOGGER.debug("httpResponse:" + httpResponse);

			// 통신 성공
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

				ResponseHandler<String> handler = new BasicResponseHandler();
				String result = handler.handleResponse(httpResponse);
//				LOGGER.debug("result:" + result);

				JSONObject resultJson = (JSONObject)parser.parse(result);
				resultMap = commonUtil.converterStringToMap(resultJson.toString());
				
				commonUtil.setSuccessResponseBody(resultMap);
								
				
			} else {	// 통신 실패
				String entity = EntityUtils.toString(httpResponse.getEntity());
				JSONObject entityObj = (JSONObject) parser.parse(entity);

				resultMap = commonUtil.converterStringToMap(entityObj.toString());

				commonUtil.setFailResponseBody(resultMap);
				LOGGER.error("Error " + uri + " connecting to http ["
						+ httpResponse.getStatusLine().getStatusCode() + "]" + entity);
			}
			
		} catch (ClientProtocolException e) {
			LOGGER.error("HttpConnection() - " + uri + ".ClientProtocolException {} ", e);
			resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.FAIL_CODE	);
			resultMap.put(ParamConstant.RESULT_MESSAGE, messageSource.getMessage(CommonConstant.FAIL_CODE, null, LocaleContextHolder.getLocale())	);
			httpRequest.releaseConnection();
		} catch (IOException e) {
			LOGGER.error("HttpConnection() - " + uri + ".IOException {} ", e);
			resultMap.put(ParamConstant.RESULT_CODE, 	CommonConstant.FAIL_CODE	);
			resultMap.put(ParamConstant.RESULT_MESSAGE, messageSource.getMessage(CommonConstant.FAIL_CODE, null, LocaleContextHolder.getLocale())	);
			httpRequest.releaseConnection();
		} finally {
			httpClient.close();
		}

		LOGGER.info("End of HttpConnection >>> return:" + resultMap);
		
		return resultMap;
	}
	
	
	/**
	 * HTTP POST 통신
	 * @param domain	도메인
	 * @param httpUrl	요청 URL
	 * @param sendData	요청 데이터
	 */
	public JSONObject httpPost(String[] domain, String httpUrl, JSONObject sendData) throws Exception {
		JSONObject resultJson = new JSONObject();
		String serverIp = getServerAlive(domain);
        HttpURLConnection con = getConnection(serverIp + httpUrl, CommonConstant.HTTP_POST);
        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        
    	LOGGER.debug("############[ Http 접속 정보 ]############");
    	LOGGER.debug("# 연결 주소 [ " + serverIp + httpUrl, CommonConstant.HTTP_POST + " ]");
        
        if(null != sendData) {
        	wr.write(JSONObject.toJSONString(sendData));
        }
        wr.flush();

        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader( new InputStreamReader(con.getInputStream(), CommonConstant.UTF8));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line );
            }
            br.close();
            
        }
        con.disconnect();
        
        LOGGER.debug("############[ Http 응답 정보 ]############");
        LOGGER.debug("# 응답 데이터 [ " + sb.toString() + " ]");
     
        JSONParser parser   = new JSONParser();
        resultJson 			= (JSONObject)parser.parse( sb.toString() );
        
        
        // 응답 헤더의 정보를 모두 출력
        for (Map.Entry<String, List<String>> header : con.getHeaderFields().entrySet()) {
            for (String value : header.getValue()) {
                System.out.println(header.getKey() + " : " + value);
            }
        }
        
        
        return resultJson;
	}
	
	/**
	 * 타겟 서버 헬스채크
	 */
	public String getServerAlive(String[] httpUrl) throws Exception {
		String resultIp = "";
		boolean isNodeAlive = false;
		
		for(int i = 0 ; i < httpUrl.length; i ++) {
			boolean isAlive = serverAlive(httpUrl[i]);
			
			if(isAlive) {
				resultIp = httpUrl[i];
				isNodeAlive = true;
				break;
			}
		}
		
		if(!isNodeAlive) {
			throw new Exception("연결가능한 서버가 존재하지 않습니다.");
		} else {
			LOGGER.debug("# 연결 가능 서버 [ " + resultIp + " ]");
		}
		return resultIp;
	}
	
	/**
	 * 서버가 수신 및 응답이 가능한 상태인지 확인
	 */

	@SuppressWarnings("unchecked")
	protected boolean serverAlive(String domain){
		
		boolean resultBoolean 	= false;
		HttpURLConnection con 	= null;
		BufferedReader br  		= null;
		
		try {
			con = getConnection(domain, CommonConstant.HTTP_GET);
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			
			JSONObject sendData = new JSONObject();
	        sendData.put(CommonConstant.REQUEST_HEAD, new JSONObject());
	        sendData.put(CommonConstant.REQUEST_BODY, new JSONObject());
			
			wr.write(JSONObject.toJSONString(sendData));
        	wr.flush();
        	
            getResponseData(con);
	        con.disconnect();	
	        
        	resultBoolean = true;
		} catch(MalformedURLException mue) {
			LOGGER.error(MalformedURLException.class.getName(), mue);
			return false;
		} catch(IOException ioe) {
			LOGGER.error(IOException.class.getName(), ioe);
			return false;
		} catch(Exception e) {
			LOGGER.error(Exception.class.getName(), e);
			return false;
		} finally {
			if(null != br) {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.error("HttpUtil.serverAlive", e);
				}
			}
			if(null != con) {
				con.disconnect();
				
			}
		}
		return resultBoolean;		
	}
	
	@SuppressWarnings("unchecked")
	protected boolean serverAlive(String domain, String methodType){
		boolean resultBoolean 	= false;
		HttpURLConnection con 	= null;
		BufferedReader br  		= null;
		
		try {
			con = getConnection(domain, methodType);
			OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
			
			JSONObject sendData = new JSONObject();
	        sendData.put(CommonConstant.REQUEST_HEAD, new JSONObject());
	        sendData.put(CommonConstant.REQUEST_BODY, new JSONObject());
			
			wr.write(JSONObject.toJSONString(sendData));
        	wr.flush();
        	
            getResponseData(con);
	        con.disconnect();	
	        
        	resultBoolean = true;
		} catch(MalformedURLException mue) {
			LOGGER.error(MalformedURLException.class.getName(), mue);
			return false;
		} catch(IOException ioe) {
			LOGGER.error(IOException.class.getName(), ioe);
			return false;
		} catch(Exception e) {
			LOGGER.error(Exception.class.getName(), e);
			return false;
		} finally {
			if(null != br) {
				try {
					br.close();
				} catch (IOException e) {
					LOGGER.error("HttpUtil.serverAlive", e);
				}
			}
			if(null != con) {
				con.disconnect();
				
			}
		}
		return resultBoolean;
	}
	
	/**
	 * httpURLConnection Default 설정  
	 */
	protected HttpURLConnection getConnection(String httpUrl) throws MalformedURLException, IOException {
		URL url = new URL(httpUrl);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		con.setConnectTimeout(CONNECT_TIMEOUT*1000);
		con.setReadTimeout(READ_TIMEOUT*1000);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type"	, "application/json;charset=UTF-8");
        con.setRequestProperty("Accept"			, "application/json;charset=UTF-8");
        
        return con;
	}
	
	/**
	 * httpURLConnection Default 설정  
	 */
	protected HttpURLConnection getConnection(String httpUrl, String reqeustMethod) throws MalformedURLException, IOException {
		HttpURLConnection con = getConnection(httpUrl);
		
		if(!commonUtil.nvlString(reqeustMethod).equals("")) {
			con.setRequestMethod(reqeustMethod);
		}
		
		return con;
	}
	
	/**
	 * request paramter -> string (GET)
	 */		
	public String getRequestDataGetType(HttpServletRequest request) throws Exception{

		ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = new HashMap<>();
        String json = "";
        
		// Http Request Parameter 로그
		Enumeration parameterNames = request.getParameterNames();
                
		while (parameterNames.hasMoreElements()) {
			String name = parameterNames.nextElement() != null ? String.valueOf(parameterNames.nextElement()) : "" ;
			String value = request.getParameter(name);
			map.put(name, value);
		}
        try {
            // convert map to JSON string
            json = mapper.writeValueAsString(map);	//compact-type

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return json;		
	}
	

	/**
	 * 응답 데이터 추출
	 * @throws IOException 
	 */
	public String getResponseData(HttpURLConnection con) throws IOException  {
		StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
        	BufferedReader br = null;
        	try {
        		br = new BufferedReader( new InputStreamReader(con.getInputStream(), CommonConstant.UTF8));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line );
                }
        	} catch (IOException e) {
        		LOGGER.error("CommonUtil.getResponseData {} ", e);
			} finally {
				if(br != null) {
					br.close();
				}
			}
        }
        return sb.toString();
	}	
}