package com.sharp.common.controller;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.sharp.common.constant.CommonConstant;
import com.sharp.common.constant.ParamConstant;
import com.sharp.common.utils.CommonUtil;
import com.sharp.common.utils.SendQueue;

import kr.msp.constant.Const;

/**
 * Created with IntelliJ IDEA.
 * User: mium2
 * Date: 14. 3. 18
 * Time: 오후 6:31
 * 서버 클라이언트 연동 파일업로드 사용 방법 샘플
 */

@Controller
public class CommonUploadController {
    private Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    @Autowired(required=true)
    private MessageSource messageSource;

    @Value("${logUpload.path}")
    private String LOG_UPLOAD_PATH;

    @Value("${logUpload.queue_path}")
    private String LOG_QUEUE_PATH;
    
    @Value("${logUpload.fileExtention.allow}")
    private String LOG_UPLOAD_FILE_EXTENTION;
    
    
	@Autowired
    private CommonUtil commonUtil;

	@Autowired
    private SendQueue sendQueue;
	
	
	@ResponseBody
    @RequestMapping(value="/app/common/fileUpload/{rsrcId}",method= RequestMethod.POST,produces = CommonConstant.CONTROLLER_DEFAULT_PRODUCES)
    public Map<String,Object> mobileTempUploadPost(HttpServletRequest request, HttpServletResponse response,  @PathVariable String rsrcId){

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 클라이언트에서 넘어온 request 값  map으로 리턴해줌 (반드시 포함)
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //rest로 넘어온 URI Path VARIABLES ATTRIBUTE 맵정보
        Map<String,Object> uriPathVal = (Map<String,Object>)request.getAttribute(Const.REST_URI_PATH_VAL);
        //클라이언트에서 넘어온 request(HEAD+BODY) 모든정보
        Map<String,Object> reqMap =  (Map<String,Object>)request.getAttribute(Const.HTTP_BODY);
        //클라이언트에서 넘어온 공통 헤더 맵정보
        Map<String,Object> reqHeadMap =  (Map<String,Object>)request.getAttribute(Const.HEAD);
        //클라이언트에서 넘긴 파라미터 맵정보
        Map<String,Object> reqBodyMap =  (Map<String,Object>)request.getAttribute(Const.BODY);

        //클라이언트에서 넘길 Response 맵 세팅
        Map<String,Object> responseMap = new HashMap<String, Object>();
        Map<String, Object> responseBodyMap= new HashMap<String, Object>();
        if(reqHeadMap==null){ //restclient를 이용하면 raw데이타가 없기 때문
            reqHeadMap = new HashMap<String, Object>();
        }
        reqHeadMap.put(Const.RESULT_CODE, Const.OK);
        reqHeadMap.put(Const.RESULT_MESSAGE, Const.SUCCESS);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //responseMap = commonUtil.successResult();
        
        LOGGER.debug("[CommonUploadController] mobileTempUploadPost:"+rsrcId);
        
        try{
            /**************************************************************************************************
             * 이 부분에 비즈니스 로직을 코딩한다.
             * 만약, 클라이언트에 에러처리를 하고 싶다면  responseMap.setResultCode(Const.EXCEPTION_ERROR); 사용
             **************************************************************************************************/
        	if(commonUtil.isEmpty(rsrcId)) {
        		new Exception();
        	}
        	rsrcId =  rsrcId.replaceAll("\\.", "_");
        	
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            LOGGER.debug("[CommonUploadController] isMultipart:"+isMultipart);
            
            if(isMultipart){
                //List<Map<String,Object>> imgInfoList = new ArrayList<Map<String, Object>>();
                MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

                final Map<String, MultipartFile> files = multipartRequest.getFileMap();
                Iterator<Map.Entry<String, MultipartFile>> itr = files.entrySet().iterator();
                MultipartFile file;
                String filePath = "";
                String uploadFileName = "";
                boolean uploadCheck =  false;
                String queueFilePath = "";
                
                while (itr.hasNext()) {
                    Map.Entry<String, MultipartFile> entry = itr.next();
                    LOGGER.debug("[" + entry.getKey() + "]");
                    file = entry.getValue();

                    if (!"".equals(file.getOriginalFilename())) {
                        uploadFileName = file.getOriginalFilename();
                        String fileExtention = uploadFileName.substring(uploadFileName.lastIndexOf(".")+1,uploadFileName.length()).toLowerCase();

                        if(LOG_UPLOAD_FILE_EXTENTION.indexOf(fileExtention) < 0) {
                        	throw new Exception("올바르지 않은 확장자 입니다");
                        }
                        
                        filePath = LOG_UPLOAD_PATH + File.separator + rsrcId + File.separator + commonUtil.getToday() + File.separator + file.getOriginalFilename();
                        queueFilePath = LOG_QUEUE_PATH + File.separator + rsrcId + File.separator + commonUtil.getToday() + File.separator + file.getOriginalFilename();
                        
                        File saveDir = new File(filePath);
                        if (!saveDir.getParentFile().exists()) {
                            saveDir.getParentFile().mkdirs();
                        }

                        file.transferTo(new File(filePath));

                        uploadCheck = true;
                    }
                    //Map<String,Object> imgInfoMap = new HashMap<String, Object>();
                    //imgInfoMap.put("httpurl",getServerHostURL(request)+"/"+filePath+"/"+uploadFileName);
                    //imgInfoMap.put("uploadFileName",uploadFileName);
                    //imgInfoMap.put("absPath",filePath);
                    //imgInfoList.add(imgInfoMap);/share_applog/uracle/mil/cesd10d/20230613/cesd10d-detail_9_xnX4yMD1zpdha9x_0_D_0_20230613_161244_000.json
                                		
            		LOGGER.debug("업로드 full url= "+filePath);
            		LOGGER.debug("queue 발송 url= "+queueFilePath);
                	sendQueue.sendUploadQueue(queueFilePath);
                }
                                
               // responseBodyMap.put("attachFiles",imgInfoList);
               // responseBodyMap.put("status","200");
                
                if(uploadCheck) {
                	responseBodyMap.put(ParamConstant.RESULT_CODE, 		CommonConstant.OK_CODE);
	                responseBodyMap.put(ParamConstant.RESULT_MESSAGE, 	commonUtil.getMessage(CommonConstant.OK_CODE) );
                }else {
                	//업로드 파일이 없습니다.
	                responseBodyMap.put(ParamConstant.RESULT_CODE, 		"0020" );
	                responseBodyMap.put(ParamConstant.RESULT_MESSAGE, 	commonUtil.getMessage("0020")  );
                }
            }else{
            	responseBodyMap.put(Const.RESULT_CODE,Const.EXCEPTION_ERROR);
            	responseBodyMap.put(Const.RESULT_MESSAGE,"ENCTYPE이 multipart/form-data가 아닙니다.");
            }

            /**************************************************************************************************
             * 이 부분에 비즈니스 로직 마침.
             *************************************************************************************************/
        } catch (Exception e) {
            reqHeadMap.put(Const.RESULT_CODE,Const.EXCEPTION_ERROR);
            if(e.getMessage() != null){
                reqHeadMap.put(Const.RESULT_MESSAGE,e.getMessage());
            } else {
                reqHeadMap.put(Const.RESULT_MESSAGE,messageSource.getMessage("500.error", null , Locale.getDefault().ENGLISH ));
            }
	    	responseBodyMap = commonUtil.failResult();
        }


        responseMap.put(Const.HEAD,	reqHeadMap);
        responseMap.put(Const.BODY,	responseBodyMap);
        
        return responseMap;        
    }

    
    public String getServerHostURL(HttpServletRequest request) {
        String sHostUrl = request.getScheme() + "://" + request.getServerName() +
                (request.getServerPort() > 0 ? ":" + request.getServerPort() : "") + request.getContextPath();
        sHostUrl = sHostUrl.endsWith("/") ? sHostUrl : sHostUrl + "/";
        return sHostUrl;
    }
    
    @ResponseBody
    @RequestMapping(value="/app/common/fileUploadDetail",method= RequestMethod.POST,produces = CommonConstant.CONTROLLER_DEFAULT_PRODUCES)
    public Map<String,Object> mobileDetailPost(HttpServletRequest request, HttpServletResponse response){

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 클라이언트에서 넘어온 request 값  map으로 리턴해줌 (반드시 포함)
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //rest로 넘어온 URI Path VARIABLES ATTRIBUTE 맵정보
        Map<String,Object> uriPathVal = (Map<String,Object>)request.getAttribute(Const.REST_URI_PATH_VAL);
        //클라이언트에서 넘어온 request(HEAD+BODY) 모든정보
        Map<String,Object> reqMap =  (Map<String,Object>)request.getAttribute(Const.HTTP_BODY);
        //클라이언트에서 넘어온 공통 헤더 맵정보
        Map<String,Object> reqHeadMap =  (Map<String,Object>)request.getAttribute(Const.HEAD);
        //클라이언트에서 넘긴 파라미터 맵정보
        Map<String,Object> reqBodyMap =  (Map<String,Object>)request.getAttribute(Const.BODY);

        //클라이언트에서 넘길 Response 맵 세팅
        Map<String,Object> responseMap = new HashMap<String, Object>();
        Map<String, Object> responseBodyMap= new HashMap<String, Object>();
        if(reqHeadMap==null){ //restclient를 이용하면 raw데이타가 없기 때문
            reqHeadMap = new HashMap<String, Object>();
        }
        reqHeadMap.put(Const.RESULT_CODE, Const.OK);
        reqHeadMap.put(Const.RESULT_MESSAGE, Const.SUCCESS);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //responseMap = commonUtil.successResult();
        
        try{
            /**************************************************************************************************
             * 이 부분에 비즈니스 로직을 코딩한다.
             * 만약, 클라이언트에 에러처리를 하고 싶다면  responseMap.setResultCode(Const.EXCEPTION_ERROR); 사용
             **************************************************************************************************/
        	
        	String rsrcId = "";//commonUtil.nvlString(reqBodyMap.get(ParamConstant.RSRC_ID));
        	String uploadFileName = commonUtil.nvlString(reqBodyMap.get(ParamConstant.FILE_NAME));
        	
        	String fileDetail = commonUtil.nvlString(reqBodyMap.get(ParamConstant.FILE_DETAIL));
        	
        	rsrcId =  rsrcId.replaceAll("\\.", "_");
        	
        	try {
        		
        		String filePath = "";
        		String queueFilePath = "";
        		
        		filePath = LOG_UPLOAD_PATH + File.separator + rsrcId + File.separator + commonUtil.getToday()  + File.separator + uploadFileName;
                queueFilePath = LOG_QUEUE_PATH + File.separator + rsrcId + File.separator + commonUtil.getToday() + File.separator + uploadFileName;
                
        		File saveDir = new File(filePath);
                if (!saveDir.getParentFile().exists()) {
                    saveDir.getParentFile().mkdirs();
                }
                
        		BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(saveDir.getPath()), "UTF8"));
        		output.write(fileDetail);
        		output.close();
        		
        		//String queueFilePath = filePath.replace("/share_applog/", "");
        		
        		LOGGER.debug("업로드 full url= "+filePath);
        		LOGGER.debug("queue 발송 url= "+queueFilePath);
            	sendQueue.sendUploadQueue(queueFilePath);
            	
        	} catch(UnsupportedEncodingException uee) {
        		StackTraceElement[] stes = uee.getStackTrace();
            	for(StackTraceElement ste : stes) {
            		LOGGER.error("############[StackTraceElements]############");
            		LOGGER.error("# " + ste);
            	}
        		responseBodyMap.put(ParamConstant.RESULT_CODE, 		CommonConstant.FAIL_CODE);
                responseBodyMap.put(ParamConstant.RESULT_MESSAGE, 	uee.getMessage());
        	} catch(IOException ioe) {
        		StackTraceElement[] stes = ioe.getStackTrace();
            	for(StackTraceElement ste : stes) {
            		LOGGER.error("############[StackTraceElements]############");
            		LOGGER.error("# " + ste);
            	}
        		responseBodyMap.put(ParamConstant.RESULT_CODE, 		CommonConstant.FAIL_CODE);
                responseBodyMap.put(ParamConstant.RESULT_MESSAGE, 	ioe.getMessage());
        	}
        	
        	responseBodyMap.put(ParamConstant.RESULT_CODE, 		CommonConstant.OK_CODE);
            responseBodyMap.put(ParamConstant.RESULT_MESSAGE, 	messageSource.getMessage(CommonConstant.OK_CODE, null, LocaleContextHolder.getLocale()));        

            /**************************************************************************************************
             * 이 부분에 비즈니스 로직 마침.
             *************************************************************************************************/
        } catch (Exception e) {
            reqHeadMap.put(Const.RESULT_CODE,Const.EXCEPTION_ERROR);
            if(e.getMessage() != null){
                reqHeadMap.put(Const.RESULT_MESSAGE,e.getMessage());
            } else {
                reqHeadMap.put(Const.RESULT_MESSAGE,messageSource.getMessage("500.error", null , Locale.getDefault().ENGLISH ));
            }
	    	responseBodyMap = commonUtil.failResult();
        }
        
        responseMap.put(Const.HEAD,	reqHeadMap);
        responseMap.put(Const.BODY,	responseBodyMap);
        
        return responseMap;        
    }
}
