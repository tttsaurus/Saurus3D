package com.tttsaurus.saurus3d.common.core.gl.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Saurus3DGLFeature
{
    // feature name
    String value() default "Unspecified Feature Name";
}
