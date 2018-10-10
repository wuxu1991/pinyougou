package com.pinyougou.pojogrop;

import com.pinyougou.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

public class Cart implements Serializable{
    private String sellerId;//商家ID
    private String sellerName;//商家名称
    private List<TbOrderItem> orderItems;//购物车明细集合

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<TbOrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<TbOrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
