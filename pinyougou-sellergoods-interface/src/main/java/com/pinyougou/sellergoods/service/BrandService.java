package com.pinyougou.sellergoods.service;
/*
 * 品牌接口
 * */
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbItem;
import entity.PageResult;

import java.util.List;
import java.util.Map;


public interface BrandService {
	public List<TbBrand> findAll();
	//分页
	public PageResult findPage(int pageNum, int pageSize);

	//增加
	public void add(TbBrand tbBrand);

	//根据id查询
    public TbBrand findOne(Long id);
    //修改
    public void update(TbBrand tbBrand);

    //删除
	public void delete(Long[] ids);

	//条件分页
	public PageResult findPage(TbBrand tbBrand ,int pageNum, int pageSize);

	//返回下拉列表数据
	public List<Map> selectOptionList();


}
