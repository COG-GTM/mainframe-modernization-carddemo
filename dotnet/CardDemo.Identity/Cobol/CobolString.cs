namespace CardDemo.Identity.Cobol;

/// <summary>
/// Helpers reproducing COBOL alphanumeric (PIC X(n)) semantics: MOVE truncates
/// or space-pads to the receiving field length, and comparisons against SPACES
/// or LOW-VALUES treat an all-space, all-NUL or empty field as such.
/// </summary>
public static class CobolString
{
    /// <summary>COBOL MOVE of an alphanumeric source into a PIC X(length) field.</summary>
    public static string Move(string? source, int length)
    {
        var value = source ?? string.Empty;
        return value.Length >= length ? value[..length] : value.PadRight(length, ' ');
    }

    /// <summary>COBOL LOW-VALUES for a PIC X(length) field.</summary>
    public static string LowValues(int length) => new('\0', length);

    /// <summary>COBOL SPACES for a PIC X(length) field.</summary>
    public static string Spaces(int length) => new(' ', length);

    /// <summary>True when the field equals SPACES or LOW-VALUES.</summary>
    public static bool IsSpacesOrLowValues(string? value)
    {
        if (string.IsNullOrEmpty(value))
        {
            return true;
        }

        return value.All(c => c == ' ') || value.All(c => c == '\0');
    }

    /// <summary>COBOL FUNCTION UPPER-CASE (invariant).</summary>
    public static string UpperCase(string? value) =>
        (value ?? string.Empty).ToUpperInvariant();
}
