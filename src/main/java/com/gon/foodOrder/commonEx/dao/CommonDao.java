package com.sharp.common.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;

@Repository
public class CommonDao {
	
    @Autowired(required=true)
    @Qualifier("sqlSession_app")  ///WEB-INF/config/context/sample-mybatis-context.xml파일에서 설정한 DB 연결세션
    private SqlSession sqlSession;
    
    @Autowired(required=true)
    @Qualifier("transactionManager_app")
    private DataSourceTransactionManager transactionManager;    
    
    /*
     * 앱버전 조회
     */
    public Map<String, Object> selectAppVersion(Map<String,Object> parameterMap) {
        return sqlSession.selectOne( "CommonSql.selectAppVersion", parameterMap);
    }  
    
}
