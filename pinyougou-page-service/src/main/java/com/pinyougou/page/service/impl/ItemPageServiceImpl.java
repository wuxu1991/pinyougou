package com.pinyougou.page.service.impl;

//import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageSerive;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ItemPageServiceImpl implements ItemPageSerive {
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    //注入配置文件
    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;//SKU列表
    @Override
    public boolean genItemHtml(Long goodsId) {

        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型,该数据是从后台来的dao数据层，读取商品数据
            Map map = new HashMap();
            //1.商品主表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            map.put("goods",goods);
            //2.商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            map.put("goodsDesc",goodsDesc);
            //3.商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            map.put("itemCat1",itemCat1);
            map.put("itemCat2",itemCat2);
            map.put("itemCat3",itemCat3);
            //4.读取SKU列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);//根据SPU商品的goods_id筛选
            criteria.andStatusEqualTo("1");//状态要为1
            //按是否默认字段进行降序排序，目的是返回的结果第一条为默认的SKU
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            map.put("itemList",itemList);
            //创建输出流
            Writer out = new FileWriter(pagedir+goodsId+".html");
            template.process(map,out);//输出
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long goodsId : goodsIds) {
                new File(pagedir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
