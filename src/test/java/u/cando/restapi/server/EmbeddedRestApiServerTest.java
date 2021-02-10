package u.cando.restapi.server;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import u.cando.restapi.config.RouteInfo;
import u.cando.restapi.config.ServiceInfo;

class EmbeddedRestApiServerTest
{
	@Test
	void testHazelcast()
	{
		Config config = new Config();
		
        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        
        Map<Integer, String> mapCustomers = instance.getMap("customers");
        
        mapCustomers.put(1, "Joe");
        mapCustomers.put(2, "Ali");
        mapCustomers.put(3, "Avi");

        System.out.println("Customer with key 1: "+ mapCustomers.get(1));
        System.out.println("Map Size:" + mapCustomers.size());
	}
	
	@Test
	void testYamlCreate()
	{
		RouteInfo routeInfo = new RouteInfo();
		
		List<ServiceInfo> services = new ArrayList<>();
		
		ServiceInfo oneService = new ServiceInfo();
		
		oneService.setServiceUrl("/api/dictionary");
		oneService.setBlockable(true);
		oneService.setTargetHandler("u.cando.restapi.controller.DictionaryController");
		
		services.add(oneService);
		
		oneService = new ServiceInfo();
		
		oneService.setServiceUrl("/api/sample");
		oneService.setBlockable(false);
		oneService.setTargetHandler("u.cando.restapi.controller.SampleController");
		
		services.add(oneService);
		
		routeInfo.setServices(services);
		
		File serviceConfig = new File("./conf/service2.yaml");
		
		ObjectMapper om = new ObjectMapper(new YAMLFactory());
		
		try
		{
			om.writeValue(serviceConfig, routeInfo);
			
			assertTrue(true);
		} catch (IOException e)
		{
			e.printStackTrace();
			
			assertTrue(false);
		}
	}
}