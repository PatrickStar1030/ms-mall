package com.dilemma.item.service.impl;

import com.dilemma.common.dto.CartDto;
import com.dilemma.common.enums.ExceptionEnum;
import com.dilemma.common.exception.MsException;
import com.dilemma.common.vo.PageResult;
import com.dilemma.item.mapper.*;
import com.dilemma.item.pojo.*;
import com.dilemma.item.service.CategoryService;
import com.dilemma.item.service.GoodsService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    /**
     * @param key 过滤（搜索）条件
     * @param saleable 是否上架
     * @param page 页数
     * @param row 每页展示行数
     * @return 分页结果集
     */
    @Override
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer row) {
        //Spu查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索条件
        if (StringUtils.isNotBlank(key)){
            //把title作为过滤条件
            criteria.andLike("title","%"+key+"%");
        }
        //是否上架
        if (saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        example.setOrderByClause("last_update_time DESC");
        //设置分页条件
        PageHelper.startPage(page, row);
        //根据example查spu
        List<Spu> spus = this.spuMapper.selectByExample(example);
        //根据spu结果创建分页信息
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);
        //存spuBo
        List<SpuBo> spuBos = new ArrayList<>();
        spus.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
            //spring工具类，copy共同属性的值到新对象
            BeanUtils.copyProperties(spu,spuBo);
            //query category_name,separator by "/"
            List<String> names = this.categoryService.queryNamesByIds(
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names,"/"));
            //查询品牌名称
            spuBo.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());
            spuBos.add(spuBo);
        });
        return new PageResult<>(spuPageInfo.getTotal(),spuPageInfo.getPages(),spuBos);
    }

    /**
     * 商品信息保存
     * 含有spu，sku，detail信息
     * @param spuBo 商品信息
     */
    @Transactional
    @Override
    public void saveGoods(SpuBo spuBo) {
        //新增spu
        //设置默认字段
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        this.spuMapper.insertSelective(spuBo);

        //新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.insertSelective(spuDetail);

        saveSkuAndStock(spuBo);

        sendMessage(spuBo.getId(),"insert");
    }

    /**
     * update goods
     * spu数据可以修改，但是SKU数据无法修改，因为有可能之前存在的SKU现在已经不存在了，或者以前的sku属性都不存在了。
     * 比如以前内存有4G，现在没了。
     * 因此这里直接删除以前的SKU，然后新增即可
     * @param spuBo
     */
    @Transactional
    @Override
    public void updateGoods(SpuBo spuBo) {
        //查询以前的sku
        List<Sku> skus = this.querySkuBySpuId(spuBo.getId());
        //如果sku存在，则删除
        if (!CollectionUtils.isEmpty(skus)){
            List<Long> ids = skus.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            //删除以前的库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);

            //删除以前的sku
            Sku oldSku = new Sku();
            oldSku.setSpuId(spuBo.getId());
            this.skuMapper.delete(oldSku);
        }
        //新增sku和库存
        saveSkuAndStock(spuBo);
        //更新spu
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        this.spuMapper.updateByPrimaryKeySelective(spuBo);
        //更新spu详情
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());
        sendMessage(spuBo.getId(),"update");
    }

    /**
     * 根据spu_id删除商品
     * @param id spu_id
     */
    @Transactional
    @Override
    public void deleteGoodsById(Long id) {
        List<Sku> skus = this.querySkuBySpuId(id);
        if (!CollectionUtils.isEmpty(skus)){
            List<Long> ids = skus.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            //删除以前的库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);
            //删除以前的sku
            Sku oldSku = new Sku();
            oldSku.setSpuId(id);
            this.skuMapper.delete(oldSku);
        }else {
            throw new MsException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //删除spu
        this.spuMapper.deleteByPrimaryKey(id);
        //删除detail
        this.spuDetailMapper.deleteByPrimaryKey(id);
    }

    private void saveSkuAndStock(SpuBo spuBo){
        spuBo.getSkus().forEach(sku -> {
            //新增sku
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insertSelective(sku);

            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
    }

    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail==null){
            throw new MsException(ExceptionEnum.SPUDETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    @Override
    public List<Sku> querySkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = this.skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)){
            throw new MsException(ExceptionEnum.SKU_NOT_FOUND);
        }
        skus.forEach(s -> {
            //顺便查出对应的stock，方便表单回显
            Stock stock = this.stockMapper.selectByPrimaryKey(s.getId());
            s.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 商品上下架
     * 后续需要添加上架商品至es，给用户界面做浏览
     * @param spuId spu_id
     */
    @Transactional
    @Override
    public void updateGoodsSaleableStateById(Long spuId) {
        Spu spu = this.spuMapper.selectByPrimaryKey(spuId);
        if (spu!=null){
            if (spu.getSaleable()){
                //商品下架
                spu.setSaleable(false);
            }else {
                //商品上架
                spu.setSaleable(true);
                //添加商品至mongodb
            }
            this.spuMapper.updateByPrimaryKeySelective(spu);
            //TODO 后续把后台管理系统中点击上架的商品同时缓存到elasticsearch中
        }else {
            throw new MsException(ExceptionEnum.GOODS_NOT_FOUND);
        }
    }

    @Override
    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }

    @Override
    public Sku querySkuById(Long id) {
        return this.skuMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Sku> querySkuByIds(List<Long> ids) {
        return this.skuMapper.selectByIdList(ids);
    }

    //利用mysql实现一个乐观锁
    //减库存
    @Override
    @Transactional
    public void decreaseStock(List<CartDto> carts) {
        for (CartDto cartDto:carts){
            int i = this.stockMapper.decreaseStock(cartDto.getSkuId(), cartDto.getNum());
            if (i != 1){
                throw new MsException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }

    private void sendMessage(Long id,String type){
        try {
            this.amqpTemplate.convertAndSend("item." + type,id);
        } catch (AmqpException e) {
            log.error("{}商品消息发送异常，商品id：{}", type, id, e);
        }
    }

}
