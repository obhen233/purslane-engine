package com.github.obhen233.annotation.framework;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Formator {
	Class<? extends com.github.obhen233.formator.Formator> value();
}
