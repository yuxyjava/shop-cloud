package com.fh.shop.api.cate.biz;

import com.alibaba.fastjson.JSON;
import com.fh.shop.api.cate.mapper.ICateMapper;
import com.fh.shop.api.cate.po.Cate;
import com.fh.shop.common.ServerResponse;
import com.fh.shop.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("cateService")
@Transactional(rollbackFor = Exception.class)
public class ICateServiceImpl implements ICateService {

    @Autowired
    private ICateMapper cateMapper;

    @Override
    @Transactional(readOnly = true)
    public ServerResponse findList() {
        // 先从缓存中找
        String cateListInfo = RedisUtil.get("cateList");
        if (StringUtils.isNotEmpty(cateListInfo)) {
            // 将json字符串转为java对象
            List<Cate> cates = JSON.parseArray(cateListInfo, Cate.class);
            // 则直接返回
            return ServerResponse.success(cates);
        }
        // 从数据库中找
        List<Cate> cateList = cateMapper.selectList(null);
        // 要把java对象转换为string[json格式的字符串]
        String cateListJson = JSON.toJSONString(cateList);
        // 放到缓存中
        RedisUtil.set("cateList", cateListJson);
        // 返回
        return ServerResponse.success(cateList);
    }
}
