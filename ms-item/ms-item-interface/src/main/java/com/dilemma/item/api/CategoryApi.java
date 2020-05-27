package com.dilemma.item.api;

import com.dilemma.item.pojo.Category;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("category")
public interface CategoryApi {
    @GetMapping("names")
    List<String> queryNamesById(@RequestParam("ids")List<Long> ids);
    @GetMapping("level")
    List<Category> queryAllByCid3(Long id);
}
