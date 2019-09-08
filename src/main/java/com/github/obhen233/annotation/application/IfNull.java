package com.github.obhen233.annotation.application;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.TYPE)
public @interface IfNull {
	boolean value() default false;
}
