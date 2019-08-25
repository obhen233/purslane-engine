package com.sunbox.annotation.framework;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Formator {
	Class<? extends com.sunbox.formator.Formator> value();
}
