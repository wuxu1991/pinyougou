 //控制层 
app.controller('typeTemplateController' ,function($scope,$controller   ,typeTemplateService,brandService,specificationService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	/*$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    */
	
	//分页
	/*$scope.findPage=function(page,rows){
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}*/
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;
				/*alert(JSON.stringify(response))*/
				// alert($scope.entity.specIds)
				//把原有的字符串转化成json对象或者json集合
				//entity.brandIds要和页面中标签的select2-model="entity.brandIds"一样，这里的brandIds是实体类TbTypeTemplate类中的属性entity相当于是这个TbTypeTemplate类的对象

				$scope.entity.brandIds = JSON.parse($scope.entity.brandIds);//JSON是js本身内置的
                console.log($scope.entity.brandIds);
                $scope.entity.specIds = JSON.parse($scope.entity.specIds);
                $scope.entity.customAttributeItems = JSON.parse($scope.entity.customAttributeItems);
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		//stringify这个方法的含义把json对象或者集合转成json字符串的形式
         // alert(JSON.stringify($scope.entity.specIds))
		//弹出来的是json对象
        /*alert($scope.entity.specIds)*/
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		typeTemplateService.dele( $scope.selectIds ).success(
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
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				/*alert(JSON.stringify(response))*/

				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
                /*[object Object],[object Object],[object Object],[object Object]
                * 这里不是字符串，所以alert出来的是JSon对象，我们把Json对象转化成json字符
                * 看一下结果
                * */
				/*alert(JSON.stringify(response))*/
			}			
		);
	}
    //定义一个变量来存储数据，然后给页面，格式是按照select2中的config3的定义格式来写的

	// $scope.brandList = {data: [{id:1,text:'联想'},{id:2,text:'华为'},{id:3,text:'中兴'}]};
    $scope.brandList = {data: []};//初始化变量
	//后台的代码实现：因为上面的数据可定是后台发送回来的数据，那么现在要思考一个问题：后台要给的数据肯定是[{id:1,text:'联想'},{id:2,text:'华为'},{id:3,text:'中兴'}]这样的格式，要不然你就给一个对象，前端进行封装也是可以的，我们现在做的是给这个格式，通过查看数据库中tb_type_template表我们发现数据中的字段不是一致的，其中的name要变成text格式，这里解决的办法是让他封装成一个map集合，这里需要在数据访问层dao去做这件事来到TbBrandMapper.xml中添加select查询语句,写完以后就是来到TbBrandMapper中添加反回list集合的方法，接下来就是去service，BrandService接口中添加反回list集合的方法，然后在实现类中去实现具体的代码逻辑,最后去controller层去写具体的代码
	//读取品牌列表
	$scope.findBrandList = function () {
		brandService.selectOptionList().success(
			function (response) {
                $scope.brandList = {data:response};
                //弹出来的json对象
				/*alert($scope.brandList)*/
            }
		);
    }
    //规格下拉列表
    $scope.specList = {data:[]};//定义的变量初始化
    $scope.findSpecList = function () {
		specificationService.selectOptionList().success(
			function (response) {
				$scope.specList = {data:response}
				/*alert(JSON.stringify($scope.specList))*/
            }
		);
    }

    //扩展属性
	//新增行
	$scope.addTableRow = function () {
        //customAttributeItems是对应表tb_type_template中的Custom_attribute_items字段它其实在TbTypeTemplate这个类中的属性
		$scope.entity.customAttributeItems.push({});//这里还需要对customAttributeItems初始化给entity，那么就在type_template.html页面中的新建的位置，进行初始化，ng-click="entity={customAttributeItems:[]}"
    }

    //删除行
	$scope.deleTableRow = function (index) {
		$scope.entity.customAttributeItems.splice(index,1);
    }
});	
