package com.pinyougou.cart.service;

import com.pinyougou.pojogrop.Cart;

import java.util.List;

public interface CartService {
    /*添加商品到购物车，购物车集合，skuid,数量*/
    public List<Cart> addGoodsToCartList(List<Cart> list,Long itemId,Integer num);
    /*从redis中拿购物车列表*/
    public List<Cart> findCartListFromRedis(String username);
    /*向redis中添加购物车列表*/
    public void saveCartListToRedis(String username,List<Cart> cartList);

    /*合并购物车*/
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
