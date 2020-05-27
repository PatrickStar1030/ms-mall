package com.dilemma.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    @NotNull
    private Long addressId; //收货人地址id
    @NotNull
    private Integer paymentType; //支付类型
    @NotNull
    private List<CartDto> carts; //订单详情
}