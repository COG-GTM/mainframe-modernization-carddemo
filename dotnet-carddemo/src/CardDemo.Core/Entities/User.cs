namespace CardDemo.Core.Entities;

/// <summary>
/// Represents a system user derived from COBOL copybook CSUSR01Y.cpy (SEC-USER-DATA).
/// </summary>
public class User
{
    /// <summary>
    /// User identifier. Maps to SEC-USR-ID PIC X(08).
    /// </summary>
    public string UserId { get; set; } = string.Empty;

    /// <summary>
    /// User first name. Maps to SEC-USR-FNAME PIC X(20).
    /// </summary>
    public string FirstName { get; set; } = string.Empty;

    /// <summary>
    /// User last name. Maps to SEC-USR-LNAME PIC X(20).
    /// </summary>
    public string LastName { get; set; } = string.Empty;

    /// <summary>
    /// User password (plaintext in COBOL, will be replaced with hashed password in Phase 2).
    /// Maps to SEC-USR-PWD PIC X(08).
    /// </summary>
    public string Password { get; set; } = string.Empty;

    /// <summary>
    /// User type: "A" for admin, "U" for regular user.
    /// Maps to SEC-USR-TYPE PIC X(01).
    /// </summary>
    public string UserType { get; set; } = string.Empty;
}
