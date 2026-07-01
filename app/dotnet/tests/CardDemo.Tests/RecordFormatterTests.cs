using CardDemo.Data.Display;
using CardDemo.Domain;
using Xunit;

namespace CardDemo.Tests;

public class RecordFormatterTests
{
    [Fact]
    public void FormatAccount_MatchesCobolLabelLayout()
    {
        var account = new AccountRecord
        {
            AccountId = 1,
            ActiveStatus = "Y",
            CurrentBalance = 194.00m,
            CreditLimit = 2020.00m,
            CashCreditLimit = 1020.00m,
            OpenDate = "2014-11-20",
            ExpirationDate = "2025-05-20",
            ReissueDate = "2025-05-20",
            CurrentCycleCredit = 0m,
            CurrentCycleDebit = 0m,
            GroupId = string.Empty,
        };

        var output = RecordFormatter.FormatAccount(account);

        Assert.Contains("ACCT-ID                 :00000000001", output);
        Assert.Contains("ACCT-CURR-BAL           :194.00", output);
        Assert.Contains("ACCT-CASH-CREDIT-LIMIT  :1020.00", output);
        Assert.Contains("-------------------------------------------------", output);
    }

    [Fact]
    public void FormatCard_RendersPaddedNumericFields()
    {
        var card = new CardRecord
        {
            CardNumber = "0500024453765740",
            AccountId = 50,
            CvvCode = 747,
            EmbossedName = "Aniya Von",
            ExpirationDate = "2023-03-09",
            ActiveStatus = "Y",
        };

        var output = RecordFormatter.FormatCard(card);

        Assert.Contains("CARD-NUM                :0500024453765740", output);
        Assert.Contains("CARD-ACCT-ID            :00000000050", output);
        Assert.Contains("CARD-CVV-CD             :747", output);
    }
}
