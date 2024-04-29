package com.sharp.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

@Component
public class commonCorsFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletResponse responseCors = (HttpServletResponse)response;
		responseCors.setHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, HEAD, OPTIONS"); 
		responseCors.setHeader("Access-Control-Max-Age", "3600");
		responseCors.setHeader("Access-Control-Allow-Headers", "x-requested-with, origin, content-type, accept, access_token, refresh_token"); 
		responseCors.setHeader("Access-Control-Allow-Origin", "*"); 
		responseCors.setHeader("Access-Control-Expose-Headers", "access_token, refresh_token");
		chain.doFilter(request, responseCors);
		
	}

	@Override
	public void destroy() {		
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {		
	}

}
