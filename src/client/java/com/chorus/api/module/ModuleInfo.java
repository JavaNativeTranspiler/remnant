
/**
 * Created: 12/7/2024
 */
package com.chorus.api.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ModuleInfo {
    String name();
    String description() default "";
    ModuleCategory category();
    int key()            default -1;
    String suffix() default "";
}


//~ Formatted by Jindent --- http://www.jindent.com
