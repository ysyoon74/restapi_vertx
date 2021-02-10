package u.cando.restapi.dto;

/**
 * RestAPI에서 사용되는 Request 객체 인터페이스.
 * 
 * @Author 윤용승
 * @Since: 2021. 2. 9.
 */
public interface IRestApiRequest
{
	public String toJsonString();
}