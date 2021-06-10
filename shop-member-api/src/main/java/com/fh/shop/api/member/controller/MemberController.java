package com.fh.shop.api.member.controller;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.member.biz.IMemberService;
import com.fh.shop.common.Constants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import com.fh.shop.vo.MemberVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@RestController
@RequestMapping("/api")
@Api(tags = "会员接口")
public class MemberController {

    @Autowired
    private HttpServletRequest request;
    @Resource(name = "memberService")
    private IMemberService memberService;

    @GetMapping("/member/findMember")
    @ApiOperation("获取会员信息")
    @ApiImplicitParam(name = "x-auth", value = "头信息", dataType = "java.lang.String", required = true, paramType = "header")
    public ServerResponse findMember() throws UnsupportedEncodingException {
        MemberVo memberVo = JSON.parseObject(URLDecoder.decode(request.getHeader(Constants.CURR_MEMBER), "utf-8"), MemberVo.class);
        return ServerResponse.success(memberVo);
    }

    @GetMapping("/member/logout")
    public ServerResponse logout() {
        MemberVo memberVo = JSON.parseObject(request.getHeader(Constants.CURR_MEMBER), MemberVo.class);
        RedisUtil.delete(KeyUtil.buildMemberKey(memberVo.getId()));
        return ServerResponse.success();
    }

    @PostMapping("/member/login")
    @ApiOperation("登陆")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberName", value = "会员名", dataType = "java.lang.String", required = true),
            @ApiImplicitParam(name = "password", value = "密码", dataType = "java.lang.String", required = true)
    })
    public ServerResponse login(String memberName, String password) {
        return memberService.login(memberName, password);
    }
}
