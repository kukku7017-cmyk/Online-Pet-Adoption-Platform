package com.petadoption.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter("/*")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;

        // Security Headers
        httpRes.setHeader("X-Content-Type-Options", "nosniff");
        httpRes.setHeader("X-Frame-Options", "DENY");
        httpRes.setHeader("X-XSS-Protection", "1; mode=block");

        // Handle Cross-Origin Requests safely for separate frontend servers and local files
        String origin = httpReq.getHeader("Origin");
        if (origin != null && !origin.isBlank() && !"null".equalsIgnoreCase(origin)) {
            httpRes.setHeader("Access-Control-Allow-Origin", origin);
            httpRes.setHeader("Access-Control-Allow-Credentials", "true");
        } else {
            httpRes.setHeader("Access-Control-Allow-Origin", "*");
        }
        httpRes.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        httpRes.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, X-CSRF-Token, Authorization");

        if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
            httpRes.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }
}
