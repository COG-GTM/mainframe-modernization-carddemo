using System.Globalization;

namespace CardDemo.Data.Cobol;

/// <summary>
/// Converts between COBOL zoned-decimal (USAGE DISPLAY) fields with a
/// trailing sign "overpunch" and modern <see cref="decimal"/> values.
///
/// A COBOL PIC S9(n)V(m) DISPLAY field stores the value as ASCII/EBCDIC
/// digits where the units position (last byte) carries the sign as an
/// overpunch character:
///   positive 0-9 => { A B C D E F G H I
///   negative 0-9 => } J K L M N O P Q R
/// The implied decimal point (V) is positional and not stored in the data.
/// </summary>
public static class ZonedDecimal
{
    // Maps an overpunch character to (digit, isNegative).
    private static readonly Dictionary<char, (int Digit, bool Negative)> OverpunchToDigit = new()
    {
        ['{'] = (0, false),
        ['A'] = (1, false),
        ['B'] = (2, false),
        ['C'] = (3, false),
        ['D'] = (4, false),
        ['E'] = (5, false),
        ['F'] = (6, false),
        ['G'] = (7, false),
        ['H'] = (8, false),
        ['I'] = (9, false),
        ['}'] = (0, true),
        ['J'] = (1, true),
        ['K'] = (2, true),
        ['L'] = (3, true),
        ['M'] = (4, true),
        ['N'] = (5, true),
        ['O'] = (6, true),
        ['P'] = (7, true),
        ['Q'] = (8, true),
        ['R'] = (9, true),
    };

    private static readonly char[] PositiveOverpunch = { '{', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I' };
    private static readonly char[] NegativeOverpunch = { '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R' };

    /// <summary>
    /// Decodes a signed zoned-decimal field into a <see cref="decimal"/>.
    /// </summary>
    /// <param name="field">Raw field content, e.g. "0000001940{".</param>
    /// <param name="decimalPlaces">Number of implied decimal places (the V position).</param>
    public static decimal DecodeSigned(string field, int decimalPlaces)
    {
        ArgumentNullException.ThrowIfNull(field);

        if (field.Length == 0)
        {
            throw new FormatException("Zoned-decimal field is empty.");
        }

        var trimmed = field.Trim();
        if (trimmed.Length == 0)
        {
            return 0m;
        }

        var digits = new char[trimmed.Length];
        var negative = false;

        for (var i = 0; i < trimmed.Length; i++)
        {
            var c = trimmed[i];
            var isLast = i == trimmed.Length - 1;

            if (char.IsDigit(c))
            {
                digits[i] = c;
                continue;
            }

            if (isLast && OverpunchToDigit.TryGetValue(char.ToUpperInvariant(c), out var mapped))
            {
                digits[i] = (char)('0' + mapped.Digit);
                negative = mapped.Negative;
                continue;
            }

            throw new FormatException(
                $"Invalid character '{c}' at position {i} in zoned-decimal field '{field}'.");
        }

        var raw = decimal.Parse(new string(digits), CultureInfo.InvariantCulture);
        if (decimalPlaces > 0)
        {
            raw /= Pow10(decimalPlaces);
        }

        return negative ? -raw : raw;
    }

    /// <summary>
    /// Encodes a <see cref="decimal"/> back into a fixed-width signed
    /// zoned-decimal field (inverse of <see cref="DecodeSigned"/>), which is
    /// useful for regenerating mainframe-compatible flat files.
    /// </summary>
    /// <param name="value">Value to encode.</param>
    /// <param name="totalDigits">Total number of digit positions (n + m).</param>
    /// <param name="decimalPlaces">Number of implied decimal places (m).</param>
    public static string EncodeSigned(decimal value, int totalDigits, int decimalPlaces)
    {
        if (totalDigits <= 0)
        {
            throw new ArgumentOutOfRangeException(nameof(totalDigits));
        }

        var negative = value < 0m;
        var scaled = decimal.Round(Math.Abs(value) * Pow10(decimalPlaces), 0, MidpointRounding.AwayFromZero);
        var unsigned = ((long)scaled).ToString(CultureInfo.InvariantCulture);

        if (unsigned.Length > totalDigits)
        {
            throw new OverflowException(
                $"Value {value} does not fit in {totalDigits} digits.");
        }

        unsigned = unsigned.PadLeft(totalDigits, '0');

        var lastDigit = unsigned[^1] - '0';
        var overpunch = negative ? NegativeOverpunch[lastDigit] : PositiveOverpunch[lastDigit];
        return unsigned[..^1] + overpunch;
    }

    private static decimal Pow10(int exponent)
    {
        decimal result = 1m;
        for (var i = 0; i < exponent; i++)
        {
            result *= 10m;
        }

        return result;
    }
}
