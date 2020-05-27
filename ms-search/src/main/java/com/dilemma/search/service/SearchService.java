package com.dilemma.search.service;

import com.dilemma.common.utils.JsonUtils;
import com.dilemma.common.vo.PageResult;
import com.dilemma.item.pojo.*;
import com.dilemma.search.client.BrandClient;
import com.dilemma.search.client.CategoryClient;
import com.dilemma.search.client.GoodsClient;
import com.dilemma.search.client.SpecificationClient;
import com.dilemma.search.pojo.Goods;
import com.dilemma.search.pojo.SearchRequestEntity;
import com.dilemma.search.pojo.SearchResult;
import com.dilemma.search.utils.EsUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 把mysql数据导入到elasticsearch中进行存储。
 * 实际上就是把spu,sku的信息查询出来转换成为goods，进行保存
 */
@Service
@Slf4j
public class SearchService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private EsUtils esUtils;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * load data those goods is salable
     * @param spu
     * @return
     * @throws JsonProcessingException
     */
    public Goods buildGoods(Spu spu) throws JsonProcessingException {
        //创建goods对象
        Goods goods = new Goods();
        //查询出brand
        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());
        //查询分类名称
        List<String> categoryNames = this.categoryClient.queryNamesById(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        //查询spu下的所有sku
        List<Sku> skuList = this.goodsClient.querySkuBySpuId(spu.getId());
        //取价格
        Set<BigDecimal> prices = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());
        //每一个spu下的中商品的sku
        List<Map<String,Object>> skuMapList = new ArrayList<>();

        //遍历skus，获取价格集合
        skuList.forEach(sku -> {
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            //图片在搜索栏永远展示第一个
            skuMap.put("image",StringUtils.isNotBlank(
                    sku.getImages())?StringUtils.split(sku.getImages(),",")[0]:"");
            skuMapList.add(skuMap);
        });
        //查询出所有搜索规格参数,spec通过三级分类可以查出当前spu的所有参数
        List<SpecParam> specParams = this.specificationClient.queryParams(spu.getCid3(), null, null, true);
        //获取规格参数值
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spu.getId());
//        String genericSpec = spuDetail.getGenericSpec();
//        String specialSpec = spuDetail.getSpecialSpec();
        //获取通用的规格参数{规格参数id：规格参数值}，使用jackson进行转换，key为specParam的id，value为具体参数
        Map<Long,Object> genericSpecMap = JsonUtils.nativeRead(spuDetail.getGenericSpec(),
                new TypeReference<Map<Long,Object>>() {});
        //获取特殊的规格参数{规格参数id：规格参数值}，key为specParam的id，value为具体参数，这里有颜色，所以是list集合
        Map<Long,List<Object>> specialSpecMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(),
                new TypeReference<Map<Long, List<Object>>>() {});
        //定义map接收{规格参数名：规格参数值}，这个spu的具体规格参数和参数值
        Map<String,Object> paramMap = new HashMap<>();
        /*说明：这里specParam中的id是和spu_detail表中的generic_spec字段中json串中的key相对应*/
        //paramMap把规格参数id变成{规格参数名：规格参数值}
        specParams.forEach(specParam -> {
            //判断是否通用参数
            if (specParam.getGeneric()){
                //获取通用规格参数值，因为有对应关系，所以这里通过specParam表中的id，可以获取json串中的value
                String value = genericSpecMap.get(specParam.getId()).toString();
                //判断是否数值类型
                if (specParam.getNumeric()){
                    //如果为数值类型,判断值落在哪个区间
                    value = chooseSegment(value, specParam);
                }
                //把参数名和值放入结果中
                paramMap.put(specParam.getName(),value);
            }else {
                //非通用参数
                paramMap.put(specParam.getName(),specialSpecMap.get(specParam.getId()));
            }
        });
        //设置goods属性
        goods.setId(spu.getId());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        //搜索字段，含标题，品牌名称，分类名称
        goods.setAll(spu.getTitle()+brand.getName()+StringUtils.join(categoryNames,""));
        goods.setPrice(prices);
        goods.setSkus(MAPPER.writeValueAsString(skuMapList));
        //当前商品的规格参数{规格参数名：规格参数值}
        goods.setSpecs(paramMap);

        return goods;
    }

    private String chooseSegment(String value,SpecParam specParam){
        //value是查询条件，(具体值x)，specParam是判断分段条件如[x-xx,x-xx,x-xx]
        double v = NumberUtils.toDouble(value);
        String result = "其他";
        //遍历每个segment
        for (String segment : specParam.getSegments().split(",")){
            //取每个segment值的范围
            //分割字符串为数据，以"-"进行分割
            String[] segs = segment.split("-");
            //获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            //最大值在数据库中表示是x-
            double end = Double.MAX_VALUE;
            //如果超过最大范围（x-）,分割后数组的长度应该为1，如果落在中间范围，那么数组长度为2，那么这里的end值设置为seg[1]
            if (segs.length==2){
                end = NumberUtils.toDouble(segs[1]);

            }
            //判断是否在范围内
            if (v>=begin && v<end){
                if(segs.length == 1){
                    //字符串拼接
                    result = segs[0] + specParam.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + specParam.getUnit() + "以下";
                }else{
                    result = segment + specParam.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public PageResult<Goods> search(SearchRequestEntity searchRequestEntity) {
        String key = searchRequestEntity.getKey();
        //判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (StringUtils.isBlank(key)){
            return null;
        }
        //1、调用一个复杂查询获取bool查询构建器
        BoolQueryBuilder boolQueryBuilder = buildBoolQueryBuilder(searchRequestEntity);
        //聚合条件构建器
        List<TermsAggregationBuilder> aggregationBuilders;
        String categoryAggName = "categories";
        String brandAggName = "brands";
        TermsAggregationBuilder cid3 = AggregationBuilders.terms(categoryAggName).field("cid3");
        TermsAggregationBuilder brandId = AggregationBuilders.terms(brandAggName).field("brandId");
        try {
            aggregationBuilders = new ArrayList<>();
            aggregationBuilders.add(cid3);
            aggregationBuilders.add(brandId);
            int size = searchRequestEntity.getSize();
            SearchResponse searchResponse = this.esUtils.search(size,
                    searchRequestEntity.getPage(),
                    new String[]{"id", "skus", "subTitle"},
                    boolQueryBuilder,
                    searchRequestEntity.getSortBy(),
                    searchRequestEntity.getDescending(),
                    aggregationBuilders);
            //hit总数，用于封装结果集
            long total = searchResponse.getHits().getTotalHits().value;
            //计算出总页数
            Integer totalPage = Math.toIntExact(total % size == 0 ? total / size : total / size + 1);
            //从response中取结果集并封装
            Aggregations aggregations = searchResponse.getAggregations();
            if (aggregations == null){
                log.info("聚合结果为空");
                return null;
            }
            //封装分类结果
            List<Map<String, Object>> categoryAggResult = getCategoryAggResult(aggregations,categoryAggName);
            //获取参数结果集
            List<Map<String, Object>> specs = null;
            if (categoryAggResult.size() == 1){
                specs = getParamAggResult(searchRequestEntity.getPage(),size,(Long) categoryAggResult.get(0).get("id"),boolQueryBuilder);
            }
            //封装品牌结果
            List<Brand> brandAggResult = getBrandAggResult(aggregations,brandAggName);
            //封装goods结果集
            List<Goods> goodsList = new ArrayList<>();
            searchResponse.getHits().forEach(hit -> {
                Goods goods = new Goods();
                String id = String.valueOf(hit.getSourceAsMap().get("id"));
                goods.setId(Long.valueOf(id));
                goods.setSubTitle((String) hit.getSourceAsMap().get("subTitle"));
                goods.setSkus((String) hit.getSourceAsMap().get("skus"));
                goodsList.add(goods);
            });
            return new SearchResult(goodsList,total,totalPage,categoryAggResult,brandAggResult,specs);
        } catch (IOException e) {
            log.error("索引失败：{}",e);
            return null;
        }
    }

    private List<Brand> getBrandAggResult(Aggregations aggregations,String aggName){
        Terms terms = aggregations.get(aggName);
        return terms.getBuckets().stream().map(term ->
                this.brandClient.queryBrandById(term.getKeyAsNumber().longValue())).collect(Collectors.toList());
    }

    private List<Map<String,Object>> getCategoryAggResult(Aggregations aggregations,String aggName){
        Terms terms = aggregations.get(aggName);
        List<Long> cids = new ArrayList<>();
        List<Map<String,Object>> categories = new ArrayList<>();
        terms.getBuckets().forEach(term -> cids.add(term.getKeyAsNumber().longValue()));
        List<String> names = this.categoryClient.queryNamesById(cids);
        for (int i=0;i<cids.size();i++ ){
            Map<String,Object> map = Maps.newHashMap();
            map.put("id",cids.get(i));
            map.put("name",names.get(i));
            categories.add(map);
        }
        return categories;
    }

    private List<Map<String,Object>> getParamAggResult(int page,int size,Long id, BoolQueryBuilder boolQueryBuilder) throws IOException {
        List<SpecParam> specParams = this.specificationClient.queryParams(id, null, null, true);
        List<TermsAggregationBuilder> aggregationBuilders = new ArrayList<>();
        //聚合条件填充
        specParams.forEach(specParam -> aggregationBuilders.add(AggregationBuilders.terms(specParam.getName()).field("specs."+specParam.getName()+".keyword")));
        SearchResponse searchResponse = this.esUtils.search(size,
                page,
                null,
                boolQueryBuilder,
                null,
                null,
                aggregationBuilders);
        List<Map<String,Object>> paramMapList = new ArrayList<>();
        //解析聚合结果
        Aggregations aggregations = searchResponse.getAggregations();
        for (Map.Entry<String, Aggregation> entry : aggregations.getAsMap().entrySet()){
            Map<String,Object> map = Maps.newHashMap();
            //放入规格参数
            map.put("k",entry.getKey());
            List<Object> options = new ArrayList<>();
            //解析每个聚合
            Terms terms = (Terms) entry.getValue();
            terms.getBuckets().forEach(term -> options.add(term.getKeyAsString()));
            map.put("options",options);
            paramMapList.add(map);
        }
        return paramMapList;
    }

    private BoolQueryBuilder buildBoolQueryBuilder(SearchRequestEntity searchRequestEntity){
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchRequestEntity.getKey()));
        //添加条件过滤，没有过滤参数直接返回结果
        if (CollectionUtils.isEmpty(searchRequestEntity.getFilter())){
            return boolQueryBuilder;
        }
        for (Map.Entry<String,String> entry : searchRequestEntity.getFilter().entrySet()){
            String key = entry.getKey();
            //如果过滤条件是“品牌”,过滤字段名是：brandId
            if (StringUtils.equals("品牌",key)){
                key = "brandId";
            }else if (StringUtils.equals("分类",key)){
                key = "cid3";
            }else {
                key = "specs."+key+".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return boolQueryBuilder;
    }

    public void createIndex(Long id)  {
        Spu spu = this.goodsClient.querySpuById(id);
        try {
            Goods goods = this.buildGoods(spu);
            boolean save = this.esUtils.save(goods);
            if (save){
                log.info("新增商品成功，物品id：{}",goods.getId());
            }else {
                log.error("新增商品失败，物品id：{}",goods.getId());
            }
        } catch (IOException e) {
            log.error("新增商品失败,错误：{}",e);
        }
    }

    public void deleteIndex(Long id){
        try {
            boolean delete = this.esUtils.delete(id);
            if (delete){
                log.info("删除商品成功，id={}",id);
            }else {
                log.error("删除失败，id={}",id);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
