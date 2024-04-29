package com.sharp.common.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;

import com.sharp.common.constant.ParamConstant;
import com.sharp.common.constant.ValidConstant;
import com.sharp.common.dao.CommonDao;
import com.sharp.common.utils.CommonUtil;

@Service
public class CommonService {

	private  Logger LOGGER = LoggerFactory.getLogger(this.getClass().getName());
    
	@Autowired(required=true)
    @Qualifier("transactionManager_app")
    private DataSourceTransactionManager transactionManager;    
    
	@Autowired
    private CommonUtil commonUtil;
    
	@Autowired
    private CommonDao commonDao;
		
	/**
	 * 앱버전 조회
	 */
	public Map<String, Object> selectAppVersion(Map<String, Object> parameterMap) {
		
		// 필수 파라메터 체크
    	if(commonUtil.validParams(parameterMap, ValidConstant.APPVERSION_PARAM_KEYS)) {
    		return commonUtil.validResult(parameterMap, ValidConstant.APPVERSION_PARAM_KEYS);
    	}
    	
    	Map<String,Object> paramMap = new HashMap<String,Object>();
    	Map<String,Object> responseMap = new HashMap<String,Object>();
    	
    	try {
    		String deviceType = commonUtil.nvlString(	parameterMap.get(ParamConstant.DEVICE_TYPE)	);
    		
    		paramMap.put(ParamConstant.DEVICE_TYPE,	deviceType.toUpperCase());
    		
    		responseMap = commonDao.selectAppVersion(paramMap);
    		
			if(responseMap == null || responseMap.isEmpty()) {
				responseMap = new HashMap<String,Object>();
				responseMap.put(ParamConstant.DEVICE_TYPE, 	paramMap.get(ParamConstant.DEVICE_TYPE));
				responseMap.put("currentAppVersion", 		"");
				responseMap.put("requiredAppVersion", 		"");
				responseMap.put("regDate",					"");
				
			}
		}catch(Exception e) {
			LOGGER.error("error : "+e.toString());
			//9000, DB 연동 오류가 발생하였습니다.
			return commonUtil.dbFailResult();
		}
		return responseMap;
	}

	
	/**
	 * 공통그룹코드 조회
	 */
	public Map<String, Object> selectCommonCodeGrp(Map<String, Object> parameterMap) {

		Map<String,Object> responseMap = new HashMap<String,Object>();
		
		try {
			
			responseMap.put(ParamConstant.OBJECT_MAP, commonDao.selectCommonCodeGrp(parameterMap));
			
		}catch(Exception e) {
			LOGGER.error("error : "+e.toString());
			//9000, DB 연동 오류가 발생하였습니다.
			return commonUtil.dbFailResult();
		}
		return responseMap;
	}
	
		
	/**
	 * 공통코드 조회
	 */
	public Map<String, Object> selectCommonCd(Map<String, Object> parameterMap) {
		
		// 필수 파라메터 체크
		if(commonUtil.validParams(parameterMap, ValidConstant.COMMON_CODE_PARAM_KEYS)) {
			return commonUtil.validResult(parameterMap, ValidConstant.COMMON_CODE_PARAM_KEYS);
		}
		
		Map<String,Object> paramMap = new HashMap<String,Object>();
		Map<String,Object> responseMap = new HashMap<String,Object>();
		
		try {
			String cdGrpId = commonUtil.nvlString(	parameterMap.get(ParamConstant.CD_GRP_ID)	);
			
			paramMap.put(ParamConstant.CD_GRP_ID,	cdGrpId.toUpperCase());
			List<Map<String, Object>> list = commonDao.selectCommonCd(parameterMap);
			responseMap.put(ParamConstant.SHARP_LIST, list);
			
		}catch(Exception e) {
			LOGGER.error("error : "+e.toString());
			//9000, DB 연동 오류가 발생하였습니다.
			return commonUtil.dbFailResult();
		}
		return responseMap;
	}	
	
}
