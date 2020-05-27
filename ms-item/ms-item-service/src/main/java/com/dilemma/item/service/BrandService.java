package com.dilemma.item.service;

import com.dilemma.common.vo.PageResult;
import com.dilemma.item.pojo.Brand;

import java.util.List;

public interface BrandService {

    PageResult<Brand> queryBrandByPage(Integer page,
                                       Integer rows,
                                       String sortBy,
                                       Boolean desc,
                                       String key);

    void saveBrand(Brand brand, List<Long> cids);

    void updateBrand(Brand brand, List<Long> cids);

    List<Brand> queryBrandByCid(Long cid);

    void deleteBrandById(Long bid);

    Brand queryBrandById(Long id);
}
