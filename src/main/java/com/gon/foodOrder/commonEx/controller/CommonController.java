package com.sharp.common.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.sharp.common.constant.CommonConstant;
import com.sharp.common.service.CommonService;
import com.sharp.common.utils.CommonUtil;

import kr.msp.constant.Const;

/**
 * 공통 컨트롤러
 */
@Controller
@RequestMapping("/common")
public class CommonController {
    private  Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());

    @Qualifier("messageSource")
    @Autowired(required=true)
    private MessageSource messageSource;

	@Autowired
    private CommonUtil commonUtil;   

	@Autowired
    private CommonService commonService;
	
    
	/*
	 * 앱버전 조회
	 */
    @RequestMapping(method= RequestMethod.POST, value="/appVersion",produces = CommonConstant.CONTROLLER_DEFAULT_PRODUCES)
    public ModelAndView appVersion(HttpServletRequest request, HttpServletResponse response)  throws Exception {

    	LOGGER.debug("[CommonController] appVersion::");
    	

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
        if(reqHeadMap==null){
            reqHeadMap = new HashMap<String, Object>();
        }
        reqHeadMap.put(Const.RESULT_CODE, Const.OK);
        reqHeadMap.put(Const.RESULT_MESSAGE, Const.SUCCESS);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        ModelAndView mv = new ModelAndView( CommonConstant.MODELVIEW_DEFAULT_JSON_VIEW );
        try{
            /**************************************************************************************************
             * 이 부분에 비즈니스 로직을 코딩한다.
             * 만약, 클라이언트에 에러처리를 하고 싶다면  responseMap.setResultCode(Const.EXCEPTION_ERROR); 사용
             **************************************************************************************************/
        	
        	
        	//진단결과마스터 조회
        	responseBodyMap = commonService.selectAppVersion(reqBodyMap);     
        	
        	LOGGER.debug("reqBodyMap" + reqBodyMap.toString());
        	   	
        	
            /**************************************************************************************************
             * 이 부분에 비즈니스 로직 마침.
             *************************************************************************************************/
        } catch (Exception e) {
            reqHeadMap.put(Const.RESULT_CODE,Const.EXCEPTION_ERROR);
            if(e.getMessage() != null){
                reqHeadMap.put(Const.RESULT_MESSAGE,e.getMessage());
            } else {
                reqHeadMap.put(Const.RESULT_MESSAGE,messageSource.getMessage("500.error", null , Locale.getDefault() ));
            }
	    	responseBodyMap = commonUtil.failResult();
        }
        
        mv.addObject(Const.HEAD,reqHeadMap);
        mv.addObject(Const.BODY,responseBodyMap);
        
        return mv;        
    }
 
    
}
