package com.dilemma.search.controller;

import com.dilemma.common.vo.PageResult;
import com.dilemma.item.pojo.Category;
import com.dilemma.search.pojo.Goods;
import com.dilemma.search.pojo.SearchRequestEntity;
import com.dilemma.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequestEntity searchRequestEntity){
        return ResponseEntity.ok(this.searchService.search(searchRequestEntity));
    }

}
