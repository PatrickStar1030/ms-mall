package com.dilemma.item.controller;

import com.dilemma.item.pojo.Category;
import com.dilemma.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam("pid") Long pid){
        return ResponseEntity.ok(categoryService.queryCategoryListByPid(pid));
    }

    /**
     * 根据brand id查询出Category
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> updateCategoryByPid(@PathVariable("bid") Long bid){
        if (bid == null){
            System.out.println("参数为空");
        }
        return ResponseEntity.ok(categoryService.updateCategoryByPid(bid));
    }


    @GetMapping("names")
    public ResponseEntity<List<String>> queryNamesById(@RequestParam("ids")List<Long> ids){
        List<String> names = this.categoryService.queryNamesByIds(ids);
        return ResponseEntity.ok(names);
    }

    @GetMapping("level")
    public ResponseEntity<List<Category>> queryAllByCid3(@RequestParam("id")Long id){
        List<Category> categories = this.categoryService.queryAllByCid3(id);
        if (categories.size() < 1){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(categories);
    }


}
