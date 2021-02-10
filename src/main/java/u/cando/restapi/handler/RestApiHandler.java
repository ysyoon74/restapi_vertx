package u.cando.restapi.handler;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import com.google.common.collect.ImmutableList;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import u.cando.restapi.controller.Controller;
import u.cando.restapi.dto.RestApiError;
import u.cando.restapi.util.RequestParamUtil;

/**
 * @Author 윤용승
 * @Since: 2021. 2. 10.
 */
@Slf4j
public class RestApiHandler implements Handler<RoutingContext>
{
	private JsonObject config;

	@Controller
	private Object controller;
	
	private String uri;

	/**
	 * @param controller
	 * @param uri
	 */
	public RestApiHandler(Object controller, String uri)
	{
		super();

		this.controller = controller;
		this.uri = uri;
	}

	public JsonObject getConfig()
	{
		return config;
	}

	/**
	 * URI 정보를 가지고 다음과 같은 형태로 변환한다.
	 * 
	 * <pre>
	 * 입력 URI : /api/search/one?doc_id=1 METHOD가 GET 이라면 doGETOne 이 메소드명이 된다.
	 * </pre>
	 * 
	 * @param routingContext
	 *            컨텍스트
	 * @param uri
	 *            부모 URI
	 * @return 서비스 메소드 명
	 */
	protected String getServiceMethodName(RoutingContext routingContext, String uri)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("do");
		sb.append(routingContext.request().method().toString());

		if (!uri.endsWith("/"))
		{
			uri += "/";
		}

		sb.append(WordUtils.capitalize(routingContext.request().path().replaceAll(uri, ""), '/').replace("/", ""));

		return sb.toString();
	}

	@Override
	public void handle(RoutingContext routingContext)
	{
		if (!validateParameter(routingContext))
		{
			routingContext.currentRoute().produces("application/json");

			routingContext.response().end(Json.encodePrettily(
					new RestApiError(routingContext.response().getStatusCode(), routingContext.response().getStatusMessage())));

			return;
		}

		String methodName = getServiceMethodName(routingContext, uri);

		routingContext.currentRoute().produces("application/json");

		Object result = null;

		Method[] allMethods = controller.getClass().getDeclaredMethods();

		boolean errorExists = false;

		String errorMessage = null;

		for (Method oneMethod : allMethods)
		{
			if (oneMethod.getName().equals(methodName))
			{
				try
				{
					@SuppressWarnings("rawtypes")
					Class[] parameterTypes = oneMethod.getParameterTypes();

					if (parameterTypes.length == 0)
					{
						result = oneMethod.invoke(controller);
					} else
					{
						log.debug("Method Name: {} {}", oneMethod.getName(), routingContext.request().params().getClass().getName());

						result = oneMethod.invoke(controller, routingContext);
					}
				} catch (Exception e)
				{
					log.error(e.getMessage());

					errorMessage = e.getMessage();

					errorExists = true;
				}
			}
		}

		if (errorExists)
		{
			routingContext.response().setStatusCode(500)
					.end(Json.encodePrettily(new RestApiError(500, StringUtils.defaultString(errorMessage, "해당 메소드가 없습니다."))));

			return;
		}

		routingContext.put("result", Json.encodePrettily(result));

		routingContext.next();
	}

	public void setConfig(JsonObject config)
	{
		this.config = config;
	}

	/**
	 * 파리미터 유효성 검사.(swagger.json 파일 기준)
	 * 
	 * @param routingContext
	 *            컨텍스트
	 * @return 파라미터 유효성 결과
	 */
	protected boolean validateParameter(RoutingContext routingContext)
	{
		MultiMap params = RequestParamUtil.getValidParameters(routingContext);

		JsonObject swagger = routingContext.get("swagger");

		JsonArray parameters = null;

		try
		{
			parameters = swagger.getJsonObject("paths").getJsonObject(routingContext.request().path())
					.getJsonObject(routingContext.request().method().toString().toLowerCase()).getJsonArray("parameters");
		} catch (Exception e)
		{
			log.error(e.getMessage());

			routingContext.response().setStatusCode(500).setStatusMessage("경로를 확인해주세요.");

			return false;
		}

		if (parameters == null)
		{
			return true;
		}

		List<?> paramsList = ImmutableList.copyOf(parameters.iterator());

		JsonObject oneParam = null;

		boolean validated = true;

		for (Object oneOjb : paramsList)
		{
			oneParam = (JsonObject) oneOjb;

			if (Boolean.TRUE.equals(oneParam.getBoolean("required")) && params.get(oneParam.getString("name")) == null)
			{
				validated = false;

				log.error("파라미터 {} 은/는 필수입니다.", oneParam.getString("name"));

				routingContext.response().setStatusCode(500).setStatusMessage(oneParam.getString("name") + " 파라미터는 필수 입니다.");

				break;
			}
		}

		return validated;
	}
}