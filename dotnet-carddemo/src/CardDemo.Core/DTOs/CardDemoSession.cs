namespace CardDemo.Core.DTOs;

/// <summary>
/// Session state DTO that mirrors the CARDDEMO-COMMAREA from COBOL copybook COCOM01Y.cpy.
/// Replaces the CICS COMMAREA concept for managing session state between program transitions.
/// </summary>
public class CardDemoSession
{
    // General info (CDEMO-GENERAL-INFO)

    /// <summary>
    /// Source transaction ID. Maps to CDEMO-FROM-TRANID PIC X(04).
    /// </summary>
    public string FromTranId { get; set; } = string.Empty;

    /// <summary>
    /// Source program name. Maps to CDEMO-FROM-PROGRAM PIC X(08).
    /// </summary>
    public string FromProgram { get; set; } = string.Empty;

    /// <summary>
    /// Target transaction ID. Maps to CDEMO-TO-TRANID PIC X(04).
    /// </summary>
    public string ToTranId { get; set; } = string.Empty;

    /// <summary>
    /// Target program name. Maps to CDEMO-TO-PROGRAM PIC X(08).
    /// </summary>
    public string ToProgram { get; set; } = string.Empty;

    /// <summary>
    /// Current user ID. Maps to CDEMO-USER-ID PIC X(08).
    /// </summary>
    public string UserId { get; set; } = string.Empty;

    /// <summary>
    /// User type: "A" for admin, "U" for regular user.
    /// Maps to CDEMO-USER-TYPE PIC X(01).
    /// </summary>
    public string UserType { get; set; } = string.Empty;

    /// <summary>
    /// Program context: 0 = enter, 1 = re-enter.
    /// Maps to CDEMO-PGM-CONTEXT PIC 9(01).
    /// </summary>
    public int PgmContext { get; set; }

    // Customer info (CDEMO-CUSTOMER-INFO)

    /// <summary>
    /// Customer identifier. Maps to CDEMO-CUST-ID PIC 9(09).
    /// </summary>
    public long CustomerId { get; set; }

    /// <summary>
    /// Customer first name. Maps to CDEMO-CUST-FNAME PIC X(25).
    /// </summary>
    public string CustomerFirstName { get; set; } = string.Empty;

    /// <summary>
    /// Customer middle name. Maps to CDEMO-CUST-MNAME PIC X(25).
    /// </summary>
    public string CustomerMiddleName { get; set; } = string.Empty;

    /// <summary>
    /// Customer last name. Maps to CDEMO-CUST-LNAME PIC X(25).
    /// </summary>
    public string CustomerLastName { get; set; } = string.Empty;

    // Account info (CDEMO-ACCOUNT-INFO)

    /// <summary>
    /// Account identifier. Maps to CDEMO-ACCT-ID PIC 9(11).
    /// </summary>
    public long AccountId { get; set; }

    /// <summary>
    /// Account status. Maps to CDEMO-ACCT-STATUS PIC X(01).
    /// </summary>
    public string AccountStatus { get; set; } = string.Empty;

    // Card info (CDEMO-CARD-INFO)

    /// <summary>
    /// Card number. Maps to CDEMO-CARD-NUM PIC 9(16).
    /// </summary>
    public string CardNumber { get; set; } = string.Empty;

    // More info (CDEMO-MORE-INFO)

    /// <summary>
    /// Last map name used. Maps to CDEMO-LAST-MAP PIC X(7).
    /// </summary>
    public string LastMap { get; set; } = string.Empty;

    /// <summary>
    /// Last mapset name used. Maps to CDEMO-LAST-MAPSET PIC X(7).
    /// </summary>
    public string LastMapSet { get; set; } = string.Empty;
}
