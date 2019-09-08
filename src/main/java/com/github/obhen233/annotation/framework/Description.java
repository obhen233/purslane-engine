package com.github.obhen233.annotation.framework;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//描述语言 对字段的文字描述。两个字段 text 文字描述 lang 语言  默认是zh_cn
@Documented
@Target(value={ElementType.TYPE,ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Description {
	String desc() default "";
	String lang() default "zh_cn";
}
