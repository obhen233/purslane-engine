package com.sunbox.annotation.framework;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//规则的自定义参数，没有的话用默认参数param  参数名称参数值 keyvalue形式
@Documented
@Target(value={ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RuleParam {
	String value() default "";
}
