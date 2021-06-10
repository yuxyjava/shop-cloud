package com.fh.shop.api.goods.po;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class Sku implements Serializable {

    private Long id;

    private String skuName;

    private Long spuId;

    private BigDecimal price;

    private Integer stock;

    private String specInfo;

    private Long colorId;

    private String image;

    private String status;

    private String recommend;

    private String newProduct;

    private Long sale;
}
