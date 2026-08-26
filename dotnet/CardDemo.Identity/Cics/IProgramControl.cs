using CardDemo.Identity.Copybooks;

namespace CardDemo.Identity.Cics;

/// <summary>EXEC CICS XCTL program control transfer.</summary>
public interface IProgramControl
{
    /// <summary>EXEC CICS XCTL PROGRAM(program) COMMAREA(commarea).</summary>
    void Xctl(string program, CardDemoCommarea commarea);
}
