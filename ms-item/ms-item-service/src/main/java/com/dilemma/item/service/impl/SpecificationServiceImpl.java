package com.dilemma.item.service.impl;

import com.dilemma.common.enums.ExceptionEnum;
import com.dilemma.common.exception.MsException;
import com.dilemma.item.mapper.SpecGroupMapper;
import com.dilemma.item.mapper.SpecParamMapper;
import com.dilemma.item.pojo.SpecGroup;
import com.dilemma.item.pojo.SpecParam;
import com.dilemma.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
@Service
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;
    /**
     * 根据分类id查询分组
     * @param cid
     * @return
     */
    @Override
    public List<SpecGroup> queryGroupsByCid(Long cid) {
        //查询条件
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        //判断
        List<SpecGroup> groups = this.specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(groups)){
            throw new MsException(ExceptionEnum.SPEC_GROUP_NOT_FIND);
        }
        return groups;
    }

    @Override
    public List<SpecParam> queryParams(Long cid,Long groupId, Boolean generic, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        specParam.setGroupId(groupId);
        specParam.setGeneric(generic);
        specParam.setSearching(searching);
        List<SpecParam> specParams = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(specParams)){
            throw new MsException(ExceptionEnum.SPEC_PARAM_NOT_FIND);
        }
        return specParams;
    }

    @Override
    public void updateGroup(SpecGroup group) {
        int i = specGroupMapper.updateByPrimaryKey(group);
        if (i!=1){
            throw new MsException(ExceptionEnum.SPEC_GROUP_UPDATE_ERROR);
        }
    }

    @Override
    @Transactional
    public void deleteGroupById(Long gid) {
        //删除tb_spec_group中对应记录
        int i = specGroupMapper.deleteByPrimaryKey(gid);
        if (i!=1){
            throw new MsException(ExceptionEnum.SPEC_GROUP_DELETE_ERROR);
        }
        //删除tb_spec_params中对应记录
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParamMapper.delete(specParam);
    }

    @Override
    public void createGroup(SpecGroup group) {
        group.setId(null);
        int i = specGroupMapper.insertSelective(group);
        if (i!=1){
            throw new MsException(ExceptionEnum.SPEC_GROUP_CREATE_ERROR);
        }

    }

    @Override
    public void createParam(SpecParam specParam) {
        specParam.setId(null);
        int i = specParamMapper.insertSelective(specParam);
        if (i!=1){
            throw new MsException(ExceptionEnum.SPEC_GROUP_CREATE_ERROR);
        }
    }

    @Override
    public void updateParam(SpecParam specParam) {
        int i = specParamMapper.updateByPrimaryKey(specParam);
        if (i!=1){
            throw new MsException(ExceptionEnum.SPEC_PARAM_UPDATE_ERROR);
        }
    }

    @Override
    public void DeleteParamById(Long id) {
        int i = specParamMapper.deleteByPrimaryKey(id);
        if (i!=1){
            throw new MsException(ExceptionEnum.SPEC_PARAM_DELETE_ERROR);
        }
    }

    @Override
    public List<SpecGroup> querySpecByCid(Long cid) {
        List<SpecGroup> groups = this.queryGroupsByCid(cid);
        groups.forEach(group -> group.setParams(this.queryParams(null,group.getId(),null,null)));
        return groups;
    }


}
