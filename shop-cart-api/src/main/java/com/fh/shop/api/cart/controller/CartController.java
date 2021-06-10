package com.fh.shop.api.cart.controller;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.cart.biz.ICartService;
import com.fh.shop.common.Constants;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.vo.MemberVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@RestController
@RequestMapping("/api/carts")
@Api(tags = "购物车接口")
public class CartController {

    @Autowired
    private HttpServletRequest request;
    @Resource(name = "cartService")
    private ICartService cartService;

    @PostMapping("/addCartItem")
    @ApiOperation("添加商品到购物车")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "skuId", value = "商品id", dataType = "java.lang.Long", required = true),
            @ApiImplicitParam(name = "count", value = "商品数量", dataType = "java.lang.Long", required = true),
            @ApiImplicitParam(name = "x-auth", value = "头信息", dataType = "java.lang.String", required = true, paramType = "header")
    })
    public ServerResponse addCartItem(Long skuId, Long count) throws UnsupportedEncodingException {
        MemberVo memberVo = JSON.parseObject(URLDecoder.decode(request.getHeader(Constants.CURR_MEMBER), "utf-8"), MemberVo.class);
        Long memberId = memberVo.getId();
        return cartService.addItem(memberId, skuId, count);
    }

    @GetMapping("/findCart")
    @ApiOperation("查找购物车商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth", value = "头信息", dataType = "java.lang.String", required = true, paramType = "header")
    })
    public ServerResponse findCart() throws UnsupportedEncodingException {
        MemberVo memberVo = JSON.parseObject(URLDecoder.decode(request.getHeader(Constants.CURR_MEMBER), "utf-8"), MemberVo.class);
        Long memberId = memberVo.getId();
        return cartService.findCart(memberId);
    }

    @GetMapping("/findCartCount")
    @ApiOperation("查找购物车商品数量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth", value = "头信息", dataType = "java.lang.String", required = true, paramType = "header")
    })
    public ServerResponse findCartCount() throws UnsupportedEncodingException {
        MemberVo memberVo = JSON.parseObject(URLDecoder.decode(request.getHeader(Constants.CURR_MEMBER), "utf-8"), MemberVo.class);
        Long memberId = memberVo.getId();
        return cartService.findCartCount(memberId);
    }

    @DeleteMapping("/deleteCartItem")
    @ApiOperation("删除购物车中得商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth", value = "头信息", dataType = "java.lang.String", required = true, paramType = "header")
    })
    public ServerResponse deleteCartItem(Long skuId) throws UnsupportedEncodingException {
        MemberVo memberVo = JSON.parseObject(URLDecoder.decode(request.getHeader(Constants.CURR_MEMBER), "utf-8"), MemberVo.class);
        Long memberId = memberVo.getId();
        return cartService.deleteCartItem(memberId, skuId);
    }

    @DeleteMapping("/deleteBatch")
    @ApiOperation("批量删除购物车中得商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "x-auth", value = "头信息", dataType = "java.lang.String", required = true, paramType = "header")
    })
    public ServerResponse deleteBatch(String ids) throws UnsupportedEncodingException {
        MemberVo memberVo = JSON.parseObject(URLDecoder.decode(request.getHeader(Constants.CURR_MEMBER), "utf-8"), MemberVo.class);
        Long memberId = memberVo.getId();
        return cartService.deleteBatch(memberId, ids);
    }
}
