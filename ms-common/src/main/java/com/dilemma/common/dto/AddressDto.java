package com.dilemma.common.dto;

import lombok.Data;

@Data
public class AddressDto {
    private Long id;
    private String name; //姓名
    private String phone; //电话
    private String state; //省
    private String city; //城市
    private String district; //地区
    private String address; //街道地址
    private String zipCode; //邮编
    private Boolean isDefault;
}
