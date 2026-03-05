namespace CardDemo.Core.Entities;

/// <summary>
/// Represents a transaction type lookup record.
/// Derived from transaction category/type codes used across COBOL programs.
/// </summary>
public class TransactionType
{
    /// <summary>
    /// Transaction type code (e.g., "01", "02"). Maps to TRAN-TYPE-CD PIC X(02).
    /// </summary>
    public string TypeCode { get; set; } = string.Empty;

    /// <summary>
    /// Human-readable description of the transaction type.
    /// </summary>
    public string TypeDescription { get; set; } = string.Empty;
}
