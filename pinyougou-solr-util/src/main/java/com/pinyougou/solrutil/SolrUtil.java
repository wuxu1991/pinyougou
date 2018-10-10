package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;
    public void ImportItemData(){
        //查询源数据库中tb_item表数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");//审核状态为1的时候查询所有的数据
        List<TbItem> itemList = itemMapper.selectByExample(example);

        System.out.println("--------商品列表---------");

        for (TbItem item : itemList) {
            System.out.println(item.getId()+" "+item.getTitle()+" "+item.getPrice());
            Map map = JSON.parseObject(item.getSpec(), Map.class);//从数据库中提取规格json字符串转换成map
            item.setSpecMap(map);
        }
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
        System.out.println("-------------结束--------");
    }

    public void deleteAll(){
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
    public static void main(String[] args) {
        //加载配置文件
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.ImportItemData();
        /*solrUtil.deleteAll();*/
    }
}
