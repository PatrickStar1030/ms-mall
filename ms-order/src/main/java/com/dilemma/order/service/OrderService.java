package com.dilemma.order.service;

import com.dilemma.auth.entity.UserInfo;
import com.dilemma.common.dto.AddressDto;
import com.dilemma.common.dto.CartDto;
import com.dilemma.common.dto.OrderDto;
import com.dilemma.common.enums.ExceptionEnum;
import com.dilemma.common.exception.MsException;
import com.dilemma.common.utils.IdWorker;
import com.dilemma.item.pojo.Sku;
import com.dilemma.order.client.AddressClient;
import com.dilemma.order.client.GoodsClient;
import com.dilemma.order.enums.OrderStatusEnum;
import com.dilemma.order.interceptor.LoginInterceptor;
import com.dilemma.order.lock.RedisLock;
import com.dilemma.order.mapper.OrderDetailMapper;
import com.dilemma.order.mapper.OrderMapper;
import com.dilemma.order.mapper.OrderStatusMapper;
import com.dilemma.order.pojo.Order;
import com.dilemma.order.pojo.OrderDetail;
import com.dilemma.order.pojo.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private RedisLock redisLock;



    @Transactional
    public Long createOrder(OrderDto orderDto) {
        // TODO 1、新增订单
        Order order = new Order();
        // 1.1 订单编号，基本信息
        long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDto.getPaymentType());
        // 1.2 用户信息
        UserInfo loginUser = LoginInterceptor.getLoginUser();
        order.setUserId(loginUser.getId());
        order.setBuyerNick(loginUser.getUsername());
        order.setBuyerRate(false);
        // 1.3 收货人地址
        AddressDto addr = AddressClient.findById(orderDto.getAddressId());
        assert addr != null;
        order.setReceiver(addr.getName());
        order.setReceiverAddress(addr.getAddress());
        order.setReceiverCity(addr.getCity());
        order.setReceiverDistrict(addr.getAddress());
        order.setReceiverState(addr.getState());
        order.setReceiverMobile(addr.getPhone());
        order.setReceiverZip(addr.getZipCode());
        // 1.4 金额
        Map<Long, Integer> numMap = orderDto.getCarts().stream()
                .collect(Collectors.toMap(CartDto::getSkuId, CartDto::getNum));
        Set<Long> ids = numMap.keySet();
        List<Sku> skus = this.goodsClient.querySkuByIds(new ArrayList<>(ids));
        //准备一个orderDetail的集合
        List<OrderDetail> orderDetails = new ArrayList<>();
        BigDecimal totalPay = new BigDecimal("0.00");
        for (Sku sku:skus){
            //bigDecimal的累加
            BigDecimal number = new BigDecimal(String.valueOf(numMap.get(sku.getId())));
            totalPay = totalPay.add(sku.getPrice().multiply(number));
            //封装一下orderDetail
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetail.setNum(numMap.get(sku.getId()));
            orderDetail.setOrderId(order.getOrderId());
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setTitle(sku.getTitle());
            orderDetails.add(orderDetail);
        }
        order.setTotalPay(totalPay);
        //实付金额：总金额+邮费 - 优惠金额
        BigDecimal postFree = new BigDecimal(String.valueOf(order.getPostFee()));
        //目前优惠设置为0
        order.setActualPay(totalPay.add(postFree).subtract(new BigDecimal("0")));
        // 1.5 写入mysql
        this.orderMapper.insertSelective(order);
        // 2、新增订单详情
        int i = this.orderDetailMapper.insertList(orderDetails);
        if (i != orderDetails.size()){
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new MsException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        // TODO 3、新增订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(order.getOrderId());
        orderStatus.setStatus(OrderStatusEnum.UNPAY.value());
        int statusCount = orderStatusMapper.insertSelective(orderStatus);
        if (statusCount !=1){
            log.error("[创建订单] 创建订单失败，orderId:{}",orderId);
            throw new MsException(ExceptionEnum.CREATE_ORDER_ERROR);
        }
        // TODO 4、减库存
        List<CartDto> carts = orderDto.getCarts();
        //分布式锁，阻塞式加锁
        //redisLock.lock();
        this.goodsClient.decreaseStock(carts);
        //解锁
        //redisLock.unlock();
        return orderId;
    }


    public Order queryOrderById(Long id) {
        Order order = this.orderMapper.selectByPrimaryKey(id);
        if (order == null){
            throw new MsException(ExceptionEnum.ORDER_NOT_FOUND);
        }

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> orderDetails = this.orderDetailMapper.select(orderDetail);
        if (orderDetails == null){
            throw new MsException(ExceptionEnum.ORDER_DETAIL_NOT_FOUND);
        }
        order.setOrderDetails(orderDetails);
        //查询订单状态
        OrderStatus orderStatus = this.orderStatusMapper.selectByPrimaryKey(id);
        if (orderStatus == null){
            throw new MsException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        order.setStatus(orderStatus.getStatus());
        return order;
    }
}
