using CardDemo.Identity.Cics;
using CardDemo.Identity.Copybooks;

namespace CardDemo.Identity.Tests;

/// <summary>Records EXEC CICS XCTL requests instead of transferring control.</summary>
public sealed class FakeProgramControl : IProgramControl
{
    public List<(string Program, CardDemoCommarea Commarea)> Transfers { get; } = new();

    public void Xctl(string program, CardDemoCommarea commarea) =>
        Transfers.Add((program, commarea));
}
