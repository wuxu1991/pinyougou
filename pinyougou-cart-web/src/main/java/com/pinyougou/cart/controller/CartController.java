package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogrop.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 6000)
    private CartService cartService;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        //当前登录人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人"+name);
        //从cookie中提取购物车
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString==null || cartListString.equals("")){
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (name.equals("anonymousUser")){//如果未登录
            //从cookie中提取购物车
            System.out.println("从cookie中提取购物车");
            return cartList_cookie;
        }else {//如果已登录
            //从redis中拿购物车列表
            System.out.println("从redis中提取购物车");
            List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
            if (cartList_cookie.size()>0){//判断当本地购物车中存在数据
                //得到合并后的购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
                //将合并后的购物车存入redis
                cartService.saveCartListToRedis(name,cartList);
                //本地购物车清除
                CookieUtil.deleteCookie(request,response,"cartList");
                System.out.println("执行了合并购物车的逻辑");
                return cartList;
            }
            return cartList_redis;
        }
    }
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId,Integer num){
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");//可以访问的域(当此方法不需要操作cookie)
        //response.setHeader("Access-Control-Allow-Credentials", "true");//如果操作cookie，必须加上这句话
        //当前登录人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人"+name);
        try {
            //提取购物车
            List<Cart> cartList = findCartList();
            //操作服务方法返回购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (name.equals("anonymousUser")){//如果未登录
                String cartListString = JSON.toJSONString(cartList);
                //将购物车列表在存入cookie中
                CookieUtil.setCookie(request,response,"cartList",cartListString,3600*24,"UTF-8");
                System.out.println("向cookie中存储购物车");
            }else {//如果已登录
                cartService.saveCartListToRedis(name,cartList);
                System.out.println("向redis中存储购物车");
            }
            return new Result(true,"添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加购物车失败");
        }

    }
}
