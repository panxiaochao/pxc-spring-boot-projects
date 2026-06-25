# 根据班级组织机构ID获取学生列表

## 基本信息
- **接口名称**：根据班级组织机构ID获取学生列表
- **接口描述**：根据班级组织机构ID获取班级下的学生列表
- **请求方法**：GET
- **完整请求路径**：`https://jyjzhfw.qiantang.gov.cn/tyba/open-api/student/class/list`

## 请求参数

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | 示例值 |
|----------|----------|----------|----------|----------|--------|
| accessToken | 接口访问凭证 | query | true | string | AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO |
| classOrgId | 班级的组织机构id | query | true | integer(int64) | 50001 |

## 成功响应参数结构

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| code | 成功或异常编码 | integer(int32) |
| status | 旧接口成功、失败或异常辅助判断标记 | string |
| message | 成功或异常消息 | string |
| data | 成功或异常数据 | array[StudentVO] |

**StudentVO 对象结构：**

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| id | 学生基本数据子类表ID | integer(int64) |
| userId | 学生的用户ID | integer(int64) |
| xm | 姓名 | string |
| xbm | 性别码 | string |
| xbmc | 性别名称 | string |
| xh | 学号 | string |
| xjh | 学籍号 | string |
| rxny | 入学年月 | string |
| xsdqztm | 学生当前状态码 | string |
| xsdqztmc | 学生当前状态名称 | string |
| sjzk | 生籍情况 | integer(int32) |
| sjzkmc | 生籍情况名称 | string |
| lastSequence | 上一次去数据后台返回给客户的Seq，初次拉取时为0 | integer(int64) |

## 成功响应示例
```json
{
    "code": 200,
    "status": "success",
    "message": "请求成功",
    "data": [
        {
            "id": "30001",
            "userId": "40001",
            "xm": "张三",
            "xbm": "1",
            "xbmc": "男",
            "xh": "20240001",
            "xjh": "G33010220240001",
            "rxny": "202409",
            "xsdqztm": "01",
            "xsdqztmc": "在读",
            "sjzk": 1,
            "sjzkmc": "有学籍",
            "lastSequence": null
        },
        {
            "id": "30002",
            "userId": "40002",
            "xm": "李四",
            "xbm": "2",
            "xbmc": "女",
            "xh": "20240002",
            "xjh": "G33010220240002",
            "rxny": "202409",
            "xsdqztm": "01",
            "xsdqztmc": "在读",
            "sjzk": 1,
            "sjzkmc": "有学籍",
            "lastSequence": null
        }
    ]
}
```

## 无数据时返回
```json
{
    "code": 200,
    "status": "success",
    "message": "请求成功",
    "data": []
}
```

## 失败响应示例
```json
{
    "code": 500,
    "data": {},
    "message": "成功或异常消息",
    "status": "fail"
}
```

## 业务调用注意事项
- `classOrgId` 参数为必填，需提供有效的班级组织机构ID

## 调用示例

**NodeJs - Axios**
```javascript
var axios = require('axios');
var config = {
  method: 'get',
  url: 'https://jyjzhfw.qiantang.gov.cn/tyba/open-api/student/class/list?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&classOrgId=50001',
  headers: {
    'X-App-Id': '1'
  }
};
axios(config)
.then(function (response) {
  console.log(JSON.stringify(response.data));
})
.catch(function (error) {
  console.log(error);
});
```

**Java - OkHttp**
```java
OkHttpClient client = new OkHttpClient().newBuilder()
  .build();
Request request = new Request.Builder()
  .url("https://jyjzhfw.qiantang.gov.cn/tyba/open-api/student/class/list?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&classOrgId=50001")
  .method("GET", null)
  .addHeader("X-App-Id", "1")
  .build();
Response response = client.newCall(request).execute();
```
