package com.sunbox.annotation.framework;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//规则的自定义基准值，没有的话，默认base
@Documented
@Target(value={ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RuleBase {
	String value() default "";
	String base() default "";
}
