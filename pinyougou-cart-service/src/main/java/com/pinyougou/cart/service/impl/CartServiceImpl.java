package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogrop.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;
    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据skuID查询商品明细SKU的对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")){
            throw new RuntimeException("商品状态不合法");
        }
        //2.根据SKU对象得到商家ID
        String sellerId = item.getSellerId();
        //3.根据商家ID在购物车列表中查询购物车对象
        Cart cart = searchCartBySellerId(cartList,sellerId);
        if (cart==null){//4.如果购物车列表中不存在该商家的购物车
            //4.1创建新的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);//商家id
            cart.setSellerName(item.getSeller());//商家名称
            List<TbOrderItem> orderItemList = new ArrayList<>();//创建购物车明细列表
            TbOrderItem orderItem = createOrderItem(item,num);
            orderItemList.add(orderItem);
            cart.setOrderItems(orderItemList);
            //4.2将新的购物车对象添加到购物车列表中
            cartList.add(cart);
        }else { //5.如果购物车列表中存在该商家的购物车
            //判断该商品是否在该购物车中的明细列表中存在
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItems(),itemId);
            if (orderItem==null){
                //5.1如果不存在，创建新的购物车明细对象，并添加到该购物车的明细列表中
                orderItem = createOrderItem(item,num);
                cart.getOrderItems().add(orderItem);
            }
            //5.2如果存在，在原有的数量上添加数量，并更新金额
            orderItem.setNum(orderItem.getNum()+num);//更改数量
            //金额
            orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
            //当明细数量小于等于0，移除明细
            if (orderItem.getNum()<=0){
                cart.getOrderItems().remove(orderItem);
            }
            //当购物车的明细数量为0，在购物车列表中移除次购物车
            if (cart.getOrderItems().size()==0){
                cartList.remove(cart);
            }
        }
        return cartList;
    }
    @Autowired
    private RedisTemplate redisTemplate;
    /*从redis中拿购物车列表*/
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车"+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList = new ArrayList<>();
        }
        return cartList;
    }
    /*把购物车列表保存到redis中*/
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis中存入购物车"+username);
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }
    /*合并购物车列表*/
    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList2) {
            List<TbOrderItem> orderItems = cart.getOrderItems();
            for (TbOrderItem orderItem : orderItems) {
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItems, Long itemId) {
        for (TbOrderItem orderItem : orderItems) {
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    //创建购物车明细对象
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    //根据商家ID在购物车列表中查询购物车对象
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }
}
