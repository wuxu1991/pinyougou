app.controller("baseController",function ($scope) {
    //分页控件配置currentPage:当前页   totalItems :总记录数  itemsPerPage:每页记录数  perPageOptions :分页选项  onChange:当页码变更后自动触发的方法
    $scope.paginationConf = {
        currentPage: 1,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();
        }
    };
    $scope.reloadList = function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

//删除品牌
    $scope.selectIds = [];

    $scope.updateSelection = function ($event,id) {
        //判断添加id
        if ($event.target.checked){
            $scope.selectIds.push(id);
        }else {
            var index = $scope.selectIds.indexOf(id);//查找id的位置index
            $scope.selectIds.splice(index,1);
        }
    }

    $scope.jsonToString = function(jsonString,key){
        //把json字符串转化成对象或者集合
        var json = JSON.parse(jsonString);
        var value = "";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=",";
            }
            value+=json[i][key]
        }

        return value;
    }
    //新增商品中的规格具体实现的思路
    /*定义一个集合：entity.goodsDesc.specificationItems=[];
                               entity.goodsDesc.specificationItems=[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]}];
         两种情况：
         1.选择的选择，规格名称不存在
         2.选择的选择，规格名称已存在
    *   判断集合中attributeName对应的值"网络制式"有没有当前我要添加的选项的规格名称
    *   写一个通用的方法来实现查询集合中元素是否有？
    * */
    $scope.searchObjectByKey = function (list,key,keyValue) {
        for (var i=0;i<list.length;i++){
            if (list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }
})