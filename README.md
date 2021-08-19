# restapi_vertx
Vertx(https://vertx.io/) 기반 RestAPI

## 주요 기능
- Clustering & Cache (use Hazelcast)
- Swagger

## 실행방법
- Windows
  ```
  # service.bat
  ```
- *Nix
  ```
  $ ./service.sh start
  ```
- Docker
  ```
  $ docker pull ysyoon74/ubuntu1804_openjdk8
  $ docker build -t 이미지명:태그 .
  ```
```bash
# 실행 URL
http://127.0.0.1:8080/assets/index.html
```

## 개발 순서
1. Controller 구현
   - xxx.xxx.controller 패키지 아래에 작성(권장)
   
   ```java
package com.saltlux.restapi.controller;

import com.saltlux.restapi.dto.Response;

/**
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Controller(version = "Simple API v20210209") // 버전 정보 입력
public class SimpleController extends AbstractRestApiController
{
	@Override
	public Response doGETVersion() // 패턴: do + GET|POST|PUT|DELETE + url
	{
		String version = getVersion();

		Response response = new Response();

		response.setReturnObject(version);
		response.setReturnType(version.getClass().getName());

		return response;   // 결과는 반드시 Response 객체의 ReturnObject로 추가해야 함
	}
}   
   ```
   
   
2. Service 구현
   - xxx.xxx.service 패키지 아래에 작성(권장)
   
3. swagger.json 추가

```json
...
	"tags": [
		{
			"name": "Dictionary Data Replacepattern API",
			"description": "대체어 규칙 사전 관련 API 목록"
		},
		// 위와 같은 형식으로 추가(컨트롤러 최초 등록시 1회)
	],
...

		"/api/dic/data/stopwords/remove": {
			"delete": {                     // 호출 방식을 정의합니다. get, post, put, delete
				"summary": "불용어 사전에서 삭제",
				"description": "불용어 사전에서 삭제합니다.",
				"consumes": [
					"application/x-www-form-urlencoded"
				],
				"produces": [
					"application/json",
					"text/html"
				],
				"parameters": [
					{
						"name": "requestId",
						"in": "query",
						"description": "요청 고유키.",
						"required": false,   // true인 경우 api 내부에서 검증합니다.
						"type": "string",
						"format": ""
					},
					{
						"name": "seq",
						"in": "query",
						"description": "삭제할 대상 아이디.",
						"required": false,
						"type": "string",
						"format": "",
						"example": ""
					},
					{
						"name": "target_db",
						"in": "query",
						"description": "대상 DB.",
						"required": false,
						"type": "string",
						"format": "",
						"example": "maria_op"
					}
				],
				"tags": [
					"Dictionary Data Stopwords API"
				],
				"responses": {
					"200": {
						"description": "Dictionary Data Stopwords API 버전 정보",
						"schema": {
							"$ref": "#/definitions/Response"
						}
					}
				}
			}
		}
```
4. service.yml 추가

```yaml
services:
- blockable: false   // 만약 시간이 초단위로 걸리는 경우 반드시 true로 해야 합니다.
  serviceUrl: "/api/simple"
  targetHandler: "com.saltlux.restapi.controller.SimpleController"
```
5. FAQ
    - 포트를 변경하고 싶은 경우 : restapi-conf.json 파일 수정
