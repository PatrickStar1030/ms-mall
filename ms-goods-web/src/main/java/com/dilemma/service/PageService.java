package com.dilemma.service;

import com.dilemma.client.BrandClient;
import com.dilemma.client.CategoryClient;
import com.dilemma.client.GoodsClient;
import com.dilemma.client.SpecificationClient;
import com.dilemma.item.pojo.*;
import com.dilemma.utils.ThreadUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;
@Slf4j
@Service
public class PageService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 根据页面分析，用spuId需要得到的参数
     * 1、spu信息
     * 2、spu详情
     * 3、sku具体商品信息
     * 4、三级分类信息
     * 5、品牌信息
     * 6、通用参数规格组
     * 7、特殊参数规格组
     * @param spuId
     * @return
     */
    public Map<String,Object> loadData(Long spuId){
        Map<String,Object> map = Maps.newHashMap();
        //标准参数
        Spu spu = this.goodsClient.querySpuById(spuId);
        //详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spuId);

        List<Sku> skus = this.goodsClient.querySkuBySpuId(spuId);

        List<Long> cids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        //分类信息
        List<String> names = this.categoryClient.queryNamesById(cids);
        //每一级分类的id和名称
        ArrayList<Map<String,Object>> categories = Lists.newArrayList();
        for (int i=0;i<cids.size();i++){
            HashMap<String,Object> categoryMap = Maps.newHashMap();
            categoryMap.put("id",cids.get(i));
            categoryMap.put("name",names.get(i));
            categories.add(categoryMap);
        }
        //品牌
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        //规格参数组
        List<SpecGroup> groups = this.specificationClient.querySpecGroupByCid(spu.getCid3());

        //查询特殊规格参数
        List<SpecParam> specParams = this.specificationClient.queryParams(spu.getCid3(), null, false, null);
        HashMap<Long, String> paramMap = Maps.newHashMap();
        specParams.forEach(specParam -> paramMap.put(specParam.getId(),specParam.getName()));
        //封装spu
        map.put("spu",spu);
        //详情
        map.put("spuDetail",spuDetail);
        //skus具体类别
        map.put("skus",skus);
        //分类信息
        map.put("categories",categories);
        //品牌
        map.put("brand",brand);
        //通用参数组
        map.put("groups",groups);
        //特殊规格参数
        map.put("paramMap",paramMap);
        return map;
    }

    public synchronized void createHtml(Long spuId){
        //上下文
        Context context = new Context();
        context.setVariables(loadData(spuId));
        //输出流
        URL resource = this.getClass().getClassLoader().getResource("/");
        System.out.println(resource);
        File file = new File("D:/02项目练习分类/ms-mall/ms-goods-web/src/main/resources/static",spuId+".html");
        if (file.exists()){
            file.delete();
        }
        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            //生成HTML
            this.templateEngine.process("item", context, writer);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            log.error("[静态页服务]生成静态页服务器异常！", e);
        }
    }

    public synchronized void deleteHtml(Long id) {
        File file = new File("D:/02项目练习分类/ms-mall/ms-goods-web/src/main/resources/static",id+".html");
        if (file.exists()){
            file.deleteOnExit();
        }
    }

    /**
     * 异步创建静态界面
     * @param spuId spuId
     */
    public void asyncExecute(Long spuId){
        ThreadUtils.execute(()->createHtml(spuId));
    }
}
