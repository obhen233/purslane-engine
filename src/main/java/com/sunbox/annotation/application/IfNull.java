package com.sunbox.annotation.application;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.TYPE)
public @interface IfNull {
	boolean value() default false;
}
