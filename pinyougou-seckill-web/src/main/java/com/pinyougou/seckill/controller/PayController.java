package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private SeckillOrderService seckillOrderService;
    @RequestMapping("/createNative")
    public Map createNative(){
        //1.获取登录名称
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(name);
        if (seckillOrder!=null){
            return weixinPayService.createNative(seckillOrder.getId()+"",(long)(seckillOrder.getMoney().doubleValue()*100)+"");
        }else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int x = 0;
        while (true){
            Map map = weixinPayService.queryPayStatus(out_trade_no);
            if (map==null){
                result = new Result(false,"支付发生错误");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){
                result = new Result(true,"支付成功");
                //保持秒杀订单
                seckillOrderService.saveOrderFromRedisToDb(name,Long.valueOf(out_trade_no), (String) map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x>=100){
                result = new Result(false,"二维码超时");
                //关闭微信支付
                Map<String,String> payResult = weixinPayService.closePay(out_trade_no);
                if (payResult!=null && "FAIL".equals(payResult.get("return_code"))){
                    if ("ORDERPAID".equals(payResult.get("err_code"))){
                        result = new Result(true,"支付成功");
                        //保持秒杀订单
                        seckillOrderService.saveOrderFromRedisToDb(name,Long.valueOf(out_trade_no), (String) map.get("transaction_id"));
                    }
                }
                //删除缓存中的订单
                if (result.isSuccess()==false){
                    seckillOrderService.deleteOrderFromRedis(name,Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return result;
    }
}