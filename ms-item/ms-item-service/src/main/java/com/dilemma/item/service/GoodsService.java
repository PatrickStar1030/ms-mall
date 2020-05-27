package com.dilemma.item.service;

import com.dilemma.common.dto.CartDto;
import com.dilemma.common.vo.PageResult;
import com.dilemma.item.pojo.Sku;
import com.dilemma.item.pojo.Spu;
import com.dilemma.item.pojo.SpuBo;
import com.dilemma.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GoodsService {
    /**
     * 商品列表分页查询
     * @param key 过滤（搜索）条件
     * @param saleable 是否上架
     * @param page 页数
     * @param row 每页展示行数
     * @return 每页的展示结果集
     */
    PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer row);

    void saveGoods(SpuBo spuBo);

    SpuDetail querySpuDetailBySpuId(Long spuId);

    List<Sku> querySkuBySpuId(Long id);

    void updateGoods(SpuBo spuBo);

    void deleteGoodsById(Long id);

    void updateGoodsSaleableStateById(Long spuId);

    Spu querySpuById(Long id);

    Sku querySkuById(Long id);

    List<Sku> querySkuByIds(List<Long> ids);

    void decreaseStock(List<CartDto> carts);
}
