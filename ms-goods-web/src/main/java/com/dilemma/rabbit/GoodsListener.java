package com.dilemma.rabbit;

import com.dilemma.service.PageService;
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
    private PageService pageService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mall.create.web.queue",durable = "true"),
            exchange = @Exchange(value = "mall.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"item.insert","item.update"}
            ))
    public void listenCreate(Long id){
        if (id == null){
            return;
        }
        this.pageService.createHtml(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "mall.create.web.queue",durable = "true"),
            exchange = @Exchange(value = "mall.item.exchange",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenDelete(Long id){
        if (id == null){
            return;
        }
        this.pageService.deleteHtml(id);
    }
}
