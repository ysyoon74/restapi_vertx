package u.cando.restapi.controller;

import u.cando.restapi.dto.Response;

/**
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Controller(version = "Simple API v20210209")
public class SimpleController extends AbstractRestApiController
{
	@Override
	public Response doGETVersion()
	{
		String version = getVersion();

		Response response = new Response();

		response.setReturnObject(version);
		response.setReturnType(version.getClass().getName());

		return response;
	}
}