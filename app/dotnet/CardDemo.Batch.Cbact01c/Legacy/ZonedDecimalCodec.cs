using System.Globalization;

namespace CardDemo.Batch.Cbact01c.Legacy;

/// <summary>
/// Decodes and encodes zoned-decimal "signed overpunch" numerics used by the legacy
/// account file and by COBOL DISPLAY of signed numeric fields.
/// The sign is encoded in the final digit: '{' and 'A'..'I' = positive 0..9,
/// '}' and 'J'..'R' = negative 0..9. Plain trailing digits are treated as positive.
/// </summary>
public static class ZonedDecimalCodec
{
    private static readonly char[] PositiveOverpunch = ['{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'];
    private static readonly char[] NegativeOverpunch = ['}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R'];

    /// <summary>
    /// Converts a signed-overpunch numeric string into a <see cref="decimal"/>.
    /// </summary>
    /// <param name="raw">The raw fixed-width digits, e.g. "00000001940{".</param>
    /// <param name="decimalPlaces">Number of implied decimal places (V99 =&gt; 2).</param>
    public static decimal Decode(string raw, int decimalPlaces)
    {
        ArgumentNullException.ThrowIfNull(raw);
        var trimmed = raw.Trim();
        if (trimmed.Length == 0)
        {
            throw new FormatException("Cannot decode an empty overpunch value.");
        }

        var (lastDigit, isNegative) = DecodeOverpunchChar(trimmed[^1]);
        var digits = trimmed[..^1] + lastDigit;

        foreach (var c in digits)
        {
            if (!char.IsDigit(c))
            {
                throw new FormatException($"Invalid numeric character '{c}' in overpunch value '{raw}'.");
            }
        }

        var scaled = decimal.Parse(digits, CultureInfo.InvariantCulture);
        var value = scaled / Pow10(decimalPlaces);
        return isNegative ? -value : value;
    }

    /// <summary>
    /// Encodes a <see cref="decimal"/> back into its zoned-overpunch representation,
    /// matching how COBOL stores/DISPLAYs a signed PIC S9(int)V(dec) field.
    /// </summary>
    /// <param name="value">The value to encode.</param>
    /// <param name="integerDigits">Number of integer digit positions (S9(10) =&gt; 10).</param>
    /// <param name="decimalPlaces">Number of implied decimal places (V99 =&gt; 2).</param>
    public static string Encode(decimal value, int integerDigits, int decimalPlaces)
    {
        var totalDigits = integerDigits + decimalPlaces;
        var isNegative = value < 0;
        var scaled = decimal.Round(Math.Abs(value) * Pow10(decimalPlaces), 0, MidpointRounding.AwayFromZero);
        var digits = ((long)scaled).ToString(CultureInfo.InvariantCulture);

        if (digits.Length > totalDigits)
        {
            throw new OverflowException(
                $"Value {value} does not fit in PIC S9({integerDigits})V9({decimalPlaces}).");
        }

        digits = digits.PadLeft(totalDigits, '0');
        var lead = digits[..^1];
        var last = digits[^1] - '0';
        var overpunch = isNegative ? NegativeOverpunch[last] : PositiveOverpunch[last];
        return lead + overpunch;
    }

    private static (char digit, bool isNegative) DecodeOverpunchChar(char c)
    {
        var posIndex = Array.IndexOf(PositiveOverpunch, c);
        if (posIndex >= 0)
        {
            return ((char)('0' + posIndex), false);
        }

        var negIndex = Array.IndexOf(NegativeOverpunch, c);
        if (negIndex >= 0)
        {
            return ((char)('0' + negIndex), true);
        }

        if (c is >= '0' and <= '9')
        {
            return (c, false);
        }

        throw new FormatException($"Unrecognized overpunch sign character '{c}'.");
    }

    private static decimal Pow10(int places)
    {
        decimal result = 1m;
        for (var i = 0; i < places; i++)
        {
            result *= 10m;
        }

        return result;
    }
}
