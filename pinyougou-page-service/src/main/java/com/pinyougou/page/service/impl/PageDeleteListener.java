package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageSerive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class PageDeleteListener implements MessageListener {
    @Autowired
    private ItemPageSerive itemPageSerive;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("收到消息："+goodsIds);
            boolean b = itemPageSerive.deleteItemHtml(goodsIds);
            System.out.println("删除商品详情页："+b);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
