package com.fh.shop.api.goods.controller;

import com.fh.shop.api.goods.biz.ISkuService;
import com.fh.shop.common.ServerResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/goods")
public class SkuController {

    @Resource(name = "skuService")
    private ISkuService skuService;

    @GetMapping("/recommend/newproduct")
    public ServerResponse list() {
        return skuService.findRecommendNewProductList();
    }

    @GetMapping("/findSkuById")
    public ServerResponse findSkuById(@RequestParam("id") Long id) {
        return skuService.findSkuById(id);
    }
}
