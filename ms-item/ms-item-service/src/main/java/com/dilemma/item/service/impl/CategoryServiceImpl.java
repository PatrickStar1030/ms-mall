package com.dilemma.item.service.impl;

import com.dilemma.common.enums.ExceptionEnum;
import com.dilemma.common.exception.MsException;
import com.dilemma.item.mapper.BrandMapper;
import com.dilemma.item.mapper.CategoryMapper;
import com.dilemma.item.pojo.Category;
import com.dilemma.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BrandMapper brandMapper;
    /**
     * 根据pid查询商品分类
     * @param pid 商品父节点id，0为顶级父节点
     * @return
     */
    @Override
    public List<Category> queryCategoryListByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        List<Category> categories = categoryMapper.select(category);
        //判断结果
        if (CollectionUtils.isEmpty(categories)){
            throw new MsException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categories;
    }

    @Override
    @Transactional
    public List<Category> updateCategoryByPid(Long bid) {
        List<Long> categoriesId = brandMapper.selectCategoryBrand(bid);
        if (CollectionUtils.isEmpty(categoriesId)){
            throw new MsException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        Example example = new Example(Category.class);
        example.createCriteria().andIn("id",categoriesId);
        List<Category> categories = categoryMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(categories)){
            throw new MsException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categories;
    }

    /**
     * 根据ids查询所有产品分类
     * @param ids 分类id集合
     * @return 所有的分类名称
     */
    @Override
    public List<String> queryNamesByIds(List<Long> ids) {
        List<Category> categories = this.categoryMapper.selectByIdList(ids);
        List<String> names = new ArrayList<>();
        categories.forEach(category -> names.add(category.getName()));
        return names;
    }

    @Transactional
    @Override
    public List<Category> queryAllByCid3(Long id) {
        Category c3 = this.categoryMapper.selectByPrimaryKey(id);
        Category c2 = this.categoryMapper.selectByPrimaryKey(c3.getParentId());
        Category c1 = this.categoryMapper.selectByPrimaryKey(c2.getParentId());
        return Arrays.asList(c1,c2,c3);
    }
}
