package u.cando.restapi.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Request Parameter 유틸리티.
 * 
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Slf4j
public class RequestParamUtil
{
	/**
	 * MultiMap 형태의 파라미터를 HashMap 형태로 변환.
	 * 
	 * @param params
	 *            MultiMap 형태의 파라미터
	 * @return Map 형태의 결과
	 */
	public static Map<String, String> convert2HashMap(MultiMap params)
	{
		Map<String, String> result = new HashMap<>();

		for (Entry<String, String> oneEntry : params.entries())
		{
			result.put(oneEntry.getKey(), oneEntry.getValue());
		}

		return result;
	}

	/**
	 * Request 객체를 맵형태로 변환
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, Object> convertRequest2Map(Object request)
	{
		Map<String, Object> params = new HashMap<>();

		try
		{
			params = PropertyUtils.describe(request);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
		{
			log.error("파라미터 변환을 하지 못했습니다: {}", e.getMessage());
		}

		return params;
	}

	private static MultiMap fetchParameter(MultiMap params)
	{
		if (params == null)
		{
			return MultiMap.caseInsensitiveMultiMap();
		}

		return params;
	}

	/**
	 * 호출 URL 정보를 이용하여 캐시키 생성. <br/>
	 * 단, requestId는 호출시마다 변경될 수 있어 캐시키를 만들때 제거한다.
	 * 
	 * @param routingContext
	 *            컨텍스트
	 * @return 캐시키
	 */
	public static String generateIndetifier(RoutingContext routingContext)
	{
		StringBuilder sb = new StringBuilder();

		if (routingContext.request().method().toString().equalsIgnoreCase("GET"))
		{
			sb.append(routingContext.request().uri());
		} else
		{
			sb.append(routingContext.request().path());
			sb.append("?");
			sb.append(routingContext.request().query());
		}

		String tempId = sb.toString();

		return RegExUtils.removeAll(tempId, "requestId=.{0,37}&");
	}

	/**
	 * MultiMap 형태의 파라미터를 Json String 형태로 변환.
	 * 
	 * @param params
	 *            MultiMap 형태의 파라미터
	 * @return Json 형태로 변환된 값
	 */
	public static String getJsonStringFromMultiMapValue(MultiMap params)
	{
		String paramJsonValue = null;

		if (params != null)
		{
			JsonObject result = new JsonObject();

			for (Entry<String, String> oneEntry : params.entries())
			{
				result.put(oneEntry.getKey(), oneEntry.getValue());
			}

			paramJsonValue = result.toString();
		}

		return paramJsonValue;
	}

	/**
	 * Json 형태의 파라미터를 MultiMap으로 변환.
	 * 
	 * @param jsonValue
	 *            JSON 형태의 파라미터 값
	 * @return MultiMap 형태의 파라미터 값
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getParametersFromJsonString(String jsonValue)
	{
		Gson gson = new Gson();

		Map<String, String> params = new HashMap<>();

		try
		{
			params = gson.fromJson(jsonValue, HashMap.class);
		} catch (JsonSyntaxException e)
		{
			log.error(e.getMessage());
		}

		return params;
	}

	/**
	 * POST, PUT, PATCH 방식으로 전송할 경우 form data 로 넘어오는 경우가 있어 확인 후 처리 필요.
	 * 
	 * @param routingContext
	 * @return
	 */
	public static MultiMap getValidParameters(RoutingContext routingContext)
	{
		MultiMap params = fetchParameter(routingContext.request().params());

		if (!routingContext.request().method().name().equalsIgnoreCase("GET"))
		{
			String bodyString = routingContext.getBodyAsString("UTF-8");

			log.debug("Body String: {}", bodyString);

			if (params == null)
			{
				params = MultiMap.caseInsensitiveMultiMap();
			}

			HttpServerRequest request = routingContext.request();

			MultiMap formMap = request.formAttributes();

			for (Entry<String, String> oneEntry : formMap.entries())
			{
				params.add(oneEntry.getKey(), oneEntry.getValue());
			}

			if (StringUtils.isNotBlank(bodyString))
			{
				try
				{
					@SuppressWarnings("unchecked")
					Map<String, String> bodyMap = new GsonBuilder().setLenient().create().fromJson(bodyString, HashMap.class);

					if (bodyMap != null && !bodyMap.isEmpty())
					{
						for (Entry<String, String> oneEntry : bodyMap.entrySet())
						{
							params.add(oneEntry.getKey(), oneEntry.getValue());
						}
					}
				} catch (JsonSyntaxException e)
				{
					// JSON 형식이 아닐경우 해당내용 저장
					params.add("body", bodyString);
				}
			}
		}

		return params;
	}

	private RequestParamUtil()
	{
		super();
	}
}