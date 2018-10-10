app.controller("brandController",function ($scope,$http,brandService,$controller) {
    /*这种继承实际它是一种伪继承，这里就是让baseController中的$scope=当前的$scope*/
    $controller("baseController",{$scope:$scope});//继承

    //查询【品牌列表】
    /*$scope.finAll = function () {
        brandService.findAll().success(
            function (responce) {
                $scope.list = responce;
            }
        );
    }*/


    //写一个方法调用后台的代码
    $scope.findPage=function(page,size){
        $http.get('../brand/findPage.do?pageNum='+page +'&pageSize='+size).success(
            function(response){
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }
    //增加
    $scope.save  = function () {
        var object = null;
        if ($scope.entity.id !=null){
            object = brandService.update($scope.entity);
        }else{
            object = brandService.add($scope.entity);
        }
        object.success(
            function (response) {
                if (response.success){
                    $scope.reloadList();//刷新页面
                    alert(response.message);
                }else {
                    alert(response.message);
                }
            }
        )
    }

    //查询实体，把数据回显到表格中
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }



    //删除
    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if (response.success){
                    $scope.reloadList();//刷新页面
                }else {
                    alert(response.message);
                }

            }
        );
    }
    //条件查询,response.rows它返回的是集合对象，是根据从数据库查询出来封装到pojo类中的PageResult这个类中的，所以它是一个集合，拿到页面中直接遍历就好，不用转化成json对象了，也只有调用玩以后是个字符串的时候，我们才转成json对象进行遍历，获取里面的值

    $scope.searchEntity = {};
    $scope.search = function (page,size) {
        brandService.search(page,size,$scope.searchEntity).success(
            function(response){
               $scope.t=response;//显示当前页数据
                //$scope.t.rows1111111 = $scope.t.rows1;
               // $scope.list = JSON.parse( response.rows);
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }
});