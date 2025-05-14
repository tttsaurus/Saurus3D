package com.tttsaurus.saurus3d.common.core.gl.debug.khr;

public class KHRMessageFilter
{
    public static final String IDENTIFIR_DEFAULT = "default";
    public static final String IDENTIFIR_EMPTY = "";

    public static final KHRMessageFilter DEFAULT = new KHRMessageFilter(IDENTIFIR_DEFAULT);

    private final String identifier;
    public String getIdentifier() { return identifier; }

    protected KHRMessageFilter(String identifier)
    {
        this.identifier = identifier;
    }

    public KHRMessageFilter()
    {
        identifier = IDENTIFIR_EMPTY;
    }

    private KHRMsgSource source = KHRMsgSource.ANY;
    private KHRMsgType type = KHRMsgType.ANY;
    private KHRMsgSeverity severity = KHRMsgSeverity.ANY;

    public KHRMsgSource getSource() { return source; }
    public KHRMsgType getType() { return type; }
    public KHRMsgSeverity getSeverity() { return severity; }

    public KHRMessageFilter(KHRMsgSource source, KHRMsgType type, KHRMsgSeverity severity)
    {
        identifier = IDENTIFIR_EMPTY;
        this.source = source;
        this.type = type;
        this.severity = severity;
    }
}
