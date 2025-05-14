package com.tttsaurus.saurus3d.common.core.gl.debug.khr;

public class DebugMessageFilter
{
    public static final String IDENTIFIR_DEFAULT = "default";
    public static final String IDENTIFIR_EMPTY = "";

    public static final DebugMessageFilter DEFAULT = new DebugMessageFilter(IDENTIFIR_DEFAULT);

    private final String identifier;
    public String getIdentifier() { return identifier; }

    protected DebugMessageFilter(String identifier)
    {
        this.identifier = identifier;
    }

    public DebugMessageFilter()
    {
        identifier = IDENTIFIR_EMPTY;
    }

    private DebugMsgSource source = DebugMsgSource.ANY;
    private DebugMsgType type = DebugMsgType.ANY;
    private DebugMsgSeverity severity = DebugMsgSeverity.ANY;

    public DebugMsgSource getSource() { return source; }
    public DebugMsgType getType() { return type; }
    public DebugMsgSeverity getSeverity() { return severity; }

    public DebugMessageFilter(DebugMsgSource source, DebugMsgType type, DebugMsgSeverity severity)
    {
        identifier = IDENTIFIR_EMPTY;
        this.source = source;
        this.type = type;
        this.severity = severity;
    }
}
