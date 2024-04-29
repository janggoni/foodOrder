package com.sharp.common.utils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <PRE>
 * 1. ClassName :
 * 2. FileName  : HttpRequestWrapperFilter.java
 * 3. Package  : kr.go.seoul.common.util
 * 4. Comment  : json string 으로 파라매터를 던질시에 request inputstream을 읽어오는데 특성상
 * 					한번밖에 읽어오지 못하므로 interceptor등에서 먼저 사용할 경우 실제 controller 단에서 읽지 못하는 상황발생
 * 					filter 를 이용해 inputstream을 따로 저장하여놓고 저장되어 있는데이터를 계속 가지고 오는것을 대체
 * 				 특정 URL에 맵핑하여 해당 filter를 사용하도록 설정해야함(web.xml)
 * 5. 작성자   : JJH
 * 6. 작성일   : 2018. 10. 19. 오후 3:54:10
 * </PRE>
 */
public class HttpRequestWrapperFilter implements Filter {

	protected static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestWrapperFilter.class);

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpServletRequest);

		// 실제 filter 적용
		chain.doFilter(requestWrapper, response);

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

}
