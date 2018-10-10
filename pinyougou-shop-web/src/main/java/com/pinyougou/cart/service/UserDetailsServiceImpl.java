package com.pinyougou.cart.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

//认证类，这里的参数username是用户在页面输入的用户名
public class UserDetailsServiceImpl implements UserDetailsService {
    //因为这个工程是通过dubbo来连接的，所以我们需要进行一下操作,拿到serllerService接口调用里面的findOne方法，这里调用sellerService服务是用的spring配置实现的
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //通过查看UserDetailsF4树结构发现这个UserDetails类有个User实现了这个类，所以我们要返回这个User
        System.out.println("经过的：UserDetailsServiceImpl");
        //第三个参数表示这个用户是那些角色，它是一个集合需要创建，下面两句是构建一个角色列表
        List<GrantedAuthority> grantAuths = new ArrayList<>();
        grantAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));//商家角色

        //得到商家对象
        TbSeller seller = sellerService.findOne(username);
        if (seller!=null){
            if (seller.getStatus().equals("1")){//通过审核才能登录

                return new User(username,seller.getPassword(),grantAuths);//返回null表示用户名和密码不正确,
            }else {
                return null;
            }
        }else {
            return null;
        }
    }
}
