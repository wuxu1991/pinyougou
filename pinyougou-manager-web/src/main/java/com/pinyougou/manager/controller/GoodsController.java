package com.pinyougou.manager.controller;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogrop.Goods;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;




/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	/*运营商没有商品的添加功能
	@RequestMapping("/add")
	public Result add(@RequestBody TbGoods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}*/
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@Autowired
	private Destination queueSolrDeleteDestination;
	@Autowired
	private Destination topicPageDeleteDestination;
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			//物理删除数据库中的数据把值改为1
			goodsService.delete(ids);
			//从索引库中删除数据
//			itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			//删除每个服务器上的商品详细页
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	/*@Reference(timeout = 100000)
	private ItemSearchService itemSearchService;*/

	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination queueSolrDestination;//用于导入solr索引库的消息目标(点对点)
	@Autowired
	private Destination topicPageDestination;//用于生产商品详细页的消息目标(发布订阅)

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status) {
		try {

			if ("1".equals(status)){
				//****导入到索引库
				//根据SPU的id查询SKU列表
				List<TbItem> list = goodsService.findItemListByGoodsIdListAndStatus(ids, status);
				//更新solr索引库
				if (list!=null){
//					itemSearchService.importList(list);
					/*使用MQ实现了解耦合的操作，在当前的工程下，把依赖pinyougou-search-interface的坐标删除，通过MQ的点对点实现信息和数据的传递，MQ有五种传递数据的类型(1.文本类型，2.对象类型{对象要实现可序列化接口的}，3Map类型，4.字节流类型，5.原始类型)。这里要传递的是list集合，但是是对象类型要求传递的数据必须是实现可序列化接口的。显然list没有实现可序列化接口。所以使用传统的文本数据类型，把list集合转化成json字符串。然后通过createTextMessage(json字符串)*/
					//使用fastJson把集合转换成json字符串，进行传递
					final String jsonString = JSON.toJSONString(list);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});
					goodsService.updateStatus(ids,status);
					//****生成商品详情页
					for (final Long goodsId:ids){
//						itemPageSerive.genItemHtml(goodsId);
                        jmsTemplate.send(topicPageDestination, new MessageCreator() {
                            @Override
                            public Message createMessage(Session session) throws JMSException {
                                return session.createObjectMessage(goodsId);
//                                return session.createTextMessage(goodsId+"");
                            }
                        });
					}
					return new Result(true,"成功");
				}

			}
			return new Result(false,"失败");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false,"失败");
		}
	}
 	/*@Reference(timeout = 400000)
    private ItemPageSerive itemPageSerive;*/
	@RequestMapping("/genHtml")
	public void genHtml(long goodsId){
		/*itemPageSerive.genItemHtml(goodsId);*/
    }
}
