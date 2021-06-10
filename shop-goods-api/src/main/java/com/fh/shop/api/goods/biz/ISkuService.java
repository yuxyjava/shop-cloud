package com.fh.shop.api.goods.biz;

import com.fh.shop.common.ServerResponse;

public interface ISkuService {

    public ServerResponse findRecommendNewProductList();

    ServerResponse findSkuById(Long id);
}
