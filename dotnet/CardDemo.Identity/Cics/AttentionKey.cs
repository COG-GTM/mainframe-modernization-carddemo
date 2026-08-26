namespace CardDemo.Identity.Cics;

/// <summary>DFHAID attention identifiers referenced by COSGN00C (EIBAID).</summary>
public enum AttentionKey
{
    /// <summary>DFHENTER</summary>
    Enter,

    /// <summary>DFHPF3</summary>
    Pf3,

    /// <summary>Any other attention key (WHEN OTHER).</summary>
    Other
}
