# 根据单位ID获取单位详情

## 基本信息
- **接口名称**：根据单位ID获取单位详情
- **接口描述**：根据单位ID获取单位详情
- **请求方法**：GET
- **完整请求路径**：`https://api_server_url/unit/info`

## 请求参数

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | 示例值 |
|----------|----------|----------|----------|----------|--------|
| accessToken | 接口访问凭证 | query | true | string | AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO |
| unitId | 单位ID | query | true | integer(int64) | 123 |

## 成功响应参数结构

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| code | 成功或异常编码 | integer(int32) |
| status | 旧接口成功、失败或异常辅助判断标记 | string |
| message | 成功或异常消息 | string |
| data | 成功或异常数据 | UnitInfoVO |

**UnitInfoVO 对象结构：**

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| id | 学校基本数据子类表ID | integer(int64) |
| orgId | 学校的组织机构id | integer(int64) |
| dwdm | 单位代码 | string |
| dwmc | 单位名称 | string |
| dwywmc | 单位英文名称 | string |
| dwdz | 单位地址 | string |
| dwyzbm | 单位邮政编码 | string |
| dwzgbmm | 单位主管部门码 | string |
| dwjj | 单位简介 | string |
| tyshxydm | 统一社会信用代码 | string |
| xzqhm | 行政区划码 | string |

## 成功响应示例
```json
{
    "code": 200,
    "status": "success",
    "message": "请求成功",
    "data": {
        "id": "1599695560903458818",
        "orgId": "1599695561170583553",
        "dwdm": "10010001",
        "dwmc": "单位一",
        "dwywmc": "",
        "dwdz": "14564444444444444",
        "dwyzbm": "",
        "dwzgbmm": "",
        "dwjj": "",
        "tyshxydm": "14564444444444444",
        "xzqhm": ""
    }
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

## 特殊状态码说明
| 状态码 | 说明 |
|--------|------|
| 200 | OK |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 其他状态码 | 参见全局状态码 |

## 业务调用注意事项
- `unitId` 为必填参数，必须提供有效的单位ID
- 需要确保 `accessToken` 有效且有权限访问该单位信息

## 调用示例

**NodeJs - Axios**
```javascript
var axios = require('axios');
var config = {
  method: 'get',
  url: 'https://api_server_url/unit/info?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&unitId=123',
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
  .url("https://api_server_url/unit/info?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&unitId=123")
  .method("GET", null)
  .addHeader("X-App-Id", "1")
  .build();
Response response = client.newCall(request).execute();
```
