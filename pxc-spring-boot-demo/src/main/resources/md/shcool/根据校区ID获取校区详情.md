# 根据校区ID获取校区详情

## 基本信息
- **接口名称**：根据校区ID获取校区详情
- **接口描述**：根据校区ID获取校区详情
- **请求方法**：GET
- **完整请求路径**：`https://api_server_url/school/campus/info`

## 请求参数

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | 示例值 |
|----------|----------|----------|----------|----------|--------|
| accessToken | 接口访问凭证 | query | true | string | AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO |
| campusId | 校区ID | query | true | integer(int64) | 111 |

## 成功响应参数结构

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| code | 成功或异常编码 | integer(int32) |
| status | 旧接口成功、失败或异常辅助判断标记 | string |
| message | 成功或异常消息 | string |
| data | 成功或异常数据 | CampusInfoVO |

**CampusInfoVO 对象结构：**

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| id | 校区基本数据子类表ID | integer(int64) |
| orgId | 校区的组织机构id | integer(int64) |
| xqdm | 校区代码 | string |
| xqmc | 校区名称 | string |
| xqm | 校区码 | string |
| xqzt | 校区状态 | string |
| sfxnxq | 是否虚拟校区（0：否，1：虚拟校区） | integer(int32) |
| sfzxqm | 是否主校区码（0：主校，1：分校） | integer(int32) |
| xxdm | 学校代码 | string |
| xxmc | 学校名称 | string |
| szzqzxs | 省（自治区、直辖市） | string |
| dsz | 地（市、州） | string |
| qqq | 县（区、旗） | string |
| xz | 乡（镇） | string |
| sq | 社区 | string |
| x | 经度 | string |
| y | 纬度 | string |
| xqdz | 校区地址 | string |
| jxny | 建校年月 | string |
| ghgmBj | 规划规模（班级数量） | integer(int32) |
| xzqhm | 行政区划码集合 | string |

## 成功响应示例
```json
{
    "code": 200,
    "status": "success",
    "message": "请求成功",
    "data": {
        "id": "554939880513956601",
        "orgId": "2577",
        "xqdm": "1000018005001",
        "xqmc": "钢铁集团有限公司幼儿园",
        "xqm": "",
        "xqzt": "0",
        "sfxnxq": 0,
        "sfzxqm": 1,
        "xxdm": "1000018005",
        "xxmc": "杭钢集团公司幼儿园",
        "szzqzxs": "330000000000",
        "dsz": "330100000000",
        "qqq": "330105000000",
        "xz": "330105011000",
        "sq": "",
        "x": "",
        "y": "30.348296",
        "xqdz": "杭州市拱墅区半山街道南苑社区36号",
        "jxny": "",
        "ghgmBj": 11,
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

## 业务调用注意事项
- `campusId` 为必填参数，必须提供有效的校区ID
- 需要确保 `accessToken` 有效且有权限访问该校区信息

## 调用示例

**NodeJs - Axios**
```javascript
var axios = require('axios');
var config = {
  method: 'get',
  url: 'https://api_server_url/school/campus/info?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&campusId=111',
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
  .url("https://api_server_url/school/campus/info?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&campusId=111")
  .method("GET", null)
  .addHeader("X-App-Id", "1")
  .build();
Response response = client.newCall(request).execute();
```
