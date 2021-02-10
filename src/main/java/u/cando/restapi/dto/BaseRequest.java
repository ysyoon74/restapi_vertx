package u.cando.restapi.dto;

import io.vertx.core.json.Json;
import lombok.Getter;
import lombok.Setter;

/**
 * 공통 파라미터를 선언하기 위한 기본 Request 객체.
 * 
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Setter
@Getter
public class BaseRequest implements IRestApiRequest
{
	private String callback;

	private String requestId;

	private Boolean testMode;

	private Boolean useJsonp;

	private Boolean useMorph;

	@Override
	public String toJsonString()
	{
		return Json.encodePrettily(this);
	}
}