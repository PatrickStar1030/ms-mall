package com.dilemma.common.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageResult<T> {
    private Long total; //总条数
    private Integer totalPage; //总页数
    private List<T> items; //数据
}
