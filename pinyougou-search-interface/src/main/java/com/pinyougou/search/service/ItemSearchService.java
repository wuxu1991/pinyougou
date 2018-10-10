package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /*搜索的方法*/
    public Map search (Map searchMap);

    /*更新索引库导入SKU列表TbItem表中的数据*/
    public void importList(List list);

    //批量删除solr中的数据
    public void deleteByGoodsIds(List goodsIds);


}
