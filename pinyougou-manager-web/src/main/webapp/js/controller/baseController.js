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
    $scope.parentId1=0;
    $scope.updateSelectionDele = function ($event,id,parentId) {
        //判断添加id
        if ($event.target.checked){
            $scope.selectIds.push(id);
            $scope.parentId1 = parentId;
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
})