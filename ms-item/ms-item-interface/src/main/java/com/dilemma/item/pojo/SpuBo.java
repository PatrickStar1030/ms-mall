package com.dilemma.item.pojo;

import lombok.Data;

import javax.persistence.Transient;
import java.util.List;

/**
 * 商品列表，包含了spu中的属性，
 * 额外需要展示商品的cname（category_name）,bname(brand_name)
 */
@Data
public class SpuBo extends Spu {
    @Transient
    private String cname; //商品分类
    @Transient
    private String bname; //品牌名称
    @Transient
    private SpuDetail spuDetail; //商品详情
    @Transient
    private List<Sku> skus; //sku列表
}
