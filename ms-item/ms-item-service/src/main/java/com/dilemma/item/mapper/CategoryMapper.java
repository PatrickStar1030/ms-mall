package com.dilemma.item.mapper;

import com.dilemma.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface CategoryMapper extends Mapper<Category>,SelectByIdListMapper<Category, Long> {
}
