package u.cando.restapi.dto;

/**
 * RestAPI 관련 에러 정의.
 * <ol>
 * <li>100: DataBase 관련 에러</li>
 * <li>200: 솔루션 관련 에러</li>
 * <li>300: 자바 처리 관련 에러</li>
 * <li>400: 기타 관련 에러</li>
 * <ol>
 * 
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
public class RestApiError
{
	private final int code;

	private final String message;

	public RestApiError(final int code, final String message)
	{
		this.code = code;
		this.message = message;
	}

	/**
	 * @return the code
	 */
	public int getCode()
	{
		return code;
	}

	/**
	 * @return the message
	 */
	public String getMessage()
	{
		return message;
	}
}