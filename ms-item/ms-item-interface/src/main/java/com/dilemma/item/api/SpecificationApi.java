package com.dilemma.item.api;

import com.dilemma.item.pojo.SpecGroup;
import com.dilemma.item.pojo.SpecParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("spec")
public interface SpecificationApi {

    @GetMapping("params")
    List<SpecParam> queryParams(
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "generic",required = false)Boolean generic,
            @RequestParam(value = "searching",required = false)Boolean searching
    );


    @GetMapping("{cid}")
    List<SpecGroup> querySpecGroupByCid(@PathVariable("cid")Long cid);

}
