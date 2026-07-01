using System.Runtime.CompilerServices;
using System.Text;
using CardDemo.Batch.Cbact01c;
using CardDemo.Batch.Cbact01c.Data;
using CardDemo.Batch.Cbact01c.Domain;
using Microsoft.Extensions.Logging.Abstractions;

namespace CardDemo.Batch.Cbact01c.Tests;

public class AccountBatchProcessorTests
{
    [Fact]
    public async Task RunAsync_PrintsEveryRecord_AndReturnsZero()
    {
        var accounts = new[]
        {
            NewAccount(1),
            NewAccount(2)
        };
        var output = new StringWriter();
        var processor = new AccountBatchProcessor(
            new FakeRepository(accounts), output, NullLogger<AccountBatchProcessor>.Instance);

        var exitCode = await processor.RunAsync();

        Assert.Equal(0, exitCode);
        var text = output.ToString();
        Assert.Contains("ACCT-ID                 :00000000001", text);
        Assert.Contains("ACCT-ID                 :00000000002", text);
        Assert.Equal(2, CountOccurrences(text, "-------------------------------------------------"));
    }

    [Fact]
    public async Task RunAsync_OnReadError_ReturnsNonZero()
    {
        var processor = new AccountBatchProcessor(
            new ThrowingRepository(), new StringWriter(), NullLogger<AccountBatchProcessor>.Instance);

        var exitCode = await processor.RunAsync();

        Assert.NotEqual(0, exitCode);
    }

    private static AccountRecord NewAccount(long id) => new()
    {
        AccountId = id,
        ActiveStatus = "Y",
        CurrentBalance = 19.40m,
        CreditLimit = 202.00m,
        CashCreditLimit = 102.00m,
        OpenDate = "2014-11-20",
        ExpirationDate = "2025-05-20",
        ReissueDate = "2025-05-20",
        CurrentCycleCredit = 0m,
        CurrentCycleDebit = 0m,
        AddressZip = "A000000000",
        GroupId = string.Empty
    };

    private static int CountOccurrences(string haystack, string needle)
    {
        var count = 0;
        var index = 0;
        while ((index = haystack.IndexOf(needle, index, StringComparison.Ordinal)) >= 0)
        {
            count++;
            index += needle.Length;
        }

        return count;
    }

    private sealed class FakeRepository : IAccountRepository
    {
        private readonly IReadOnlyList<AccountRecord> _accounts;

        public FakeRepository(IReadOnlyList<AccountRecord> accounts) => _accounts = accounts;

        public async IAsyncEnumerable<AccountRecord> StreamAllOrderedByIdAsync(
            [EnumeratorCancellation] CancellationToken cancellationToken = default)
        {
            foreach (var account in _accounts)
            {
                cancellationToken.ThrowIfCancellationRequested();
                yield return account;
                await Task.Yield();
            }
        }
    }

    private sealed class ThrowingRepository : IAccountRepository
    {
        public async IAsyncEnumerable<AccountRecord> StreamAllOrderedByIdAsync(
            [EnumeratorCancellation] CancellationToken cancellationToken = default)
        {
            await Task.Yield();
            throw new InvalidOperationException("simulated read failure");
#pragma warning disable CS0162 // Unreachable code required to satisfy iterator signature.
            yield break;
#pragma warning restore CS0162
        }
    }
}
