# 根据学校ID获取学校详情

## 基本信息
- **接口名称**：根据学校ID获取学校详情
- **接口描述**：根据学校ID获取学校详情
- **请求方法**：GET
- **完整请求路径**：`https://api_server_url/school/info`

## 请求参数

| 参数名称 | 参数说明 | 请求类型 | 是否必须 | 数据类型 | 示例值 |
|----------|----------|----------|----------|----------|--------|
| accessToken | 接口访问凭证 | query | true | string | AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO |
| schoolId | 学校ID | query | true | integer(int64) | 1158 |

## 成功响应参数结构

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| code | 成功或异常编码 | integer(int32) |
| status | 旧接口成功、失败或异常辅助判断标记 | string |
| message | 成功或异常消息 | string |
| data | 成功或异常数据 | SchoolInfoVO |

**SchoolInfoVO 对象结构：**

| 参数名称 | 参数说明 | 类型 |
|----------|----------|------|
| id | 学校基本数据子类表ID | integer(int64) |
| orgId | 学校的组织机构id | integer(int64) |
| xxbsm | 学校标识码 | string |
| xxbxlxm | 办学类型码 | string |
| xxdm | 学校代码 | string |
| xxmc | 学校名称 | string |
| xxdz | 学校地址 | string |
| xxjc | 学校简称 | string |
| xxjj | 学校简介 | string |
| xxxzm | 学校性质 | string |
| xxzt | 学校状态 | string |
| jxny | 建校年月 | string |
| lxdh | 联系电话 | string |
| dzxx | 电子信箱 | string |
| tyshxydm | 统一社会信用代码 | string |
| xxyzbm | 学校邮政编码 | string |
| xxzgbmm | 学校主管部门码 | string |
| xzqhm | 行政区划码 | string |
| zydz | 主页地址 | string |

## 成功响应示例
```json
{
    "code": 200,
    "status": "success",
    "message": "请求成功",
    "data": {
        "id": "1158",
        "orgId": "207",
        "xxbsm": "1000018005",
        "xxbxlxm": "111",
        "xxdm": "1000018005",
        "xxmc": "杭钢集团公司幼儿园",
        "xxdz": "杭州市拱墅区半山街道南苑社区36号",
        "xxjc": "",
        "xxjj": "杭钢幼儿园创办于1958年，是一所省属企业园，园内环境优美，阳光充足，师资雄厚。占地面积2285平方米，建筑面积3875平方米，现有11个班级，327名幼儿，全园教职工47人，园长1人（副高级），专任教师33人（副高级1人，中级职称14人，100%具有大专以上学历及教师资格证）。作为一所企业幼儿园，杭钢幼儿园以"幸福教育，完美启点"为办园指导思想，努力培养幼儿成为让自己幸福，也能带给别人幸福的人。坚持依托周边资源开发课程，逐步形成幼儿园山野探究园本特色，围绕"山野、自然、生长"的教育理念，逐步形成"亲自然、真体验；乐探究、爱思考；敢挑战、品质优"的良好素养，为幼儿终身学习生活奠定基础。",
        "xxxzm": "1",
        "xxzt": "0",
        "jxny": "",
        "lxdh": "13957111616",
        "dzxx": "",
        "tyshxydm": "913301001430989305",
        "xxyzbm": "",
        "xxzgbmm": "",
        "xzqhm": "",
        "zydz": ""
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
- `schoolId` 为必填参数，必须提供有效的学校ID
- 需要确保 `accessToken` 有效且有权限访问该学校信息

## 调用示例

**NodeJs - Axios**
```javascript
var axios = require('axios');
var config = {
  method: 'get',
  url: 'https://api_server_url/school/info?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&schoolId=1158',
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
  .url("https://api_server_url/school/info?accessToken=AT-10-R6Bl-dFf8itd6BM-pdSMzKRTR-kTmlpO&schoolId=1158")
  .method("GET", null)
  .addHeader("X-App-Id", "1")
  .build();
Response response = client.newCall(request).execute();
```
