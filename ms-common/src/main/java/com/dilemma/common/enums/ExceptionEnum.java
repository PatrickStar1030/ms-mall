package com.dilemma.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400,"价格不能为空"),

    CATEGORY_NOT_FOUND(404,"商品分类信息没有查到"),
    CATEGORY_BRAND_SAVE_ERROR(500,"新增品牌分类中间表失败"),
    CATEGORY_BRAND_UPDATE_ERROR(500,"更新品牌分类中间表失败"),

    BRAND_NOT_FOUND(404,"没有查询到品牌"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    BRAND_UPDATE_ERROR(500,"更新品牌失败"),

    UPLOAD_FILE_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(400,"非法文件"),

    SPEC_GROUP_NOT_FIND(404,"商品规格组不存在"),
    SPEC_GROUP_CREATE_ERROR(500,"规格组增加失败"),
    SPEC_GROUP_UPDATE_ERROR(500,"规格组更新失败"),
    SPEC_GROUP_DELETE_ERROR(500,"规格组删除失败"),


    SPEC_PARAM_NOT_FIND(404,"规格组参数不存在"),
    SPEC_PARAM_CREATE_ERROR(500,"规格组参数新增失败"),
    SPEC_PARAM_UPDATE_ERROR(500,"规格组参数更新失败"),
    SPEC_PARAM_DELETE_ERROR(500,"规格组参数删除失败"),

    SPUDETAIL_NOT_FOUND(1001,"spu_detail不存在"),

    SKU_NOT_FOUND(2001,"没有从当前spu模型中找到sku"),

    GOODS_NOT_FOUND(3001,"没有找到商品"),

    INVALID_USER_DATA_TYPE(4001,"错误的用户数据类型"),

    AUTHORIZATION_FAIL(401,"授权校验失败"),

    CREATE_ORDER_ERROR(500,"创建订单失败"),
    ORDER_NOT_FOUND(501,"未找到订单"),
    ORDER_DETAIL_NOT_FOUND(502,"未找到订单详情"),
    ORDER_STATUS_ERROR(503,"订单状态异常"),

    CART_NOT_FOUND(5001,"购物车没找到"),

    STOCK_NOT_ENOUGH(5002,"库存不足"),
    ;
    private int code;
    private String msg;
}
