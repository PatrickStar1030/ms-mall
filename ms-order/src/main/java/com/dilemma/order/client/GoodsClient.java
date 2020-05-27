package com.dilemma.order.client;

import com.dilemma.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "goods-service")
public interface GoodsClient extends GoodsApi {
}
