package com.dongkuksystems.dbox.config.support;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.HiddenHttpMethodFilter;

public class InputStreamWrapperFilter extends HiddenHttpMethodFilter {
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		ServletRequest servletRequest = request;
		
		String uri = request.getRequestURI();
		AntPathMatcher pathMatcher = new AntPathMatcher();
		if (pathMatcher.match("/kupload/**", uri)) {
			servletRequest = new InputStreamHttpServletRequestWrapper(request);
		}
		
		filterChain.doFilter(servletRequest, response);
	}
}