package com.tttsaurus.saurus3d.common.api.shader.uniform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// especially for glsl 330

// should only get this from UniformParseUtils
public class UniformType
{
    public static final UniformType UNKNOWN = new UniformType("NO_SYMBOL", UniformTypeKind.UNKNOWN);
    public static final UniformType ARRAY = new UniformType("NO_SYMBOL", UniformTypeKind.ARRAY);

    public static final List<UniformType> TYPE_PRESET = new ArrayList<>(Arrays.asList(

            new UniformType("float", UniformTypeKind.SCALAR),
            new UniformType("int", UniformTypeKind.SCALAR),
            new UniformType("uint", UniformTypeKind.SCALAR),
            new UniformType("bool", UniformTypeKind.SCALAR),

            new UniformType("vec2", UniformTypeKind.VECTOR),
            new UniformType("vec3", UniformTypeKind.VECTOR),
            new UniformType("vec4", UniformTypeKind.VECTOR),

            new UniformType("mat2", UniformTypeKind.MATRIX),
            new UniformType("mat3", UniformTypeKind.MATRIX),
            new UniformType("mat4", UniformTypeKind.MATRIX),

            new UniformType("sampler1D", UniformTypeKind.SAMPLER),
            new UniformType("sampler2D", UniformTypeKind.SAMPLER),
            new UniformType("sampler3D", UniformTypeKind.SAMPLER),
            new UniformType("samplerCube", UniformTypeKind.SAMPLER),
            new UniformType("sampler2DArray", UniformTypeKind.SAMPLER),
            new UniformType("samplerCubeArray", UniformTypeKind.SAMPLER),
            new UniformType("sampler1DShadow", UniformTypeKind.SAMPLER),
            new UniformType("sampler2DShadow", UniformTypeKind.SAMPLER),
            new UniformType("samplerCubeShadow", UniformTypeKind.SAMPLER),
            new UniformType("sampler2DMS", UniformTypeKind.SAMPLER),
            new UniformType("sampler2DArrayShadow", UniformTypeKind.SAMPLER),

            new UniformType("atomic_uint", UniformTypeKind.ATOMIC_COUNTER),

            new UniformType("image2D", UniformTypeKind.IMAGE),
            new UniformType("image3D", UniformTypeKind.IMAGE),
            new UniformType("image2DArray", UniformTypeKind.IMAGE),
            new UniformType("imageCube", UniformTypeKind.IMAGE),
            new UniformType("image2DArrayShadow", UniformTypeKind.IMAGE)
    ));

    private String symbol;
    private UniformTypeKind kind;
    private Variant variant = Variant.F;
    private UniformType subType = null;
    private int arrayLength = 0;

    protected UniformType(String symbol, UniformTypeKind kind)
    {
        this.symbol = symbol;
        this.kind = kind;
    }

    protected UniformType setSymbol(String symbol) { this.symbol = symbol; return this; }
    public String getSymbol() { return symbol; }

    protected UniformType setKind(UniformTypeKind kind) { this.kind = kind; return this; }
    public UniformTypeKind getKind() { return kind; }

    protected UniformType setVariant(Variant variant) { this.variant = variant; return this; }
    public Variant getVariant() { return variant; }

    protected UniformType setSubType(UniformType subType) { this.subType = subType; return this; }
    public UniformType getSubType() { return subType; }

    protected UniformType setArrayLength(int arrayLength) { this.arrayLength = arrayLength; return this; }
    public int getArrayLength() { return arrayLength; }

    public UniformType deepClone()
    {
        UniformType cloned = new UniformType(symbol, kind);
        cloned.variant = variant;
        cloned.arrayLength = arrayLength;
        if (subType == null)
            cloned.subType = null;
        else
            cloned.subType = subType.deepClone();
        return cloned;
    }

    @Override
    public String toString()
    {
        UniformType temp;

        StringBuilder builder = new StringBuilder();
        builder.append(symbol);

        temp = this;

        if (temp.kind == UniformTypeKind.ARRAY && temp.subType != null)
        {
            builder.append("[").append(temp.arrayLength).append("]");
            temp = temp.subType;
        }

        builder.append(" (").append(temp.kind.toString()).append(")");

        return builder.toString();
    }
}
