package com.yangyang.starter;

import org.springframework.cloud.sleuth.Tracer;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author chenshunyang
 * @create 2018-04-02 16:36
 **/
public class SleuthWebTraceFilter implements Filter {

    private final Tracer tracer;

    public SleuthWebTraceFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //通过tracer.getCurrentSpan().traceIdString()方法获取traceId并放到header中
        response.addHeader("X-TrackId", this.tracer.getCurrentSpan().traceIdString());
        // 允许前端访问该请求头
        response.addHeader("Access-Control-Expose-Headers", "X-TrackId");
        chain.doFilter(servletRequest, response);
    }

    @Override
    public void destroy() {

    }
}
