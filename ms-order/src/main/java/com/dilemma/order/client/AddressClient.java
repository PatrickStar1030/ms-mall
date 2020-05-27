package com.dilemma.order.client;

import com.dilemma.common.dto.AddressDto;

import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {
    public static final List<AddressDto> addressList = new ArrayList<AddressDto>(){
        {
            AddressDto addressDto = new AddressDto();
            addressDto.setId(1L);
            addressDto.setAddress("蓝域拿铁公寓8栋501");
            addressDto.setDistrict("东湖高新区");
            addressDto.setCity("武汉");
            addressDto.setState("湖北省");
            addressDto.setName("派大星");
            addressDto.setPhone("18672580545");
            addressDto.setZipCode("430000");

            AddressDto addressDto2 = new AddressDto();
            addressDto2.setId(2L);
            addressDto2.setAddress("保健巷11号");
            addressDto2.setDistrict("城中街道");
            addressDto2.setCity("应城市");
            addressDto2.setState("湖北省");
            addressDto2.setName("派大星");
            addressDto2.setPhone("18672580545");
            addressDto2.setZipCode("432400");
        }
    };


    public static AddressDto findById(Long id){
        for (AddressDto addressDto : addressList){
            if (addressDto.getId() == id) return addressDto;
        }
        return null;
    }
}
