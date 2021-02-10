package u.cando.restapi.controller;

import java.lang.annotation.Annotation;

import u.cando.restapi.dto.Response;

/**
 * 추상 컨트롤러. 공통으로 사용할 부분 구현.
 * 
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Controller(version = "RestAPI v20210209")
public abstract class AbstractRestApiController
{
	/**
	 * 버전 정보 표시
	 * 
	 * @return
	 */
	public Response doGETVersion()
	{
		String version = getVersion();

		Response response = new Response();

		response.setReturnObject(version);
		response.setReturnType(version.getClass().getName());

		return response;
	}

	/**
	 * 어노테이션에 정의된 버전 정보 가져오기
	 * 
	 * @return
	 */
	protected String getVersion()
	{
		String version = "";

		Annotation[] annotations = this.getClass().getAnnotations();

		for (Annotation annotation : annotations)
		{
			Controller controller = (Controller) annotation;

			version = controller.version();
		}

		return version;
	}
}