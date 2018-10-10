package com.pinyougou.page.service;

public interface ItemPageSerive {
    /*生成商品详情页goodsIdSPU的id*/
    public boolean genItemHtml(Long goodsId);

    /*删除商品详情页*/

    public boolean deleteItemHtml(Long[] goodsIds);
}
