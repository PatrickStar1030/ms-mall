package com.dilemma.order.controller;

import com.dilemma.common.dto.OrderDto;
import com.dilemma.order.pojo.Order;
import com.dilemma.order.service.OrderService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("order")
@Api("订单服务接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("create")
    public ResponseEntity<Long> createOrder(@RequestBody @Valid OrderDto orderDto){
        Long id = this.orderService.createOrder(orderDto);
        return new ResponseEntity<>(id,HttpStatus.CREATED);
    }

    /**
     * 根据订单编号查询订单
     * @param id 订单编号
     * @return 订单实体
     */
    @GetMapping("query/{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){
        return ResponseEntity.ok(this.orderService.queryOrderById(id));
    }


}
