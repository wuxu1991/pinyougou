app.controller('searchController',function($scope,$location,searchService){
	
	//定义搜索对象的结构  category:商品分类
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
	
	//搜索
	$scope.search=function(){
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);//转换成数字
		searchService.search($scope.searchMap).success(
			function(response){

				$scope.resultMap=response;
				// $scope.searchMap.pageNo = 1;//查询后显示第一页
                //构建分页拦
                buildPageLabel();

			}
		);		
	}

	buildPageLabel = function () {
        //构建分页拦
        $scope.pageLabel = [];
        var firstPage = 1;//开始页码
        var lastPage = $scope.resultMap.totalPages;//截止页
        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后面有点
        if ($scope.resultMap.totalPages>5){//如果页码数大于5
            if ($scope.searchMap.pageNo<=3){//显示前5页
                lastPage = 5;
                $scope.firstDot = false;
            }else if ($scope.searchMap.pageNo>=$scope.resultMap.totalPages-2){//显示后5页
                firstPage = $scope.resultMap.totalPages-4;
                $scope.lastDot = false;
            }else {
                firstPage = $scope.searchMap.pageNo-2;
                lastPage = $scope.searchMap.pageNo+2;
            }
        }else {
            $scope.firstDot = false;//前面无点
            $scope.lastDot = false;//后面无点
        }
        for (var i = firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }
	$scope.addSearchItem = function (key,value) {
        if (key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key] = value;
        }else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//查询
    }
//撤销搜索项
    $scope.removeSearchItem=function(key){
        if(key=='category' || key=='brand' || key=='price'){//如果用户点击的是分类或品牌
            $scope.searchMap[key]="";
        }else{//用户点击的是规格
            delete $scope.searchMap.spec[key];
        }
        $scope.search();//查询
    }

    //分页查询
    $scope.queryByPage = function (pageNo) {
	    if (pageNo<1||pageNo>$scope.resultMap.totalPages){
	        return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //判断当前页是否是第一页
    $scope.isToPage = function () {
        if ($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }

    //判断当前页是否是最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    }
    //排序查询
    $scope.sortSearch = function (sort,sortField) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        $scope.search();//查询
    }

    //判断关键字是否包含品牌
    $scope.keywordsIsBrand = function () {
	    for (var i=0;i<$scope.resultMap.brandList.length;i++){
           if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    //加载portal工程中的index.html中传递过来的参数
    $scope.loadKeywords = function () {//这个方法在页面加载的时候就调用使用ng-init
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();//查询
    }

	/*//添加搜索项  改变searchMap的值
	$scope.addSearchItem=function(key,value){
		
		if(key=='category' || key=='brand'){//如果用户点击的是分类或品牌
			$scope.searchMap[key]=value;
			
		}else{//用户点击的是规格
			$scope.searchMap.spec[key]=value;		
		}
		$scope.search();//查询
	}
	
	//撤销搜索项
	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand'){//如果用户点击的是分类或品牌
			$scope.searchMap[key]="";
		}else{//用户点击的是规格
			delete $scope.searchMap.spec[key];		
		}
		$scope.search();//查询
	}
	*/
	
});