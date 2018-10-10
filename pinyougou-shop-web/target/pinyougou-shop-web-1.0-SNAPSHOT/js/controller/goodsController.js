 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		//angularjs中的本地服务，可以用来静态页面跳转传递参数的，调用search方法返回数组，这样就可以在传递参数的时候，可以传递多个参数
        var id = $location.search()['id'];
        if (id==null){return;}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;//返回回来的是组合实体来Goods

				editor.html($scope.entity.goodsDesc.introduction);//商品详情
				//商品图片
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				//扩展属性，这里拿到以后，会覆盖下面根据模板id拿到扩展属性的代码，所以要做一个非空的判断
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//显示规格选择
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

				//查询出来的规格选项是个字符串，我们要循环遍历它，转换成对象，给页面使用
				//转换sku列表中的规格对象
				// var items = $scope.entity.itemList;
				for (var i=0; $scope.entity.itemList.length;i++){
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
        $scope.entity.goodsDesc.introduction = editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
                    alert("保存成功");
                    /*$scope.entity = {};
                    //保存成功以后让其清空富文本
                    editor.html('');*/
                    //用原生的js跳转
					location.href = "goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}

    //添加
   /* $scope.add=function(){
        $scope.entity.goodsDesc.introduction = editor.html();
        goodsService.add( $scope.entity).success(
            //调用方法赋值给商品扩展中的introduction属性
            function(response){
                if(response.success){
                    alert("新增加成功");
                    $scope.entity = {};
                    //保存成功以后让其清空富文本
                    editor.html('');
                }else{
                    alert(response.message);
                }
            }
        );
    }*/
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//文件上传
    $scope.uploadFile=function(){
        uploadService.uploadFile().success(
            function(response){
                if(response.success){
                    $scope.image_entity= response;
                    alert($scope.image_entity)
                    $scope.image_entity.url = $scope.image_entity.message;
                }else{
                    alert(response.message);
                }
            }
        );
    }
	//把集合定义出来
	$scope.entity = {goods:{},goodsDesc:{itemImages:[],specificationItems:[]}}
	//把商品图片信息添加到列表集合中
	$scope.add_image_entity = function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }
    //删除图片信息
	$scope.dele_image_entity = function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
    }
    //通过parentId查询表tb_item_cat一级分类商品中的name
	$scope.selectItemCat1List = function () {
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List = response;
            }
		);
    }
    //通过监测页面中的变量监测newValue的变化二级商品
	$scope.$watch('entity.goods.category1Id',function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat2List = response;
            }
        );
    })
    $scope.$watch('entity.goods.category2Id',function (newValue,oldValue) {
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.itemCat3List = response;
            }
        );
    })

	//查询模板id:typeTemplateId
	$scope.$watch('entity.goods.category3Id',function (newValue,oldValue) {
		itemCatService.findOne(newValue).success(
			function (response) {
                $scope.entity.goods.typeTemplateId =response.typeId;
                alert($scope.entity.goods.typeTemplateId)
            }
		)
    });

	//根据读取模板Id,查询品牌列表
	$scope.$watch('entity.goods.typeTemplateId',function (newValue,oldValue) {
		typeTemplateService.findOne(newValue).success(
			function (response) {
				//因为response.brandIds返回的是一个字符串，所以要变成json对象才能遍历获取里面品牌的值,select这个标签中ng-option属性中是传递json对象的，字符串不行，所以要转换
               /* $scope.typeTemplate.brandIds = response.b;*/
                $scope.typeTemplate  = response;
                alert($scope.typeTemplate.brandIds)
				$scope.typeTemplate.brandIds = JSON.parse(response.brandIds);

				//扩展属性也是通过检测模板Id来查询表tb_type_template中的custom_attribute_items字段，当保存的时候给这个表tb_goods_desc表中的custom_attribute_items这个字段
				//这里这样写的
				if ($location.search()['id']==null){//增加时候的操作
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
				}

            }
		)

		//查询规格选项newValue=35模板表tb_type_template中的id
		typeTemplateService.findSpecList(newValue).success(
			function (response) {
				$scope.specList=response;
            }
		);
    })

	//[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5.5寸"]}] 初始化一下集合

	$scope.updateSpecAttribute = function ($event,keyValue,value) {
		//传递集合的时候集合给这个集合初始化定义一下$scope.entity.goodsDesc.specificationItems，在上面的代码中有初始化定义属于成员的位置
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',keyValue);
		if (object==null){
			//向集合中添加元素
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":keyValue,"attributeValue":[value]});
		}else {
			if ($event.target.checked){//如果没有选中直接添加
                //不是第一次点击规格选项，那么直接向集合中的attributeValue的key中添加值
                object.attributeValue.push(value);
			}else {//取消勾选就移除勾选项
                object.attributeValue.splice(object.attributeValue.indexOf(value),1);
                //如果没有勾选的时候，把集合中的一条记录移除
				if (object.attributeValue.length==0){
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}

		}
    }

    //SKU列表_变量构建，实现思路：
    /*1.首先我们看页面分析出来，它是一个$scope.entity.goodsDesc.specificationItems这个集合下去演变新的集合，也就是说当我们在点击规格选项的时候，去检测这个老集合的变化，这里用到的是深克隆的技术，(创建一个新的集合，在遍历老集合，向新集合中添加元素，最后返回，这样就可以实现点击规格选项的时候集合翻倍增加)
    * */
    //定义一个方法
	$scope.createList = function () {
		//初始化要返回的新集合给页面展示,通过了解这种的数据是存在tb_item这个表中的，所以定义集合的时候，我们选用之前定义pojo组合商品实体类Goods中的itemList这的属性进行创建集合,集合中放的是对象，这里初始化属于局部的位置
		$scope.entity.itemList = [{spec:{},price:0,num:99999,status:'0',isDefault:'0'}];
		//循环遍历这个$scope.entity.goodsDesc.specificationItems老集合
		var items = $scope.entity.goodsDesc.specificationItems;
		for (var i=0;i<items.length;i++){
			//定义一个方法实现向集合中添加元素，并返回新的集合,记得返回定义的这个$scope.entity.itemList集合，调用了这个createList方法以后，因为angularJs双向绑定中$scope实现的，所以在页面中就可以看见展示的数据
            $scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
    }

    //参数：columnName：表示列的名称，也就是[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5.5寸"]}]这个集合中的attributeName对应的值"网络制式"，columnValues:表示的是这个集合中attributeValue的key对应的里面的集合
    addColumn = function (list,columnName,columnValues) {
		//创建一个新的集合返回该新集合
		var newList = [];
		//通过页面展示的效果分析这里要进行深克隆，需要两层for循环遍历，才能把数据填完整
		for (var i=0;i<list.length;i++){
			//定义一个遍历接收老的集合
			var oldRowList = list[i];
			for (var j=0;j<columnValues.length;j++){
				//深克隆,把原来的老集合赋值一下成为一个新的集合，在往这个新集合中添加元素,数据表中tb_item中的spec字段存的值是{"网络":"移动4G","机身内存":"32G"}
               var newRowList = JSON.parse(JSON.stringify(oldRowList));
               //克隆完以后向这个新集合中spec对象中添加属性
                newRowList.spec[columnName] = columnValues[j];
                //把原来的集合push到新定义的newList集合中去
				newList.push(newRowList);
			}
		}
		return newList;
    }

    $scope.status = ['未审核','已审核','审核未通过','已关闭'];

	//查询商品分类表
	$scope.itemCatList = [];//商品分类列表
	$scope.findItemCatList = function () {
		itemCatService.findAll().success(
			function (response) {
                for (var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id] = response[i].name;
				}
            }
		)
    }

    //定义一个方法实现拿到specification这个集合中的属性值，判断其值是不不是有，如果没有返回false，让其不勾选
	//[{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},{"attributeValue":["16G","32G"],"attributeName":"机身内存"}]
	//specName = 网络   optionName = 移动3G,移动4G,这两个都是前端传递过来的
	$scope.checkAttributeValue = function (specName,optionName) {
		var items = $scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(items,'attributeName',specName);

		if (object!=null){
			if(object.attributeValue.indexOf(optionName)>=0){//如果能查询到规格选项
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
    }
});	
