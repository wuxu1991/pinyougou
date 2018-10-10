package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService{
    @Autowired
    private SolrTemplate solrTemplate;
    @Override
    public Map search(Map searchMap) {
        Map map=new HashMap();
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));//关键字去掉空格
        //1.高亮显示,查询列表
        map.putAll(searchList(searchMap));
        //2.分组查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //3从缓存中查询品牌和规格列表
        System.out.println(categoryList.size());
        String category = (String) searchMap.get("category");
        if (!"".equals(category)){
            map.putAll(searchBrandAndSpecList(category));
        }else {
            if (categoryList.size()>0){
                System.out.println(categoryList.get(0));
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
    }

    //把TbItem表中的数据SKU导入到索引库中，更新数据
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //高亮显示查询
    private Map searchList(Map searchMap){
        Map map=new HashMap();
        //高亮显示
        HighlightQuery query = new SimpleHighlightQuery();
        //构建高亮选项对象
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        highlightOptions.setSimplePostfix("</em>");//后缀
        query.setHighlightOptions(highlightOptions);//为查询设置高亮选项
        //1.1关键字查询，高亮显示
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //1.2按商品分类过滤
        if (!"".equals(searchMap.get("category"))){//如果用户选择了分类
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3按品牌分类过滤
        if (!"".equals(searchMap.get("brand"))){
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4按照规格过滤
        if (searchMap.get("spec")!=null){
            System.out.println(searchMap.get("spec").toString());
           Map<String,String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.5按价格过滤
        if (!"".equals(searchMap.get("price"))){
            String[] price = ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")){//如果价格不等于0
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")){//如果最高价格不等于*
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //1.6分页
        Integer pageNo = (Integer) searchMap.get("pageNo");//获取页码
        if (pageNo==null){
            pageNo = 1;
        }
       Integer pageSize = (Integer) searchMap.get("pageSize");//获取页大小
        if (pageSize==null){
            pageSize=20;
        }
        query.setOffset((pageNo-1)*pageSize);//起始索引
        query.setRows(pageSize);//每页记录数

        //1.7排序
        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        if (sortValue!=null && !sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        //返回高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {
            //获取高亮列表(高亮域的个数)
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
            /*for (HighlightEntry.Highlight h : highlightList) {
                List<String> sns = h.getSnipplets();
                System.out.println(sns);
            }*/
            if (highlightList.size()>0&&highlightList.get(0).getSnipplets().size()>0){
                TbItem item = entry.getEntity();
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//返回给前端的总页数
        map.put("total",page.getTotalElements());//返回给前端的总记录数
        return map;
    }

    private List<String> searchCategoryList(Map searchMap){
        List<String> list = new ArrayList<>();
        //普通的查询query
        Query query = new SimpleQuery("*:*");
        //关键字查询 相当于sql中的where 条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");//相当于group by
        query.setGroupOptions(groupOptions);
        //获得分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : entryList) {
            list.add(entry.getGroupValue());
        }

        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        System.out.println(1111);
        if (templateId!=null){
            System.out.println(templateId);
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            System.out.println(brandList);
            map.put("brandList",brandList);
//System.out.println("brandList="+brandList.size());
            System.out.println(3333);
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList",specList);
// System.out.println("specList="+specList.size());
            System.out.println(4444);
        }

        return map;
    }
}
