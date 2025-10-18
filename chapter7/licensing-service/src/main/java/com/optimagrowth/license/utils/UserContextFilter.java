package com.optimagrowth.license.utils;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserContextFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(UserContextFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        UserContext context = new UserContext();
        context.setCorrelationId(httpServletRequest.getHeader(UserContext.CORRELATION_ID));
        context.setUserId(httpServletRequest.getHeader(UserContext.USER_ID));
        context.setAuthToken(httpServletRequest.getHeader(UserContext.AUTH_TOKEN));
        context.setOrganizationId(httpServletRequest.getHeader(UserContext.ORGANIZATION_ID));

        UserContextHolder.setContext(context);

        logger.debug("UserContextFilter Correlation id: {}", context.getCorrelationId());

        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}