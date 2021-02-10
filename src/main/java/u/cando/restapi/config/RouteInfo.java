package u.cando.restapi.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * YAML 파일에서 읽은 정보 저장
 * 
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Setter
@Getter
public class RouteInfo
{
	private List<ServiceInfo> services;
}