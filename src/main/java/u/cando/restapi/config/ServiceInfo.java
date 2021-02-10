package u.cando.restapi.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Setter
@Getter
public class ServiceInfo
{
	/**
	 * Block Handler 사용 여부
	 */
	private Boolean blockable;

	/**
	 * 서비스 URL
	 */
	private String serviceUrl;

	/**
	 * 대상 핸들러
	 */
	private String targetHandler;
}