using CardDemo.Identity.Copybooks;

namespace CardDemo.Identity.Cics;

/// <summary>
/// EXEC CICS READ against the USRSEC dataset. COSGN00C only reads this file.
/// </summary>
public interface IUserSecurityFile
{
    /// <summary>
    /// READ DATASET(dataset) INTO(SEC-USER-DATA) RIDFLD(key). Returns the RESP
    /// code: 0 = NORMAL, 13 = NOTFND, anything else = other error.
    /// </summary>
    int Read(string dataset, string key, SecUserData into);
}
