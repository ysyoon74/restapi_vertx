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
	private final JsonObject config;

	public RestApiBeforeHandler(JsonObject config)
	{
		this.config = config;
	}

	@Override
	public void handle(RoutingContext routingContext)
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

		String identifier = RequestParamUtil.generateIdentifier(routingContext);

		HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(RestApiConstants.CACHE_INSTANCE_NAME);

		Object result = null;

		if (Boolean.TRUE.equals(config.getBoolean(RestApiConstants.CONFIG_USE_CACHE, true)) && instance != null)
		{
			Map<String, String> cachedMap = instance.getMap(RestApiConstants.CACHE_MAP_NAME);

			result = cachedMap.get(identifier);

			if (log.isDebugEnabled() && result != null)
			{
				log.debug("Result from : Cache({})", identifier);
			}
		}

		if (result != null)
		{
			Gson gson = new GsonBuilder().create();

			Response response = gson.fromJson((String) result, Response.class);

			response.setRequestId(routingContext.session().id());
			response.setRemoteAddress(remoteAddress);
			response.setValueCached(true);
			response.setProcessTime(0L);

			String cachedValue = Json.encodePrettily(response);

			if (Boolean.TRUE.equals(config.getBoolean(RestApiConstants.CONFIG_USE_CACHE, true)))
			{
				Map<String, String> cachedMap = instance.getMap(RestApiConstants.CACHE_MAP_NAME);

				if (cachedMap != null)
				{
					result = cachedMap.put(identifier, cachedValue);

					log.debug("Cache[{}] is added.", identifier);
				}
			}

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
				routingContext.response().putHeader("Content-Type", "application/json").putHeader("Access-Control-Allow-Origin", "*")
						.putHeader("Access-Control-Allow-Methods", "GET,POST,OPTION").putHeader("Access-Control-Max-Age", "3600")
						.putHeader("Access-Control-Allow-Headers",
								"Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization")
						.end((String) result);
			} else
			{
				String accessControlAllowOrigin = config.getString("access-control-allow-origin");

				if (StringUtils.isBlank(accessControlAllowOrigin))
				{
					accessControlAllowOrigin = "http://127.0.0.1";
				}

				routingContext.response().putHeader("Content-Type", "application/json")
						.putHeader("Access-Control-Allow-Origin", accessControlAllowOrigin)
						.putHeader("Access-Control-Allow-Methods", "GET,POST,OPTION").putHeader("Access-Control-Max-Age", "3600")
						.putHeader("Access-Control-Allow-Headers",
								"Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization")
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
}