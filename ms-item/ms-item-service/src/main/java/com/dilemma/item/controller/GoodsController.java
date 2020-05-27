package com.dilemma.item.controller;

import com.dilemma.common.dto.CartDto;
import com.dilemma.common.vo.PageResult;
import com.dilemma.item.pojo.Sku;
import com.dilemma.item.pojo.Spu;
import com.dilemma.item.pojo.SpuBo;
import com.dilemma.item.pojo.SpuDetail;
import com.dilemma.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询
     * @param key 过滤条件 默认false
     * @param saleable 是否上架
     * @param page 当前页
     * @param row 每页显示多少
     * @return 分页结果
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
            @RequestParam(value = "key",required = false)String key,
            @RequestParam(value = "saleable",required = false)Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "row",defaultValue = "5")Integer row
    ){
        PageResult<SpuBo> spuBoPageResult = this.goodsService.querySpuBoByPage(key, saleable, page, row);
        if (CollectionUtils.isEmpty(spuBoPageResult.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuBoPageResult);
    }

    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        this.goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("goods/delete/{id}")
    public ResponseEntity<Void> deleteGoodsById(@PathVariable("id")Long id){
        this.goodsService.deleteGoodsById(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
        return ResponseEntity.ok(this.goodsService.querySpuDetailBySpuId(spuId));
    }
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id")Long id){
        return ResponseEntity.ok(this.goodsService.querySkuBySpuId(id));
    }

    //商品上架和下架
    @PutMapping("goods/saleable/{spuId}")
    public ResponseEntity<Void> updateGoodsSaleableStateById(@PathVariable("spuId")Long spuId){
        this.goodsService.updateGoodsSaleableStateById(spuId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
        Spu spu = this.goodsService.querySpuById(id);
        if (spu == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spu);
    }


    @GetMapping("sku/{id}")
    public ResponseEntity<Sku> querySkuById(@PathVariable("id") Long id){
        Sku sku = this.goodsService.querySkuById(id);
        if (sku == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(sku);
    }

    @GetMapping("sku/list/{ids}")
    public ResponseEntity<List<Sku>> querySkuByIds(@PathVariable("ids")List<Long> ids){
        List<Sku> skus = this.goodsService.querySkuByIds(ids);
        if (skus == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(skus);
    }

    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDto> carts){
        this.goodsService.decreaseStock(carts);
        return ResponseEntity.ok().build();
    }

}
