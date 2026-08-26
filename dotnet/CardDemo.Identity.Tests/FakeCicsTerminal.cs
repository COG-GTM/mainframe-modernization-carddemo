using CardDemo.Identity.Bms;
using CardDemo.Identity.Cics;

namespace CardDemo.Identity.Tests;

/// <summary>Records EXEC CICS terminal traffic and replays the operator's input.</summary>
public sealed class FakeCicsTerminal : ICicsTerminal
{
    public string? TypedUserid { get; set; }

    public string? TypedPasswd { get; set; }

    public int ReceiveRespCode { get; set; }

    public int ReceiveCount { get; private set; }

    public List<Cosgn0aMap> SentMaps { get; } = new();

    public List<string> SentTexts { get; } = new();

    public int ReceiveMap(string map, string mapset, Cosgn0aMap into)
    {
        ReceiveCount++;
        into.UseridI = TypedUserid ?? into.UseridI;
        into.PasswdI = TypedPasswd ?? into.PasswdI;
        return ReceiveRespCode;
    }

    public void SendMap(string map, string mapset, Cosgn0aMap from) =>
        SentMaps.Add(from.Clone());

    public void SendText(string message, int length) =>
        SentTexts.Add(message[..length]);

    public string AssignApplid() => "CICSAPP1";

    public string AssignSysid() => "CICS";
}
