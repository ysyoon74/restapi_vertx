package u.cando.restapi.dto;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;

import lombok.Getter;
import lombok.Setter;
import u.cando.restapi.RestApiConstants;

/**
 * API 결과를 담는 그릇
 * 
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
@Setter
@Getter
public class Response
{
	/**
	 * API 오류 발생시 사용
	 */
	private RestApiError apiError;

	/**
	 * JSONP 방식을 사용할 경우 callback 함수명
	 */
	private String callback;

	/**
	 * 수행시간
	 */
	private Long processTime = 0L;

	/**
	 * 호출 서버 아이피
	 */
	private String remoteAddress;
	
	/**
	 * 요청 고유번호(UUID 사용)
	 */
	private String requestId = UUID.randomUUID().toString();

	/**
	 * 결과 객체
	 */
	private Object returnObject;

	/**
	 * 결과 목록 형태
	 */
	private String returnType;

	/**
	 * 처리 결과 성공 여부
	 */
	private Boolean success = Boolean.TRUE;

	/**
	 * 처리 결과 전체 건수
	 */
	private Long totalResultCount = 0L;

	/**
	 * JSONP 방식 사용 여부: callback 파라미터 반드시 필요함.
	 */
	private Boolean useJsonp = Boolean.FALSE;

	/**
	 * 결과값 캐쉬되었는지 여부
	 */
	private Boolean valueCached= Boolean.FALSE;

	public Response()
	{
		super();
	}

	public Response(Map<String, String> parameters)
	{
		this.useJsonp = BooleanUtils.toBooleanObject(parameters.get(RestApiConstants.PARAMETER_USE_JSONP));
		
		this.callback = parameters.get(RestApiConstants.PARAMETER_CALLBACK);
		this.requestId = parameters.get(RestApiConstants.PARAMETER_REQUEST_ID);
	}
}