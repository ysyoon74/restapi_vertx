package u.cando.restapi.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.hazelcast.config.Config;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import lombok.extern.slf4j.Slf4j;
import u.cando.restapi.RestApiConstants;
import u.cando.restapi.config.RouteInfo;
import u.cando.restapi.config.ServiceInfo;
import u.cando.restapi.handler.RestApiAfterHandler;
import u.cando.restapi.handler.RestApiBeforeHandler;
import u.cando.restapi.handler.RestApiHandler;

/**
 * RestApi 서버
 * 
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Slf4j
public class EmbeddedRestApiServer
{
	private static JsonObject config;

	private static final String URI_PREFIX = "/api/*";

	private static void createRoutingRule(Router router)
	{
		File serviceConfig = new File("./conf/service.yaml");

		if (!serviceConfig.exists())
		{
			return;
		}

		ObjectMapper om = new ObjectMapper(new YAMLFactory());

		RouteInfo routeInfo = null;

		try
		{
			routeInfo = om.readValue(serviceConfig, RouteInfo.class);
		} catch (IOException e)
		{
			log.error(e.getMessage());
		}

		if (routeInfo != null)
		{
			for (ServiceInfo service : routeInfo.getServices())
			{
				Route route = router.route(service.getServiceUrl() + "/*");

				String className = service.getTargetHandler();

				try
				{
					Object controller = Class.forName(className).getDeclaredConstructor().newInstance();

					Handler<RoutingContext> targetHandler = new RestApiHandler(controller, service.getServiceUrl());

					if (service.getBlockable().equals(Boolean.TRUE))
					{
						route.blockingHandler(targetHandler);
					} else
					{
						route.handler(targetHandler);
					}
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | ClassNotFoundException e)
				{
					log.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		StopWatch stopWatch = new StopWatch(EmbeddedRestApiServer.class.getName());

		stopWatch.start("readConfigurationFromFile");

		readConfigurationFromFile(args);

		stopWatch.stop();

		stopWatch.start("vertxOptions");

		Config hazelcastConfig = ConfigUtil.loadConfig();

		hazelcastConfig.setInstanceName(RestApiConstants.CACHE_INSTANCE_NAME);

		ClusterManager clusterManager = new HazelcastClusterManager(hazelcastConfig);

		VertxOptions options = new VertxOptions(config.getJsonObject("vertx.options")).setClusterManager(clusterManager);

		options.setMetricsOptions(
				new MicrometerMetricsOptions().setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true)).setEnabled(true));

		Vertx vertx = Vertx.vertx(options);

		stopWatch.stop();

		stopWatch.start("router");

		// Later on, creating a router
		Router router = Router.router(vertx);

		// Create a local session store using defaults
		SessionStore store = LocalSessionStore.create(vertx);

		SessionHandler sessionHandler = SessionHandler.create(store);

		// Make sure all requests are routed through the session handler too
		router.route().handler(sessionHandler);

		// ResponseContentTypeHandler 적용
		router.route(URI_PREFIX).handler(ResponseContentTypeHandler.create());

		// Form 데이터 관련 핸들러 적용
		router.route(URI_PREFIX).handler(BodyHandler.create(config.getString(RestApiConstants.CONFIG_FILE_UPLOAD)));

		router.route(URI_PREFIX).handler(new RestApiBeforeHandler(config));

		createRoutingRule(router);

		router.route(URI_PREFIX).handler(new RestApiAfterHandler(config));

		router.route().handler(StaticHandler.create().setWebRoot("webroot"));

		// API 문서(Swagger 사용)
		router.route("/assets/*").handler(StaticHandler.create("./html/swagger"));

		//관리용 페이지 구성용
		router.route("/manager/*").handler(StaticHandler.create("./html/manager"));

		// 관리자 화면
		router.route("/metrics").handler(PrometheusScrapingHandler.create());

		vertx.createHttpServer().requestHandler(router::handle).listen((config.getInteger(RestApiConstants.CONFIG_HTTP_PORT, 9090)));

		stopWatch.stop();

		if (config.getBoolean(RestApiConstants.CONFIG_CLUSTER_MODE).equals(Boolean.TRUE)
				|| config.getBoolean(RestApiConstants.CONFIG_USE_CACHE).equals(Boolean.TRUE))
		{
			Vertx.clusteredVertx(options);
		}

		if (log.isInfoEnabled())
		{
			log.info("Server was started in {} (ms)", DurationFormatUtils.formatDurationHMS(stopWatch.getTotalTimeMillis()));
			log.debug("Time Detail : {}", stopWatch.prettyPrint());
		}
	}

	private static void readConfigurationFromFile(String[] args)
	{
		if (args.length > 0)
		{
			String filePath = "./conf/restapi-conf.json";

			for (String oneArgument : args)
			{
				if (oneArgument.startsWith("-conf"))
				{
					String[] value = oneArgument.split("=");

					filePath = StringUtils.trim(value[1]);

					break;
				}
			}

			Gson gson = new GsonBuilder().create();

			try
			{
				@SuppressWarnings("unchecked")
				Map<String, Object> mapValue = gson.fromJson(new FileReader(new File(filePath)), LinkedHashMap.class);

				config = JsonObject.mapFrom(mapValue);
			} catch (JsonSyntaxException | JsonIOException | FileNotFoundException e)
			{
				log.error(e.getMessage());
			}
		}

		if (config == null)
		{
			config = new JsonObject();
		}
	}
}