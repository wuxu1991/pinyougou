app.service("uploadService",function ($http) {

   /* //上传文件
    this.uploadFile = function () {
        //html5之后的上传文件的方法
        var formdata = new FormData();
        formdata.append('file',file.files[0]);//file 文件上传框的name
        return $http({
                url:'../upload.do',
                method:'post',
                data:formdata,
                //当我们上传文件的时候，必须指定这个undefined类型，它默认指定的类型是json类型
                //如果我们指定undefined
                headers:{'Content-Type':undefined},
                //表示对整个表单进行二进制序列化的
                transformRequest:angular.identity
            });
    }*/

        //上传文件
        this.uploadFile=function(){
            var formdata=new FormData();
            formdata.append('file',file.files[0]);//file 文件上传框的name

            return $http({
                url:'../upload.do',
                method:'post',
                data:formdata,
                headers:{ 'Content-Type':undefined },
                transformRequest: angular.identity
            });

        }

})