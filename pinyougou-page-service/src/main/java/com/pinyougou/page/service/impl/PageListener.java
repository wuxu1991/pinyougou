package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageSerive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/*监听类用于生成网页*/
@Component
public class PageListener implements MessageListener {
    @Autowired
    private ItemPageSerive itemPageSerive;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long goodsIds = (Long) objectMessage.getObject();
            System.out.println("接收到消息："+goodsIds);
            boolean b = itemPageSerive.genItemHtml(goodsIds);
            System.out.println("网页生成结果："+b);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
