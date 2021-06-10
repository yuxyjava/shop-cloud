package com.fh.shop.api.cart.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.cart.vo.CartItemVo;
import com.fh.shop.api.cart.vo.CartVo;
import com.fh.shop.api.goods.IGoodsFeignService;
import com.fh.shop.api.goods.po.Sku;
import com.fh.shop.common.Constants;
import com.fh.shop.common.KeyUtil;
import com.fh.shop.common.ResponseEnum;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.BigdecimalUtil;
import com.fh.shop.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service("cartService")
@Slf4j
public class ICartServiceImpl implements ICartService {

    @Autowired
    private IGoodsFeignService goodsFeignService;
    @Value("${sku.count.limit}")
    private int countLimit;

    @Override
    public ServerResponse addItem(Long memberId, Long skuId, Long count) {
        if (count > countLimit) {
            return ServerResponse.error(ResponseEnum.CART_SKU_COUNT_LIMIT);
        }
        // 商品是否存在
        ServerResponse<Sku> skuResponse = goodsFeignService.findSkuById(skuId);
        log.info("==========={}", skuResponse);
        Sku sku = skuResponse.getData();
        if (sku == null) {
            return ServerResponse.error(ResponseEnum.CART_SKU_IS_NULL);
        }
        // 商品是否上架
        if (sku.getStatus().equals(Constants.STATUS_DOWN)) {
            return ServerResponse.error(ResponseEnum.CART_SKU_IS_DOWN);
        }
        // 商品的库存量大于等于购买的数量
        Integer stock = sku.getStock();
        if (stock.intValue() < count) {
            return ServerResponse.error(ResponseEnum.CART_SKU_STOCK_IS_ERROR);
        }

        // 会员是否有购物车
        String key = KeyUtil.buildCartKey(memberId);
        boolean exist = RedisUtil.exist(key);
        // 如果没有购物车
        if (!exist) {
            if (count < 0) {
                return ServerResponse.error(ResponseEnum.CART_IS_ERROR);
            }
            // 创建一个购物车，直接把商品加入到购物车
            CartVo cartVo = new CartVo();
            CartItemVo cartItemVo = new CartItemVo();
            cartItemVo.setCount(count);
            String price = sku.getPrice().toString();
            cartItemVo.setPrice(price);
            cartItemVo.setSkuId(sku.getId());
            cartItemVo.setSkuImage(sku.getImage());
            cartItemVo.setSkuName(sku.getSkuName());
            BigDecimal subPrice = BigdecimalUtil.mul(price, count + "");
            cartItemVo.setSubPrice(subPrice.toString());
            cartVo.getCartItemVoList().add(cartItemVo);
            cartVo.setTotalCount(count);
            cartVo.setTotalPrice(cartItemVo.getSubPrice());
            // 更新购物车【redis中得购物车】
//            RedisUtil.set(key, JSON.toJSONString(cartVo));
            RedisUtil.hset(key, Constants.CART_JSON_FIELD, JSON.toJSONString(cartVo));
            RedisUtil.hset(key, Constants.CART_COUNT_FIELD, cartVo.getTotalCount()+"");
        } else {
            // 如果有购车
            String cartJson = RedisUtil.hget(key, Constants.CART_JSON_FIELD);
            CartVo cartVo = JSON.parseObject(cartJson, CartVo.class);
            List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
            Optional<CartItemVo> item = cartItemVoList.stream().filter(x -> x.getSkuId().longValue() == skuId.longValue()).findFirst();
            if (item.isPresent()) {
                // 购物车有这款商品，找到这款商品，更新商品的数量，小计
                CartItemVo cartItemVo = item.get();
                long itemCount = cartItemVo.getCount() + count;
                // 购买限制，最多是10个
                if (itemCount > countLimit) {
                    return ServerResponse.error(ResponseEnum.CART_SKU_COUNT_LIMIT);
                }
                if (itemCount <= 0) {
                    // 从购物车中删除当前商品
                    cartItemVoList.removeIf(x -> x.getSkuId().longValue() == cartItemVo.getSkuId().longValue());
                    if (cartItemVoList.size() == 0) {
                        // 把整个购物车干掉
                        RedisUtil.delete(key);
                        return ServerResponse.success();
                    }
                    // 更新
                    updateCart(key, cartVo);
                    return ServerResponse.success();
                }
                cartItemVo.setCount(itemCount);
                BigDecimal subPrice = new BigDecimal(cartItemVo.getSubPrice());
                String subPriceStr = subPrice.add(BigdecimalUtil.mul(cartItemVo.getPrice(), count + "")).toString();
                cartItemVo.setSubPrice(subPriceStr);
                // 更新购物车
                updateCart(key, cartVo);
            } else {
                if (count < 0) {
                    return ServerResponse.error(ResponseEnum.CART_IS_ERROR);
                }
                // 购物车中没有这款商品，直接将商品加入购物车
                CartItemVo cartItemVo = new CartItemVo();
                cartItemVo.setCount(count);
                String price = sku.getPrice().toString();
                cartItemVo.setPrice(price);
                cartItemVo.setSkuId(sku.getId());
                cartItemVo.setSkuImage(sku.getImage());
                cartItemVo.setSkuName(sku.getSkuName());
                BigDecimal subPrice = BigdecimalUtil.mul(price, count + "");
                cartItemVo.setSubPrice(subPrice.toString());
                cartVo.getCartItemVoList().add(cartItemVo);
                // 更新购物车
                updateCart(key, cartVo);
            }
        }
        return ServerResponse.success();
    }

    private void updateCart(String key, CartVo cartVo) {
        // 更新购物车
        List<CartItemVo> cartItemVos = cartVo.getCartItemVoList();
        long totalCount = 0;
        BigDecimal totalPrice = new BigDecimal(0);
        for (CartItemVo itemVo : cartItemVos) {
            totalCount += itemVo.getCount();
            totalPrice = totalPrice.add(new BigDecimal(itemVo.getSubPrice()));
        }
        cartVo.setTotalCount(totalCount);
        cartVo.setTotalPrice(totalPrice.toString());
        // 更新购物车【redis中得购物车】
        RedisUtil.hset(key, Constants.CART_JSON_FIELD, JSON.toJSONString(cartVo));
        RedisUtil.hset(key, Constants.CART_COUNT_FIELD, cartVo.getTotalCount()+"");
    }

    @Override
    public ServerResponse findCart(Long memberId) {
        String cartJson = RedisUtil.hget(KeyUtil.buildCartKey(memberId), Constants.CART_JSON_FIELD);
        CartVo cartVo = JSON.parseObject(cartJson, CartVo.class);
        return ServerResponse.success(cartVo);
    }

    @Override
    public ServerResponse findCartCount(Long memberId) {
        String count = RedisUtil.hget(KeyUtil.buildCartKey(memberId), Constants.CART_COUNT_FIELD);
        return ServerResponse.success(count);
    }

    @Override
    public ServerResponse deleteCartItem(Long memberId, Long skuId) {
        // 获取会员对应的购物车
        String key = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.hget(key, Constants.CART_JSON_FIELD);
        CartVo cartVo = JSON.parseObject(cartJson, CartVo.class);
        List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
        Optional<CartItemVo> itemVo = cartItemVoList.stream().filter(x -> x.getSkuId().longValue() == skuId.longValue()).findFirst();
        if (!itemVo.isPresent()) {
            return ServerResponse.error(ResponseEnum.CART_IS_ERROR);
        }
        cartItemVoList.removeIf(x -> x.getSkuId().longValue() == skuId.longValue());
        if (cartItemVoList.size() == 0) {
            RedisUtil.delete(key);
            return ServerResponse.success();
        }
        // 更新购物车
        updateCart(key, cartVo);
        return ServerResponse.success();
    }

    @Override
    public ServerResponse deleteBatch(Long memberId, String ids) {
        if (StringUtils.isEmpty(ids)) {
            return ServerResponse.error(ResponseEnum.CART_BATCH_DELETE_NO_SELECT);
        }
        String key = KeyUtil.buildCartKey(memberId);
        String cartJson = RedisUtil.hget(key, Constants.CART_JSON_FIELD);
        CartVo cartVo = JSON.parseObject(cartJson, CartVo.class);
        List<CartItemVo> cartItemVoList = cartVo.getCartItemVoList();
        Arrays.stream(ids.split(",")).forEach(x -> cartItemVoList.removeIf(y -> y.getSkuId().longValue() == Long.parseLong(x)));
        if (cartItemVoList.size() == 0) {
            RedisUtil.delete(key);
            return ServerResponse.success();
        }
        // 更新购物车
        updateCart(key, cartVo);
        return ServerResponse.success();
    }
}
