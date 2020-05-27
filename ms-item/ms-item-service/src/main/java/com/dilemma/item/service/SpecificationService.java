package com.dilemma.item.service;

import com.dilemma.item.pojo.SpecGroup;
import com.dilemma.item.pojo.SpecParam;

import java.util.List;

public interface SpecificationService {
    /**
     * 通过商品分类查询规格分类组信息
     * @param cid category_id
     * @return
     */
    List<SpecGroup> queryGroupsByCid(Long cid);
    /**
     * 新增
     * @param group
     */
    void createGroup(SpecGroup group);

    /**
     * 更新规格参数组
     * @param group
     */
    void updateGroup(SpecGroup group);

    /**
     *  删除规格组
     * @param gid 规格组id
     */
    void deleteGroupById(Long gid);
//=================================================
    /**
     * 通过规格分类组查询当前组参数
     *
     * @param cid
     * @param groupId SpecGroupId
     * @param generic
     * @param searching
     * @return 具体规格参数
     */
    List<SpecParam> queryParams(Long cid, Long groupId, Boolean generic, Boolean searching);

    void createParam(SpecParam specParam);

    void updateParam(SpecParam specParam);

    void DeleteParamById(Long id);

    List<SpecGroup> querySpecByCid(Long id);
}
