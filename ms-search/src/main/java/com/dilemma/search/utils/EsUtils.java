package com.dilemma.search.utils;

import com.dilemma.search.pojo.Goods;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class EsUtils {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private BulkProcessor bulkProcessor;

    private static final String INDEX_NAME = "goods";

    @PostConstruct
    public void initBulkProcessor(){
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            //前置处理
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                //需要执行批量的记录数
                int record = request.numberOfActions();
                log.info("Executing bulk [{}] with {} requests", executionId, record);
            }

            //后置处理
            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
                if (response.hasFailures()){
                    //失败后置处理
                    log.info("批量处理失败，失败执行id：{}，失败信息：{}",executionId,response.buildFailureMessage());
                }else {
                    log.info("批量处理完成，执行id：{}，耗时：{}毫秒",executionId,response.getTook().getMillis());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
                log.info("批量存储失败",failure);
            }
        };
        //在这里调用build()方法构造bulkProcessor,在底层实际上是用了bulk的异步操作
        this.bulkProcessor = BulkProcessor.builder((bulkRequest, bulkResponseActionListener) ->
                restHighLevelClient.bulkAsync(bulkRequest,RequestOptions.DEFAULT,bulkResponseActionListener),listener)
                //执行最大记录数
                .setBulkActions(100)
                //5mb刷新一次数据
                .setBulkSize(new ByteSizeValue(5L,ByteSizeUnit.MB))
                //是否并发执行，0不并发，1并发执行
                .setConcurrentRequests(0)
                //固定1秒刷新一次数据
                .setFlushInterval(TimeValue.timeValueSeconds(1L))
                //重试5次，间隔1s
                .setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueSeconds(1L),5))
                .build();
    }

    @PreDestroy
    public void destroyBulkProcessor(){
        try {
            bulkProcessor.awaitClose(30,TimeUnit.SECONDS);
            log.info("bulkProcessor执行关闭！");
        } catch (InterruptedException e) {
            log.info("bulkProcessor closed!");
        }
    }

    public void createIndex(String index){
        try {
            if (this.existsIndex(index)){
                log.info("索引库已经存在！无需重复创建");
                return;
            }
        } catch (IOException e) {
            log.error("索引库校验错误！信息：{}",e.getMessage());
        }
        //创建名字为index的索引库
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        try {
//            Map<String,Object> properties = Maps.newHashMap();
//            Map<String,Object> property = Maps.newHashMap();
//            property.put("type","text");
//            property.put("index","true");
//            property.put("analyzer","ik_max_word");
//            properties.put("all",property);
//
//            XContentBuilder builder = JsonXContent.contentBuilder();
//            builder.startObject()
//                        .startObject("mappings")
//                            .startObject("goods")
//                                .field("properties",properties)
//                            .endObject()
//                        .endObject()
//                        .startObject("settings")
//                            .field("number_of_shards",3)
//                            .field("number_of_replicas",1)
//                        .endObject()
//                    .endObject();
            //初始化配置settings包括分片等信息
            ClassPathResource settingsResource = new ClassPathResource("mapper/settings.json");
            InputStream settingsInputStream = settingsResource.getInputStream();
            //把读取到的数据转换成json字符串
            String settingsJson = String.join("\n", IOUtils.readLines(settingsInputStream, "utf-8"));
            settingsInputStream.close();
            //读取mapping相关的信息
            ClassPathResource mappingResource = new ClassPathResource("mapper/goods-mapping.json");
            InputStream mappingInputStream = mappingResource.getInputStream();
            String mappingJson = String.join("\n", IOUtils.readLines(mappingInputStream, "utf-8"));
            mappingInputStream.close();
            //如果没有特殊的要求可以使用默认的，直接转换类信息
            //通过createIndexRequest设置信息
//            XContentBuilder builder = XContentFactory.jsonBuilder()
////                    .startObject()
////                    .field()
            createIndexRequest.settings(settingsJson,XContentType.JSON);
            createIndexRequest.mapping(mappingJson,XContentType.JSON);
            //createIndexRequest.alias(new Alias("_alia")); 别名设置
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (createIndexResponse.isAcknowledged()){
                log.info("索引库{}创建成功",index);
            }else {
                log.info("索引库创建失败");
            }
        } catch (IOException e) {
            log.error("读取配置文件失败",e);
        }
    }

    private boolean existsIndex(String index) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(index);
        getIndexRequest.local(false);
        getIndexRequest.humanReadable(true);
        return restHighLevelClient.indices().exists(getIndexRequest,RequestOptions.DEFAULT);
    }

    /**
     * 批量新增
     * @param index 库名
     * @param list 数据集合
     */
    public boolean saveAll(String index,List<Goods> list) {
        BulkRequest bulkRequest = new BulkRequest();
        if (list != null){
            list.forEach(goods -> bulkRequest.add(new IndexRequest(index)
                    .id(goods.getId().toString())
                    .source(toMap(goods),XContentType.JSON)));
            try {
                BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                log.info("批量新增数据成功,插入库index={},每次请求条数：{},完成执行时间：{}",index,bulkRequest.numberOfActions(),bulk.getTook().getMillis());
                return true;
            } catch (IOException e) {
                log.error("批量新增数据失败",e);
                return false;
            }
        }else {
            return false;
        }
    }

    public boolean save(Goods goods) throws IOException {
        IndexRequest indexRequest = new IndexRequest(INDEX_NAME);
        indexRequest.id(String.valueOf(goods.getId())).source(toMap(goods),XContentType.JSON);
        IndexResponse response = this.restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        if (response.getResult() == DocWriteResponse.Result.CREATED){
            log.info("新增文档成功，id={}",response.getId());
            return true;
        }else {
            return false;
        }
    }

    public boolean delete(Long id) throws IOException {
        DeleteRequest request = new DeleteRequest(INDEX_NAME,id.toString());
        DeleteResponse response = this.restHighLevelClient.delete(request, RequestOptions.DEFAULT);
        if (response.getResult() == DocWriteResponse.Result.NOT_FOUND){
            log.info("未找到id为{}的文档",id);
            return false;
        }else {
            log.info("delete doc success which id is {}",id);
            return true;
        }
    }

    private Map<String,Object> toMap(Object obj){
        if (obj == null){
            log.info("传入对象为空，转换失败");
            return null;
        }
        Map<String,Object> map = Maps.newHashMap();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field :declaredFields) {
            field.setAccessible(true);
            try {
                map.put(field.getName(),field.get(obj));
            } catch (IllegalAccessException e) {
                log.error("转换失败，请检查传入对象，错误信息={}",e.getMessage());
            }
        }
        return map;
    }

    private Object toObject(Map<String,Object> map,Class<?> beanClass) throws IllegalAccessException, InstantiationException {
        if (map == null){
            return null;
        }
        Object object = beanClass.newInstance();
        //取字段
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            int mod= field.getModifiers();
            //判断是否static，final修饰，如果是直接跳过
            if (Modifier.isStatic(mod) || Modifier.isFinal(mod)){
                continue;
            }
            //设置private可见
            field.setAccessible(true);
            field.set(object,map.get(field.getName()));
        }
        return object;
    }

    /**
     * 复杂查询并做聚合结果
     * @param size 页面大小
     * @param page 页数
     * @param includes 过滤保留字段
     * @param boolQueryBuilder bool查询构建器
     * @param sortBy 排序字段
     * @param descending 升序降序
     * @param aggregationBuilders 聚合构建器
     * @return response 响应结果
     * @throws IOException IO异常对象
     */
    public SearchResponse search(int size,
                                 int page,
                                 String[] includes,
                                 BoolQueryBuilder boolQueryBuilder,
                                 String sortBy,
                                 Boolean descending,
                                 List<TermsAggregationBuilder> aggregationBuilders) throws IOException {
        //初始化查询构建器
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(size*(page-1)+1);
        sourceBuilder.size(size);
        //设置一个超时限制
        sourceBuilder.timeout(new TimeValue(60,TimeUnit.SECONDS));
        //字段过滤
        sourceBuilder.fetchSource(includes,null);
        //排序字段不为空，进行排序
        if (!StringUtils.isBlank(sortBy)){
            sourceBuilder.sort(new FieldSortBuilder(sortBy).order(descending ? SortOrder.DESC:SortOrder.ASC));
        }
        //进行元素聚合
        if (aggregationBuilders.size()>0){
            aggregationBuilders.forEach(sourceBuilder::aggregation);
        }
        sourceBuilder.query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        searchRequest.source(sourceBuilder);
        return restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
    }
}
