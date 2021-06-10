package com.fh.shop.filter;

import com.alibaba.fastjson.JSON;
import com.fh.shop.common.Constants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.util.Md5Util;
import com.fh.shop.util.RedisUtil;
import com.fh.shop.vo.MemberVo;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TokenFilter extends ZuulFilter {

    @Value("${check.urls}")
    private List<String> checkUrlList;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @SneakyThrows
    @Override
    public Object run() throws ZuulException {
        log.info("{]", checkUrlList);
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        HttpServletResponse response = currentContext.getResponse();
        StringBuffer requestURL = request.getRequestURL();
        boolean flag = false;
        for (String s : checkUrlList) {
            if (requestURL.indexOf(s) > 0) {
                // 当前是需要拦截的
                flag = true;
                break;
            }
        }

        // 处理跨域
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        // 处理自定义的请求头
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "x-auth,content-type,x-token");
        // 处理特殊的请求方式 delete,put,get,post
        response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "DELETE,POST,PUT,GET");

        if (!flag) {
            return null;
        }


        // 验证[x-auth:eyJpZCI6MSwibWVtYmVyTmFtZSI6InpoYW5nc2FuIiwibmlja05hbWUiOiLlvKDkuIkifQ==.ZGQyYmJjYTZiNWUwNmU0NGU5NjE4MDQ0NTQ4ZGY1ZmI=]
        // 判断是否有头信息
        String header = request.getHeader("x-auth");
        if (StringUtils.isEmpty(header)) {
            // 不仅拦截了[不往后面走了]，而且还能给前台提示
//            throw new ShopException(ResponseEnum.TOKEN_IS_MISS);
            return buildResponse(currentContext, ResponseEnum.TOKEN_IS_MISS);
        }
        // 判断头信息是否完整
        String[] headerArr = header.split("\\.");
        if (headerArr.length != 2) {
//            throw new ShopException(ResponseEnum.TOKEN_IS_NOT_FULL);
            return buildResponse(currentContext, ResponseEnum.TOKEN_IS_NOT_FULL);
        }
        // 验签 [核心]
        String memberVoJsonBase64 = headerArr[0];
        String signBase64 = headerArr[1];
        // 进行base64解码,怎么把字节数组变为字符串
        String memberVoJson = new String(Base64.getDecoder().decode(memberVoJsonBase64), "utf-8");
        String sign = new String(Base64.getDecoder().decode(signBase64), "utf-8");
        String newSign = Md5Util.sign(memberVoJson, Constants.SECRET);
        if (!newSign.equals(sign)) {
            return buildResponse(currentContext, ResponseEnum.TOKEN_IS_FAIL);
        }
        // 将json转为java对象
        MemberVo memberVo = JSON.parseObject(memberVoJson, MemberVo.class);
        Long id = memberVo.getId();
        // 判断是否过期
        if (!RedisUtil.exist(KeyUtil.buildMemberKey(id))) {
//            throw new ShopException(ResponseEnum.TOKEN_IS_TIME_OUT);
            return buildResponse(currentContext, ResponseEnum.TOKEN_IS_TIME_OUT);
        }
        // 续命
        RedisUtil.expire(KeyUtil.buildMemberKey(id), Constants.TOKEN_EXPIRE);
        // 将memberVO存入request中
//        request.setAttribute(Constants.CURR_MEMBER, memberVo);
        currentContext.addZuulRequestHeader(Constants.CURR_MEMBER, URLEncoder.encode(JSON.toJSONString(memberVo), "utf-8"));
        return null;
    }

    private Object buildResponse(RequestContext currentContext, ResponseEnum responseEnum) {
        Map<String, String> res = new HashMap<>();
        res.put("code", responseEnum.getCode() + "");
        res.put("msg", responseEnum.getMsg() + "");
        HttpServletResponse response = currentContext.getResponse();
        response.setContentType("application/json;charset=utf-8");
        currentContext.setSendZuulResponse(false);
        currentContext.setResponseBody(JSON.toJSONString(res));
        return null;
    }


}
