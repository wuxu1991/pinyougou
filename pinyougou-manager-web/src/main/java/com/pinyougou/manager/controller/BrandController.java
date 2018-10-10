package com.pinyougou.manager.controller;

import java.util.List;
import java.util.Map;

import entity.PageResult;
import entity.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/*@RestController = @ResponseBody+@Controller
*/
@RestController
@RequestMapping("/brand")
public class BrandController {
	
	@Reference
	private BrandService brandService;
	
	@RequestMapping("/findAll")
	public List<TbBrand> findAll(){
		return brandService.findAll();
	}

	@RequestMapping("/findPage")
	public PageResult findPage(int pageNum, int pageSize){
		return brandService.findPage(pageNum, pageSize);
	}

	//增加
	@RequestMapping("/add")
	public Result add(@RequestBody TbBrand tbBrand){
		try {
			brandService.add(tbBrand);
			return new Result(true,"增加成功");
		} catch (Exception e) {
			e.printStackTrace();

			return new Result(false,"增加失败");
		}
	}

    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
	    return brandService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand tbBrand){
        try {
            brandService.update(tbBrand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();

            return new Result(false,"修改失败");
        }
    }
	@RequestMapping("/delete")
	public Result delete(Long[] ids){
		try {
			brandService.delete(ids);
			return new Result(true,"删除成功");
		} catch (Exception e) {
			e.printStackTrace();

			return new Result(false,"删除失败");
		}
	}

	//条件查询
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbBrand tbBrand ,int pageNum, int pageSize){
		return brandService.findPage(tbBrand,pageNum,pageSize);
	}

	@RequestMapping("/selectOptionList")
	public List<Map> selectOptionList(){
		return brandService.selectOptionList();
	}
}
