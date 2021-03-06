package com.dilemma.search.rabbit;

import com.dilemma.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodsListener {
    @Autowired
    private SearchService searchService;


    /**
     * 处理insert和update的消息
     *
     * @param id spu id
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mall.create.index.queue",durable = "true"),
            exchange = @Exchange(value = "mall.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "item.insert"))
    public void listenCreate(Long id){
        if (id == null){
            return;
        }
        //删除索引
        this.searchService.createIndex(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mall.create.index.queue",durable = "true"),
            exchange = @Exchange(value = "mall.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = "item.delete"))
    public void listenDelete(Long id){
        if (id == null){
            return;
        }
        this.searchService.deleteIndex(id);
    }
}
