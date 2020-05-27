package com.dilemma.search.client;

import com.dilemma.SearchServiceApp;
import com.dilemma.common.vo.PageResult;
import com.dilemma.item.pojo.SpuBo;
import com.dilemma.item.pojo.SpuDetail;
import com.dilemma.search.pojo.Goods;
import com.dilemma.search.service.SearchService;
import com.dilemma.search.utils.EsUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchServiceApp.class)
@Slf4j
public class CategoryClientTest {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private EsUtils esUtils;

    @Autowired
    private SearchService searchService;



    @Test
    public void testQueryCategories(){
        List<String> names = this.categoryClient.queryNamesById(Arrays.asList(1L,2L,3L));
        names.forEach(System.out::println);
    }
    @Test
    public void createElasticsearchIndex(){
        esUtils.createIndex("goods");
    }

    @Test
    public void loadData(){
        Integer page = 1;
        Integer rows = 100;
        int size;
        Long total;
        do {
            //分批查询spuBo
            PageResult<SpuBo> spuBoPageResult = this.goodsClient.querySpuBoByPage(null,
                    true, page, rows);
            //查询结果经过分页后的总页数
            size = spuBoPageResult.getTotalPage();
            total = spuBoPageResult.getTotal();
            List<Goods> goodsList = spuBoPageResult.getItems().stream().map(spuBo -> {
                try {
                    if (spuBo != null){
                        return this.searchService.buildGoods(spuBo);
                    }else {
                        throw new IllegalArgumentException("信息不存在！");
                    }
                } catch (JsonProcessingException e) {
                    log.error("json转换失败", e);
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
            boolean isSuccess = this.esUtils.saveAll("goods", goodsList);
            log.info("当前第：{}页，总页数：{},执行结果:{}",page,size,isSuccess);
            if (isSuccess){
                //每次循环页码加1
                page++;
            }
            log.info("当前页数加1：{}",page);
        } while (page == size);
        log.info("总页数：{}，总记录数：{}",size,total);
    }

    @Test
    public void testGuava(){
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(2L);
        String params = spuDetail.getGenericSpec();
        Map<String, String> split = Splitter.on(",").withKeyValueSeparator(":").split(params.substring(1,params.length()-1));
        for (Map.Entry<String,String> entry : split.entrySet()) {
            System.out.println("key = "+entry.getKey() +",value="+entry.getValue());
        }
    }
}