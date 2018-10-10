 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;

                $scope.entity.typeId=[{"id": $scope.entity.typeId,"text":$scope.entity.typeId}];
                /*console.log($scope.entity.typeId)*/
			}
		);				
	}
	
	//保存 
	$scope.save=function(){

       $scope.entity.typeId=$scope.entity.typeId[0].text;
		/*console.log($scope.entity.typeId);*/
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
            $scope.entity.parentId=$scope.parentId;//赋予上级 ID
			serviceObject=itemCatService.add( $scope.entity);//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	/*$scope.reloadList();//重新加载*/
                    $scope.findByParentId($scope.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框
		itemCatService.dele( $scope.selectIds).success(
			function(response){
				if(response.success){
                    $scope.findByParentId($scope.parentId1);//重新加载*/

					alert(response.message);
				}else{
                    if ($scope.selectIds==""){
                        alert("请选中在删除");
                        return;
					}
                    alert(response.message);
                }
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    //根据上级parentId查询上级商品
	//定义一个变量去记住上级parentId
	$scope.parentId = 0;//顶级id
	$scope.findByParentId = function (parentId) {

        $scope.parentId=parentId;//记住上级 ID
        itemCatService.findByParentId(parentId).success(
            function (response) {
                $scope.list = response;
            }
        );

    }
    //类型模板下拉列表
    $scope.typeList = {data: []};//初始化变量
	$scope.findTypeList = function () {
        typeTemplateService.selectOptionList().success(
			function (response) {
				$scope.typeList = {data:response};
               /* $scope.typeList = response;*/
				/*alert(JSON.stringify($scope.typeList));*/
            }
		);
    }

//实现最后一级分类中隐藏查询下级菜单
    var v = 1;
    $scope.myVar = true;
    $scope.myHide = function () {
        v++;
        if (v==3){
            $scope.myVar = !$scope.myVar;
            v--;
        }
    }
    //定义变量来控制级别
    $scope.grade = 1;
    $scope.selectGrade = function (value) {
        $scope.grade = value;
    }
    $scope.selectList = function (p_entity) {
		if ($scope.grade==1){
			$scope.entity_1 = null;
            $scope.entity_2 = null;
            $scope.myVar = true;
			v=1;
        }
        if ($scope.grade==2){
            $scope.entity_1 = p_entity;
            $scope.entity_2 = null;
           $scope.myVar = true;

        }
        if ($scope.grade==3){
            $scope.entity_2 = p_entity;
        }
        //调用一下根据parentId查询商品
		$scope.findByParentId(p_entity.id);
    }

});	
