package u.cando.restapi.handler;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import u.cando.restapi.RestApiConstants;
import u.cando.restapi.dto.Response;
import u.cando.restapi.util.RequestParamUtil;

/**
 * @Author 윤용승
 * @Since: 2021. 2. 10.
 */
@Slf4j
public class RestApiAfterHandler implements Handler<RoutingContext>
{
	private final JsonObject config;

	private static final String REQUEST_PARAM_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	private static final String ALLOW_METHODS = "GET,POST,OPTION";

	private static final String ALLOW_HEADERS = "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization";

	private static final String REQUEST_PARAM_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	private static final String REQUEST_PARAM_ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	private static final String REQUEST_PARAM_ACCESS_CONTROL_ALLOW_HEADES = "Access-Control-Allow-Headers";

	public RestApiAfterHandler(JsonObject config)
	{
		this.config = config;
	}

	@Override
	public void handle(RoutingContext routingContext)
	{
		String identifier = RequestParamUtil.generateIndetifier(routingContext);

		HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(RestApiConstants.CACHE_INSTANCE_NAME);

		Object result = null;

		if (Boolean.TRUE.equals(config.getBoolean(RestApiConstants.CONFIG_USE_CACHE, true)) && instance != null)
		{
			Map<String, String> cachedMap = instance.getMap(RestApiConstants.CACHE_MAP_NAME);

			result = cachedMap.get(identifier);
		}

		if (result == null)
		{
			result = routingContext.get(RestApiConstants.RESPONE_RESULT_NAME);

			if (instance != null)
			{
				Map<String, String> cachedMap = instance.getMap(RestApiConstants.CACHE_MAP_NAME);

				cachedMap.put(identifier, routingContext.get(RestApiConstants.RESPONE_RESULT_NAME));

				log.debug("Cache({}) is added.", identifier);
			}

		}

		if (result != null)
		{
			Gson gson = new GsonBuilder().create();

			Response response = gson.fromJson((String) result, Response.class);

			HttpServerRequest request = routingContext.request();

			response.setRequestId(routingContext.session().id());
			response.setRemoteAddress(request.remoteAddress().host());

			result = Json.encodePrettily(response);

			if (BooleanUtils.toBoolean(response.getUseJsonp()) && StringUtils.isNotBlank(response.getCallback()))
			{
				result = response.getCallback() + "( " + result + " )";
			}
		} else
		{
			log.error("Result가 Null입니다. 컨트롤러가 있는지 확인해주세요.");
		}

		if (Boolean.TRUE.equals(config.getBoolean(RestApiConstants.CONFIG_DEV_MODE, false)))
		{
			if (routingContext.response().headers().get("content-type") == null)
			{
				log.warn("content type is null. set default to application/json");

				routingContext.response().putHeader("Content-Type", "application/json")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_ORIGIN, "*")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_METHODS, ALLOW_METHODS)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_MAX_AGE, "3600")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_HEADES, ALLOW_HEADERS).end((String) result);
			} else
			{
				routingContext.response().putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_ORIGIN, "*")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_METHODS, ALLOW_METHODS)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_MAX_AGE, "3600")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_HEADES, ALLOW_HEADERS).end((String) result);
			}
		} else
		{
			String accessControlAllowOrigin = StringUtils.defaultIfBlank(config.getString("access-control-allow-origin"),
					"http://127.0.0.1");

			if (routingContext.response().headers().get("content-type") == null)
			{
				log.warn("content type is null. set default to application/json");

				routingContext.response().putHeader("Content-Type", "application/json")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_ORIGIN, accessControlAllowOrigin)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_METHODS, ALLOW_METHODS)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_MAX_AGE, "3600")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_HEADES, ALLOW_HEADERS)
						.putHeader("Access-Control-Allow-Credentials", "true").end((String) result);
			} else
			{
				routingContext.response().putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_ORIGIN, accessControlAllowOrigin)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_METHODS, ALLOW_METHODS)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_MAX_AGE, "3600")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_HEADES, ALLOW_HEADERS)
						.putHeader("Access-Control-Allow-Credentials", "true").end((String) result);
			}
		}
	}
}