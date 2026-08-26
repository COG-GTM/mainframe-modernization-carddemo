using CardDemo.Identity.Cobol;

namespace CardDemo.Identity.Copybooks;

/// <summary>
/// CSUSR01Y - 01 SEC-USER-DATA, the USRSEC (VSAM KSDS) record keyed by SEC-USR-ID.
/// </summary>
public sealed class SecUserData
{
    public string SecUsrId { get; set; } = CobolString.Spaces(8);       // PIC X(08)
    public string SecUsrFname { get; set; } = CobolString.Spaces(20);   // PIC X(20)
    public string SecUsrLname { get; set; } = CobolString.Spaces(20);   // PIC X(20)
    public string SecUsrPwd { get; set; } = CobolString.Spaces(8);      // PIC X(08)
    public string SecUsrType { get; set; } = CobolString.Spaces(1);     // PIC X(01)
    public string SecUsrFiller { get; set; } = CobolString.Spaces(23);  // PIC X(23)
}
