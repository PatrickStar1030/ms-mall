package com.dilemma.item.mapper;

import com.dilemma.common.mapper.BaseMapper;
import com.dilemma.item.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StockMapper extends BaseMapper<Stock,Long> {

    @Select("UPDATE tb_stock SET stock = stock - #{num} WHERE id = #{id} AND stock >= #{num}")
    int decreaseStock(@Param("id")Long id,@Param("num") Integer num);
}
