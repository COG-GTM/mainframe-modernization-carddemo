namespace CardDemo.Identity.Cics;

/// <summary>
/// The EIB fields COSGN00C inspects on entry: EIBCALEN (commarea length passed
/// by CICS) and EIBAID (the attention key that raised the transaction).
/// </summary>
/// <param name="EibCalen">EIBCALEN - 0 on the very first entry.</param>
/// <param name="EibAid">EIBAID - the attention identifier.</param>
public sealed record TerminalInput(int EibCalen, AttentionKey EibAid);
