package org.tekila.musikjunker.web.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Spring interceptor for cross-origine ressource sharing (CORS)
 * @author lc
 *
 */
public class CorsInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, Object handler) throws Exception {
		httpResponse.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with, my-cool-header");
		httpResponse.addHeader("Access-Control-Max-Age", "60"); // seconds to cache preflight request --> less OPTIONS traffic
		httpResponse.addHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		httpResponse.addHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
		
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		
	}

}
