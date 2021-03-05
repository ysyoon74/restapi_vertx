package u.cando.restapi.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import lombok.extern.slf4j.Slf4j;
import u.cando.restapi.RestApiConstants;
import u.cando.restapi.dto.Response;
import u.cando.restapi.util.RequestParamUtil;

/**
 * @Author 윤용승
 * @Since: 2021. 2. 10.
 */
@Slf4j
public class RestApiBeforeHandler implements Handler<RoutingContext>
{
	private static final String ALLOW_HEADERS = "Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization";

	private static final String ALLOW_METHODS = "GET,POST,PUT,DELETE";

	private static final String REQUEST_PARAM_ACCESS_CONTROL_ALLOW_HEADES = "Access-Control-Allow-Headers";

	private static final String REQUEST_PARAM_ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	private static final String REQUEST_PARAM_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	private static final String REQUEST_PARAM_ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	private final JsonObject config;

	public RestApiBeforeHandler(JsonObject config)
	{
		this.config = config;
	}

	/**
	 * 캐시에서 정보 가져오기
	 * 
	 * @param identifier
	 * @param instance
	 * @return
	 */
	private Object fetchFromCache(String identifier)
	{
		HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(RestApiConstants.CACHE_INSTANCE_NAME);

		if (Boolean.TRUE.equals(config.getBoolean(RestApiConstants.CONFIG_USE_CACHE, true)) && instance != null)
		{
			Map<String, Object> cachedMap = instance.getMap(RestApiConstants.CACHE_MAP_NAME);

			return cachedMap.get(identifier);
		}

		return null;
	}

	@Override
	public void handle(RoutingContext routingContext)
	{
		logRequestInfo(routingContext);

		String identifier = RequestParamUtil.generateIdentifier(routingContext);

		Object result = fetchFromCache(identifier);

		if (result != null)
		{
			Gson gson = new GsonBuilder().create();

			Response response = gson.fromJson((String) result, Response.class);

			response.setRequestId(routingContext.session().id());
			response.setRemoteAddress(routingContext.request().host());
			response.setValueCached(true);
			response.setProcessTime(0L);

			String cachedValue = Json.encodePrettily(response);

			pushToCache(result, identifier);

			if (BooleanUtils.toBoolean(response.getUseJsonp()))
			{
				if (StringUtils.isNotBlank(response.getCallback()))
				{
					result = response.getCallback() + "( " + result + " )";
				} else
				{
					result = "callback( " + result + " )";
				}
			} else
			{
				result = cachedValue;
			}

			if (Boolean.TRUE.equals(config.getBoolean(RestApiConstants.CONFIG_DEV_MODE, false)))
			{
				routingContext.response().putHeader("Content-Type", "application/json")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_ORIGIN, "*")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_METHODS, ALLOW_METHODS)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_MAX_AGE, "3600")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_HEADES, ALLOW_HEADERS).end((String) result);
			} else
			{
				String accessControlAllowOrigin = config.getString("access-control-allow-origin", "http://127.0.0.1");

				routingContext.response().putHeader("Content-Type", "application/json")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_ORIGIN, accessControlAllowOrigin)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_METHODS, ALLOW_METHODS)
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_MAX_AGE, "3600")
						.putHeader(REQUEST_PARAM_ACCESS_CONTROL_ALLOW_HEADES, ALLOW_HEADERS)
						.putHeader("Access-Control-Allow-Credentials", "true").end((String) result);
			}
		} else
		{
			try (InputStream is = new FileInputStream(new File("./html/swagger/swagger.json"));)
			{
				String swaggerConf = IOUtils.toString(is, StandardCharsets.UTF_8);

				JsonObject swagger = new JsonObject(swaggerConf);

				routingContext.put("swagger", swagger);
			} catch (IOException e)
			{
				log.error(e.getMessage());
			}

			routingContext.next();
		}
	}

	/**
	 * Request 정보 로그에 기록하기
	 * 
	 * @param routingContext
	 */
	private void logRequestInfo(RoutingContext routingContext)
	{
		String remoteAddress = "";

		if (log.isDebugEnabled())
		{
			Session session = routingContext.session();

			log.debug("Session ID : {}", session.id());

			HttpServerRequest request = routingContext.request();

			remoteAddress = request.remoteAddress().host();

			log.debug("absoluteURI : {}", request.absoluteURI());
			log.debug("host : {}", request.host());
			log.debug("method : {}", request.method());
			log.debug("path : {}", request.path());
			log.debug("query : {}", request.query());
			log.debug("method : {}", request.method());
			log.debug("remoteAddress : {} {}", remoteAddress, request.remoteAddress().port());
			log.debug("scheme : {}", request.scheme());
			log.debug("uri : {}", request.uri());
			log.debug("version : {}", request.version());

			MultiMap formMap = request.formAttributes();

			if (formMap != null)
			{
				log.debug("form attributes : {}", RequestParamUtil.getJsonStringFromMultiMapValue(formMap));
				log.debug("body : {}", routingContext.getBodyAsString());
			}

			Set<FileUpload> uploads = routingContext.fileUploads();

			if (uploads != null)
			{
				for (FileUpload oneUpload : uploads)
				{
					log.debug("uploaded File : {}", oneUpload.name());
				}
			}

			log.debug("config : {}", config.toString());
		}
	}

	/**
	 * 캐시에 저장하기
	 * 
	 * @param result
	 * @param identifier
	 */
	private void pushToCache(Object result, String identifier)
	{
		HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(RestApiConstants.CACHE_INSTANCE_NAME);

		if (instance != null && Boolean.TRUE.equals(config.getBoolean(RestApiConstants.CONFIG_USE_CACHE, true)))
		{
			Map<String, Object> cachedMap = instance.getMap(RestApiConstants.CACHE_MAP_NAME);

			cachedMap.put(identifier, result);

			log.debug("Cache({}) is added.", identifier);
		}
	}
}