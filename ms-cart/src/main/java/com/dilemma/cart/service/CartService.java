package com.dilemma.cart.service;

import com.dilemma.auth.entity.UserInfo;
import com.dilemma.cart.client.GoodsClient;
import com.dilemma.cart.interceptor.LoginInterceptor;
import com.dilemma.cart.pojo.Cart;
import com.dilemma.common.utils.JsonUtils;
import com.dilemma.item.pojo.Sku;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Slf4j
public class CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "mall:cart:uid:";

    public void addCart(Cart cart) {
        //获取登陆用户
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        //设置redis中用户的key，mall:cart:uid key(skuId) value(skuValue)
        String key = KEY_PREFIX + loginUser.getId();
        //获取hash操作对象
        BoundHashOperations<String,Object,Object> hashOps = this.redisTemplate.boundHashOps(key);
        System.out.println(key);
        //查询是否存在
        Long skuId = cart.getSkuId();
        Integer num = cart.getNum();
        //查询商品id是否存在
        Boolean boo = hashOps.hasKey(skuId.toString());
        if (boo){
            //存在获取购物车数据
            String json = Objects.requireNonNull(hashOps.get(skuId.toString())).toString();
            cart = JsonUtils.parse(json, Cart.class);
            //修改购物车中商品的数量
            assert cart != null;
            cart.setNum(cart.getNum()+ num);
        }else {
            cart.setUserId(loginUser.getId());
            //其他商品需要查询商品微服务
            Sku sku = this.goodsClient.querySkuById(skuId);
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" :
                    StringUtils.split(sku.getImages(),",")[0]);
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
        }
        //将购物车数据写入redis
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    //查询购物车
    public List<Cart> queryCartList() {
        //获取登陆用户，利用登陆用户查询购物车信息
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + loginUser.getId();
        if (!this.redisTemplate.hasKey(key)){
            //不存在直接返回null
            return null;
        }
        BoundHashOperations<String,Object,Object> hashOps = this.redisTemplate.boundHashOps(key);
        List<Object> carts = hashOps.values();
        //判断carts是否为空
        if (CollectionUtils.isEmpty(carts)){
            return null;
        }
        //查询购物车数据
        return carts.stream().map(o -> JsonUtils.parse(o.toString(),Cart.class)).collect(Collectors.toList());
    }

    /**
     * update cart
     * @param cart cart object
     */
    public void updateCarts(Cart cart) {
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + loginUser.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        String cartJson = Objects.requireNonNull(hashOps.get(cart.getSkuId().toString())).toString();
        //更新数量
        Cart newCart = JsonUtils.parse(cartJson, Cart.class);
        assert newCart != null;
        newCart.setNum(cart.getNum());
        //写入购物车
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(newCart));
    }

    /**
     * 删除购物车中的物品
     * @param skuId skuId
     */
    public void deleteCart(String skuId) {
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        String key = KEY_PREFIX + loginUser.getId();
        BoundHashOperations<String,Object,Object> hashOps = this.redisTemplate.boundHashOps(key);
        Long result = hashOps.delete(skuId);
        System.out.println(result);
    }
    /**
     * 购物车合并
     * 1、当跳转到购物车页面，查询购车前先查询用户登陆状态
     * 2、判断登陆状态
     * 2.1如果登陆
     *  check用户的localStorage中是否有购物车信息
     *  如果有，则提交到后台保存数据
     *  清空LocalStorage
     * 2.2如果未登录，直接查询即可
     */
    public void mergeCart(Cart cart){

    }


}
