package com.sunbox.annotation.framework;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface NoParam {

}
