app.controller('seckillGoodsController',function ($scope,$location,seckillGoodsService,$interval) {
    //读取列表数据绑定到表单中
    $scope.findList = function () {
        seckillGoodsService.findList().success(
            function (response) {

                $scope.list = response;
                alert(JSON.stringify($scope.list ));
            }
        );
    }
//查询秒杀商品到详情页
    $scope.findOne = function () {
        var id = $location.search()['id'];
        seckillGoodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //实现倒计时，纯前端的事情
                //获取从结束时间到当前日期的秒数
                allSecond = Math.floor((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);
                time = $interval(function () {
                    allSecond = allSecond-1;
                    $scope.timeString = converTimeString(allSecond);
                    if (allSecond<=0){
                        $interval.cancel(time);
                    }
                },1000);
            }
        );
    }
//转换秒为   天小时分钟秒格式  XXX天 10:22:33
    converTimeString = function (allSecond) {
        var days= Math.floor( allSecond/(60*60*24));//天数
        var hours= Math.floor( (allSecond-days*60*60*24)/(60*60) );//小数数
        var minutes= Math.floor(  (allSecond -days*60*60*24 - hours*60*60)/60    );//分钟数
        var seconds= allSecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
        var timeString="";
        if(days>0){
            timeString=days+"天 ";
        }
        return timeString+hours+":"+minutes+":"+seconds;
    }

    //提交订单
    $scope.submitOrder=function(){
        seckillGoodsService.submitOrder( $scope.entity.id ).success(
            function(response){
                if(response.success){//如果下单成功
                    alert("抢购成功，请在5分钟之内完成支付");
                    location.href="pay.html";//跳转到支付页面
                }else{
                    alert(response.message);
                }
            }
        );
    }
})