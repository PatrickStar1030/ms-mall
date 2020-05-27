package com.dilemma.item.service;

import com.dilemma.item.pojo.Category;

import java.util.List;

public interface CategoryService {
    //根据父id查询商品分类
    List<Category> queryCategoryListByPid(Long pid);

    List<Category> updateCategoryByPid(Long bid);

    /**
     * 根据ids查询所有产品分类
     * @param ids 分类id集合
     * @return 所有的分类名称
     */
    List<String> queryNamesByIds(List<Long> ids);

    List<Category> queryAllByCid3(Long id);
}
