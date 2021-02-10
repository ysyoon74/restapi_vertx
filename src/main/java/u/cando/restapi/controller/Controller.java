package u.cando.restapi.controller;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
/**
 * @Author 윤용승
 * @Since: 2021. 2. 10.
 */
public @interface Controller
{
	String version() default "v20210209";
}