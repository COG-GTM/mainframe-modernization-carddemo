using CardDemo.Identity.Cobol;

namespace CardDemo.Identity.Copybooks;

/// <summary>
/// COCOM01Y - 01 CARDDEMO-COMMAREA. Field lengths mirror the copybook PIC clauses.
/// </summary>
public sealed class CardDemoCommarea
{
    // 05 CDEMO-GENERAL-INFO.
    public string CdemoFromTranid { get; set; } = CobolString.Spaces(4);   // PIC X(04)
    public string CdemoFromProgram { get; set; } = CobolString.Spaces(8);  // PIC X(08)
    public string CdemoToTranid { get; set; } = CobolString.Spaces(4);     // PIC X(04)
    public string CdemoToProgram { get; set; } = CobolString.Spaces(8);    // PIC X(08)
    public string CdemoUserId { get; set; } = CobolString.Spaces(8);       // PIC X(08)
    public string CdemoUserType { get; set; } = CobolString.Spaces(1);     // PIC X(01)
    public int CdemoPgmContext { get; set; }                               // PIC 9(01)

    // 05 CDEMO-CUSTOMER-INFO.
    public long CdemoCustId { get; set; }                                  // PIC 9(09)
    public string CdemoCustFname { get; set; } = CobolString.Spaces(25);   // PIC X(25)
    public string CdemoCustMname { get; set; } = CobolString.Spaces(25);   // PIC X(25)
    public string CdemoCustLname { get; set; } = CobolString.Spaces(25);   // PIC X(25)

    // 05 CDEMO-ACCOUNT-INFO.
    public long CdemoAcctId { get; set; }                                  // PIC 9(11)
    public string CdemoAcctStatus { get; set; } = CobolString.Spaces(1);   // PIC X(01)

    // 05 CDEMO-CARD-INFO.
    public decimal CdemoCardNum { get; set; }                              // PIC 9(16)

    // 05 CDEMO-MORE-INFO.
    public string CdemoLastMap { get; set; } = CobolString.Spaces(7);      // PIC X(7)
    public string CdemoLastMapset { get; set; } = CobolString.Spaces(7);   // PIC X(7)

    // 88 CDEMO-USRTYP-ADMIN VALUE 'A'.
    public bool CdemoUsrtypAdmin => CdemoUserType == "A";

    // 88 CDEMO-USRTYP-USER VALUE 'U'.
    public bool CdemoUsrtypUser => CdemoUserType == "U";

    // 88 CDEMO-PGM-ENTER VALUE 0.
    public bool CdemoPgmEnter => CdemoPgmContext == 0;

    // 88 CDEMO-PGM-REENTER VALUE 1.
    public bool CdemoPgmReenter => CdemoPgmContext == 1;
}
