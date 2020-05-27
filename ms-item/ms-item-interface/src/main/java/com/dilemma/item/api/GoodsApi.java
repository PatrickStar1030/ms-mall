package com.dilemma.item.api;

import com.dilemma.common.dto.CartDto;
import com.dilemma.common.vo.PageResult;
import com.dilemma.item.pojo.Sku;
import com.dilemma.item.pojo.Spu;
import com.dilemma.item.pojo.SpuBo;
import com.dilemma.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    /**
     * 分页查询
     * @param key 过滤条件 默认false
     * @param saleable 是否上架
     * @param page 当前页
     * @param row 每页显示多少
     * @return 分页结果
     */
    @GetMapping("/spu/page")
    PageResult<SpuBo> querySpuBoByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "row",defaultValue = "5")Integer row);

    @GetMapping("/spu/detail/{id}")
    SpuDetail querySpuDetailById(@PathVariable("id") Long id);

    @GetMapping("/sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id);

    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);

    @GetMapping("sku/{id}")
    Sku querySkuById(@PathVariable("id")Long id);

    @GetMapping("sku/list/{ids}")
    List<Sku> querySkuByIds(@PathVariable("ids")List<Long> ids);

    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDto> carts);
}
