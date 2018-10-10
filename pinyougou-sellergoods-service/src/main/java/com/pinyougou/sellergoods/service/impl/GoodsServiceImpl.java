package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogrop.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbSellerMapper sellerMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//新添加的状态是未审核的，所以要设置一下
		goods.getGoods().setAuditStatus("0");
		goodsMapper.insert(goods.getGoods());
		/*int i = 1/0;*/
		//将商品的id设置给商品扩展GoodsId
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		//插入扩展商品
		goodsDescMapper.insert(goods.getGoodsDesc());
		//插入sku商品数据
		setItemList(goods);
	}
	//插入sku商品数据
	private void setItemList(Goods goods){
		if("1".equals(goods.getGoods().getIsEnableSpec())){
			for(TbItem item:   goods.getItemList()){
				//构建标题  SPU名称+ 规格选项值
				String title=goods.getGoods().getGoodsName();//SPU名称
				Map<String,Object> map=  JSON.parseObject(item.getSpec());//JSON对象
				for(String key:map.keySet()) {
					title+=" "+map.get(key);
				}
				item.setTitle(title);

				setItemValues(item,goods);

				itemMapper.insert(item);
			}
		}else{//没有启用规格

			TbItem item=new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//标题
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setNum(99999);//库存数量
			item.setStatus("1");//状态
			item.setIsDefault("1");//默认
			item.setSpec("{}");//规格

			setItemValues(item,goods);

			itemMapper.insert(item);
		}
	}
	private void setItemValues(TbItem item,Goods goods){
		//商品分类
		item.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
		item.setCreateTime(new Date());//创建日期
		item.setUpdateTime(new Date());//更新日期

		item.setGoodsId(goods.getGoods().getId());//商品ID
		item.setSellerId(goods.getGoods().getSellerId());//商家ID

		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//商家名称(店铺名称)
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());

		//图片
		List<Map> imageList = JSON.parseArray( goods.getGoodsDesc().getItemImages(), Map.class) ;
		if(imageList.size()>0){
			item.setImage( (String)imageList.get(0).get("url"));
		}

	}
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//更新基本表数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//更新扩展表属性
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//更新sku表的是数据，这里的思路是先删除数据库中原有的数据，再进行添加进去
		//先删除
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);
		//插入sku商品数据
		setItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);

		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> items = itemMapper.selectByExample(example);
		goods.setItemList(items);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();//过滤只有没有逻辑删除的才能显示出来
		if(goods!=null){			
			if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
//				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
				//精确的查询
				criteria.andSellerIdEqualTo(goods.getSellerId());
			}

			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id : ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	public List<TbItem> findItemListByGoodsIdListAndStatus(Long[] goodsIds,String status){
        List<TbItem> items = itemMapper.selectByExample(null);
        for (TbItem item : items) {
            String status1 = item.getStatus();
            if (goodsIds==null || !status.equals(status1)){
                return null;
            }
        }
        TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo(status);//添加条件状态
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));//添加条件goodsIds
		return itemMapper.selectByExample(example);
	}
}
