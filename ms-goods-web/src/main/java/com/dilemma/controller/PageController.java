package com.dilemma.controller;

import com.dilemma.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("item")
public class PageController {
    @Autowired
    private PageService pageService;

    @GetMapping("{id}.html")
    public String toItemPage(Model model, @PathVariable("id") Long spuId) {
        Map<String, Object> map = this.pageService.loadData(spuId);
        model.addAllAttributes(map);
        this.pageService.asyncExecute(spuId);
        return "item";
    }
}
