package com.dilemma.item.service.impl;

import com.dilemma.common.enums.ExceptionEnum;
import com.dilemma.common.exception.MsException;
import com.dilemma.common.vo.PageResult;
import com.dilemma.item.mapper.BrandMapper;
import com.dilemma.item.pojo.Brand;
import com.dilemma.item.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandMapper brandMapper;
    @Override
    public PageResult<Brand> queryBrandByPage(Integer page,
                                              Integer rows,
                                              String sortBy,
                                              Boolean desc,
                                              String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){ //非空
            example.createCriteria().orLike("name","%"+key+"%")
                    .orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy + (desc? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> brands = brandMapper.selectByExample(example);
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getPageSize(),brands);
    }

    @Override
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        //新增品牌
        brand.setId(null);
        int count = brandMapper.insert(brand);
        if (count != 1){
            throw new MsException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
        //新增中间表
        cids.forEach(cid->{
            int i = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (i != 1){
                throw new MsException(ExceptionEnum.CATEGORY_BRAND_SAVE_ERROR);
            }
        });
    }

    /**
     * 更新品牌数据
     * @param brand 品牌
     * @param cids 分类id
     */
    @Override
    @Transactional
    public void updateBrand(Brand brand, List<Long> cids) {
        //更新数据
        int count = brandMapper.updateByPrimaryKeySelective(brand);
        if (count != 1){
            throw new MsException(ExceptionEnum.BRAND_UPDATE_ERROR);
        }
        cids.forEach(cid->{
            int i = brandMapper.updateCategoryBrand(cid, brand.getId());
            if (i != 1){
                throw new MsException(ExceptionEnum.CATEGORY_BRAND_UPDATE_ERROR);
            }
        });
    }

    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = this.brandMapper.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(brands)){
            throw new MsException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    @Override
    @Transactional
    public void deleteBrandById(Long bid) {
        int i = this.brandMapper.deleteByPrimaryKey(bid);
        if (i!=1){
            throw new MsException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        this.brandMapper.deleteCategoryBrand(bid);
    }

    @Override
    public Brand queryBrandById(Long id) {
        return this.brandMapper.selectByPrimaryKey(id);
    }
}
