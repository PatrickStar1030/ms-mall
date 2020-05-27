package com.dilemma.search.pojo;

import com.dilemma.common.vo.PageResult;
import com.dilemma.item.pojo.Brand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult extends PageResult<Goods> {
    private List<Map<String,Object>> categories;
    private List<Brand> brands;
    private List<Map<String,Object>> specs;

    public SearchResult(List<Goods> items,
                        Long total,
                        Integer totalPage,
                        List<Map<String,Object>> categories,
                        List<Brand> brands,
                        List<Map<String,Object>> specs){
        super(total,totalPage,items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
