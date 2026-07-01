using CardDemo.Batch.Cbact01c.Data;
using CardDemo.Batch.Cbact01c.Formatting;
using Microsoft.Extensions.Logging;

namespace CardDemo.Batch.Cbact01c;

/// <summary>
/// Direct port of CBACT01C's PROCEDURE DIVISION: opens the account source, reads every
/// record in key order, prints each one via <see cref="AccountFormatter"/>, then closes.
/// Any read failure is surfaced as an exception so the caller can exit non-zero,
/// mirroring the original CEE3ABD ABEND.
/// </summary>
public sealed class AccountBatchProcessor
{
    private readonly IAccountRepository _repository;
    private readonly TextWriter _output;
    private readonly ILogger<AccountBatchProcessor> _logger;

    public AccountBatchProcessor(
        IAccountRepository repository,
        TextWriter output,
        ILogger<AccountBatchProcessor> logger)
    {
        _repository = repository ?? throw new ArgumentNullException(nameof(repository));
        _output = output ?? throw new ArgumentNullException(nameof(output));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    public async Task<int> RunAsync(CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("START OF EXECUTION OF PROGRAM CBACT01C");

        var count = 0;
        try
        {
            await foreach (var account in _repository
                               .StreamAllOrderedByIdAsync(cancellationToken)
                               .ConfigureAwait(false))
            {
                foreach (var line in AccountFormatter.FormatLines(account))
                {
                    await _output.WriteLineAsync(line).ConfigureAwait(false);
                }

                count++;
            }
        }
        catch (Exception ex)
        {
            // Equivalent to '9999-ABEND-PROGRAM' after 'ERROR READING ACCOUNT FILE'.
            _logger.LogError(ex, "ERROR READING ACCOUNT FILE");
            _logger.LogInformation("END OF EXECUTION OF PROGRAM CBACT01C (ABEND)");
            return 12;
        }

        _logger.LogInformation("Read {Count} account record(s)", count);
        _logger.LogInformation("END OF EXECUTION OF PROGRAM CBACT01C");
        return 0;
    }
}
