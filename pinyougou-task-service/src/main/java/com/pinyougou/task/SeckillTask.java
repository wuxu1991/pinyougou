package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Scheduled(cron = "0/10 * * * * ?")
    public void refreshSeckillGoods(){
      System.out.println("执行了秒杀商品增量更新的任务调度"+new Date());
        List goodsList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println(goodsList);
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
            TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//审核通过的商品
            criteria.andStartTimeLessThanOrEqualTo(new Date());//开始日期小于等于当前日期
            criteria.andStockCountGreaterThan(0);//库存大于0
            criteria.andEndTimeGreaterThanOrEqualTo(new Date());//截止日期大于当前日期
            if (goodsList.size()>0){
                criteria.andIdNotIn(goodsList);//排除缓存中已经存在的商品ID集合
            }
            List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
            for (TbSeckillGoods tbSeckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoods").put(tbSeckillGoods.getId(),tbSeckillGoods);
                System.out.println("增量更新秒杀商品Id："+tbSeckillGoods.getId());
            }
        System.out.println("...end...");
     /* List goodsList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        if (goodsList.size()>0){
            for (Object o : goodsList) {
                redisTemplate.boundHashOps("seckillGoods").delete(o);
                System.out.println("清空了缓存id："+o);
            }
        }
        System.out.println("没任务了"+new Date());*/
    }
   @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods(){
        //查询出缓存中的数据，扫描每条记录，判断时间，如果当前时间超过了截止时间，移除此记录
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
//        System.out.println("执行了清除秒杀商品的任务"+new Date());

        for (TbSeckillGoods seckillGood : seckillGoods) {
//            System.out.println((new Date().getTime())-(seckillGood.getEndTime().getTime()));
//            System.out.println(seckillGood.getEndTime());
//            System.out.println(new Date());

           if (seckillGood.getEndTime().getTime()<new Date().getTime()){
                //同步数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGood);
//                System.out.println("111111111111");
                //清空缓存
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGood.getId());
                System.out.println("秒杀商品333"+seckillGood.getId()+"已过期");
            }
        }
//        System.out.println("执行了清除秒杀商品的任务...end");
    }
}
