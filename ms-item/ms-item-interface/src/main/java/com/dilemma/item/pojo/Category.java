package com.dilemma.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_category")
@Data
public class Category {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id; //主键id
    private String name; //分类名称
    private Long parentId; //父节点id
    private Boolean isParent; //是否为父节点
    private Integer sort; //排序字段，越小越靠前
}
