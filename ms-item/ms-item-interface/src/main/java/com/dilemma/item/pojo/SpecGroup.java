package com.dilemma.item.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

/**
 * 规格参数组
 */
@Table(name = "tb_spec_group")
@Data
public class SpecGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long cid; //对应分类id
    private String name;

    @Transient
    private List<SpecParam> params; //参数详细信息
}
