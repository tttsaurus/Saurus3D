package com.tttsaurus.saurus3d.common.core.gl.feature;

public interface IGLFeature
{
    // impl this like a static method but without the static modifier
    // dont access 'this' and instance fields in its impl
    boolean isSupported();
}
