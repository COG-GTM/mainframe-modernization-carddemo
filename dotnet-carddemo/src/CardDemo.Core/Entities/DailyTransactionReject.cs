namespace CardDemo.Core.Entities;

/// <summary>
/// Represents a rejected daily transaction record.
/// Same fields as DailyTransaction plus reject reason information.
/// Derived from the reject logic in CBTRN02C.cbl.
/// </summary>
public class DailyTransactionReject
{
    /// <summary>
    /// Reject record identifier (auto-generated).
    /// </summary>
    public long DailyTransactionRejectId { get; set; }

    /// <summary>
    /// Transaction identifier. Maps to TRAN-ID PIC X(16).
    /// </summary>
    public string TransactionId { get; set; } = string.Empty;

    /// <summary>
    /// Transaction type code. Maps to TRAN-TYPE-CD PIC X(02).
    /// </summary>
    public string TransactionType { get; set; } = string.Empty;

    /// <summary>
    /// Transaction category code. Maps to TRAN-CAT-CD PIC 9(04).
    /// </summary>
    public int TransactionCategory { get; set; }

    /// <summary>
    /// Transaction source. Maps to TRAN-SOURCE PIC X(10).
    /// </summary>
    public string TransactionSource { get; set; } = string.Empty;

    /// <summary>
    /// Transaction description. Maps to TRAN-DESC PIC X(100).
    /// </summary>
    public string TransactionDescription { get; set; } = string.Empty;

    /// <summary>
    /// Transaction amount. Maps to TRAN-AMT PIC S9(09)V99.
    /// </summary>
    public decimal TransactionAmount { get; set; }

    /// <summary>
    /// Merchant identifier. Maps to TRAN-MERCHANT-ID PIC 9(09).
    /// </summary>
    public string MerchantId { get; set; } = string.Empty;

    /// <summary>
    /// Merchant name. Maps to TRAN-MERCHANT-NAME PIC X(50).
    /// </summary>
    public string MerchantName { get; set; } = string.Empty;

    /// <summary>
    /// Merchant city. Maps to TRAN-MERCHANT-CITY PIC X(50).
    /// </summary>
    public string MerchantCity { get; set; } = string.Empty;

    /// <summary>
    /// Merchant ZIP code. Maps to TRAN-MERCHANT-ZIP PIC X(10).
    /// </summary>
    public string MerchantZip { get; set; } = string.Empty;

    /// <summary>
    /// Card number associated with the transaction. Maps to TRAN-CARD-NUM PIC X(16).
    /// </summary>
    public string CardNumber { get; set; } = string.Empty;

    /// <summary>
    /// Original timestamp. Maps to TRAN-ORIG-TS PIC X(26).
    /// </summary>
    public DateTime OriginTimestamp { get; set; }

    /// <summary>
    /// Processing timestamp. Maps to TRAN-PROC-TS PIC X(26).
    /// </summary>
    public DateTime ProcessingTimestamp { get; set; }

    /// <summary>
    /// Reject reason code indicating why the transaction was rejected.
    /// </summary>
    public int RejectReasonCode { get; set; }

    /// <summary>
    /// Human-readable description of the reject reason.
    /// </summary>
    public string RejectReasonDescription { get; set; } = string.Empty;
}
