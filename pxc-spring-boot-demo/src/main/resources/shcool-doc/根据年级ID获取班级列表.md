# 根据年级ID获取班级列表

## 基本信息
- **接口名称**：根据年级ID获取班级列表
- **接口描述**：根据年级ID获取年级下的班级列表
- **请求方法**：GET
- **完整请求路径**：`https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/class/list`

## 请求参数

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | 示例值 |
|----------|----------|----------|----------|----------|--------|
| accessToken | 接口访问凭证 | query | true | string | AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO |
| gradeId | 年级ID | query | true | integer(int64) | 1001 |

## 成功响应参数结构

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| code | 成功或异常编码 | integer(int32) |
| status | 旧接口成功、失败或异常辅助判断标记 | string |
| message | 成功或异常消息 | string |
| data | 成功或异常数据 | array[ClassListVO] |

**ClassListVO 对象结构：**

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| id | 班级数据子类表ID | integer(int64) |
| orgId | 班级的组织机构id | integer(int64) |
| bjdm | 班级代码 | string |
| bj | 班号 | string |
| bjmc | 班级名称 | string |
| njdm | 年级代码 | string |

## 成功响应示例
```json
{
    "code": 200,
    "status": "success",
    "message": "请求成功",
    "data": [
        {
            "id": "10001",
            "orgId": "20001",
            "bjdm": "1000101",
            "bj": "1",
            "bjmc": "1班",
            "njdm": "21"
        },
        {
            "id": "10002",
            "orgId": "20002",
            "bjdm": "1000102",
            "bj": "2",
            "bjmc": "2班",
            "njdm": "21"
        }
    ]
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
- `gradeId` 为必填参数，必须提供有效的年级ID
- 需要确保 `accessToken` 有效且有权限访问该年级的班级信息

## 调用示例

**NodeJs - Axios**
```javascript
var axios = require('axios');
var config = {
  method: 'get',
  url: 'https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/class/list?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&gradeId=1001',
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
  .url("https://jyjzhfw.qiantang.gov.cn/tyba/open-api/school/campus/grade/class/list?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&gradeId=1001")
  .method("GET", null)
  .addHeader("X-App-Id", "1")
  .build();
Response response = client.newCall(request).execute();
```
