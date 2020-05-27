package com.dilemma.item.controller;

import com.dilemma.item.pojo.SpecGroup;
import com.dilemma.item.pojo.SpecParam;
import com.dilemma.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecificationService specificationService;


    /**
     * 根据分类id查询规格分类组
     * @param cid category_id
     * @return 分类组
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupsByCid(@PathVariable("cid")Long cid){
        return ResponseEntity.ok(this.specificationService.queryGroupsByCid(cid));
    }
    /**
     * 新增分类组
     * @param group 分类组信息
     * @return 状态码
     */
    @PostMapping("group/create")
    public ResponseEntity<Void> createGroup(@RequestBody SpecGroup group){
        if (group!=null){
            System.out.println("======================");
            System.out.println(group.getCid());
            System.out.println(group.getName());
            System.out.println(group.toString());
            specificationService.createGroup(group);
        }else {
            System.out.println("未接受到传入参数");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    /**
     * 更新分类组
     * @param group 更新信息
     * @return 状态码
     */
    @PutMapping("group/update")
    public ResponseEntity<Void> updateGroup(@RequestBody SpecGroup group){
        specificationService.updateGroup(group);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除分类组
     * @param gid group_id
     * @return 状态码
     */
    @DeleteMapping("group/{gid}")
    public ResponseEntity<Void> deleteGroupById(@PathVariable("gid")Long gid){
        specificationService.deleteGroupById(gid);
        return ResponseEntity.ok().build();
    }

    /**
     * 根据group_id 查询参数信息
     * @param gid 根据group_id
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamsByGid(
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "generic",required = false)Boolean generic,
            @RequestParam(value = "searching",required = false)Boolean searching
    ){

        return ResponseEntity.ok(this.specificationService.queryParams(cid,gid,generic,searching));
    }
    //新增
    @PostMapping("param/create")
    public ResponseEntity<Void> createParam(@RequestBody SpecParam specParam){
        specificationService.createParam(specParam);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    //更新
    @PutMapping("param/update")
    public ResponseEntity<Void> updateParam(@RequestBody SpecParam specParam){
        specificationService.updateParam(specParam);
        return ResponseEntity.ok().build();
    }
    //删除参数
    @DeleteMapping("param/{id}")
    public ResponseEntity<Void> deleteParamById(@PathVariable("id")Long id){
        specificationService.DeleteParamById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroupByCid(@PathVariable("cid")Long cid){
        List<SpecGroup> groups = this.specificationService.querySpecByCid(cid);
        if (groups == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(groups);
    }
}
