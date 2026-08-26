using CardDemo.Identity.Bms;

namespace CardDemo.Identity.Cics;

/// <summary>
/// The EXEC CICS terminal operations used by COSGN00C: RECEIVE MAP, SEND MAP,
/// SEND TEXT and ASSIGN APPLID/SYSID.
/// </summary>
public interface ICicsTerminal
{
    /// <summary>
    /// EXEC CICS RECEIVE MAP(map) MAPSET(mapset) - populates the symbolic map
    /// input fields from the terminal and returns the RESP code.
    /// </summary>
    int ReceiveMap(string map, string mapset, Cosgn0aMap into);

    /// <summary>EXEC CICS SEND MAP(map) MAPSET(mapset) FROM(from) ERASE CURSOR.</summary>
    void SendMap(string map, string mapset, Cosgn0aMap from);

    /// <summary>EXEC CICS SEND TEXT FROM(message) LENGTH(length) ERASE FREEKB.</summary>
    void SendText(string message, int length);

    /// <summary>EXEC CICS ASSIGN APPLID(...).</summary>
    string AssignApplid();

    /// <summary>EXEC CICS ASSIGN SYSID(...).</summary>
    string AssignSysid();
}
