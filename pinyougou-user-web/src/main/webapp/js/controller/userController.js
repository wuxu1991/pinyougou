 //控制层 
app.controller('userController' ,function($scope,$controller   ,userService){	
	//注册用户
    $scope.reg = function () {
        //比较两次密码输入是否一致
        if ($scope.password!=$scope.entity.password){
            alert("两次密码输入不一致，请重新输入");
            $scope.entity.password="";
            $scope.password="";
            return;
        }
        //新增
        userService.add($scope.entity,$scope.smscode).success(
            function (response) {
                alert(response.message);
            }
        );
    }
    //发送验证码
    $scope.sendCode = function () {
        //判断手机号是否为空
        if ($scope.entity.phone==null||$scope.entity.phone==""){
            alert("手机号不能空");
            return;
        }
        userService.sendCode($scope.entity.phone).success(
            function (response) {
                alert(response.message);
            }
        );
    }
});	
