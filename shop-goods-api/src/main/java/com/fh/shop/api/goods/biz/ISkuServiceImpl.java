package com.fh.shop.api.goods.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.goods.mapper.ISkuMapper;
import com.fh.shop.api.goods.po.Sku;
import com.fh.shop.api.goods.vo.SkuVo;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("skuService")
@Transactional(rollbackFor = Exception.class)
public class ISkuServiceImpl implements ISkuService {

    @Autowired
    private ISkuMapper skuMapper;

    @Override
    @Transactional(readOnly = true)
    public ServerResponse findRecommendNewProductList() {
        String skuListJson = RedisUtil.get("skuList");
        if (StringUtils.isNotEmpty(skuListJson)) {
            List<SkuVo> skuList = JSON.parseArray(skuListJson, SkuVo.class);
            // jackson工具包【没有对bigdecimal做特殊的处理】
            return ServerResponse.success(skuList);
        }
        List<Sku> skuList = skuMapper.findRecommendNewProductList();
        List<SkuVo> skuVoList = skuList.stream().map(x -> {
            SkuVo skuVo = new SkuVo();
            skuVo.setId(x.getId());
            skuVo.setImage(x.getImage());
            skuVo.setPrice(x.getPrice().toString());
            skuVo.setSkuName(x.getSkuName());
            return skuVo;
        }).collect(Collectors.toList());
        // fastjson工具包 【对bigdecimal做特殊的处理】
        String skuListVoJson = JSON.toJSONString(skuVoList);
        RedisUtil.setEx("skuList", skuListVoJson, 30);
        // jackson工具包
        return ServerResponse.success(skuVoList);
    }

    @Override
    public ServerResponse findSkuById(Long id) {
        Sku sku = skuMapper.selectById(id);
        return ServerResponse.success(sku);
    }


}
