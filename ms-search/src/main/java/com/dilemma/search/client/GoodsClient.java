package com.dilemma.search.client;

import com.dilemma.item.api.GoodsApi;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {
}
