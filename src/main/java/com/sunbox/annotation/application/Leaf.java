
package com.sunbox.annotation.application;
//node 的话指向哪个leaf 或者node
//leaf的话 指向哪个rule 这个rule的class路径.还有是否取非

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Leaf {
	Rule[] value();
}