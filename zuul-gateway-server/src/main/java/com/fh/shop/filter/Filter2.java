package com.fh.shop.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class Filter2 extends ZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        StringBuffer requestURL = request.getRequestURL();
        log.info("**************{}", requestURL);
        HttpServletResponse response = currentContext.getResponse();
        String responseBody = currentContext.getResponseBody();
        log.info("响应结果{}", responseBody);
        InputStream stream = RequestContext.getCurrentContext().getResponseDataStream();
            String body = IOUtils.toString(stream);
        log.info("响应结果____{}", body);
        RequestContext currentContext1 = RequestContext.getCurrentContext();
        HttpServletResponse response1 = currentContext1.getResponse();
//        response1.setContentType("application/json;charset=utf-8");
        currentContext1.setResponseBody(body);

        return null;
    }
}
