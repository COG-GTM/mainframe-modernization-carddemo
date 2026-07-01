using CardDemo.Batch.Cbact01c;
using CardDemo.Batch.Cbact01c.Configuration;
using CardDemo.Batch.Cbact01c.Data;
using CardDemo.Batch.Cbact01c.Etl;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

var configuration = new ConfigurationBuilder()
    .SetBasePath(AppContext.BaseDirectory)
    .AddJsonFile("appsettings.json", optional: true)
    .AddEnvironmentVariables("CARDDEMO_")
    .Build();

var options = new BatchOptions();
configuration.GetSection("Batch").Bind(options);
options.ConnectionString = configuration["Batch:ConnectionString"]
    ?? configuration.GetConnectionString("Account")
    ?? options.ConnectionString;

using var loggerFactory = LoggerFactory.Create(builder =>
    builder.AddSimpleConsole(o =>
    {
        o.SingleLine = true;
        o.TimestampFormat = "yyyy-MM-dd HH:mm:ss ";
    }));

var logger = loggerFactory.CreateLogger<AccountBatchProcessor>();

if (string.IsNullOrWhiteSpace(options.ConnectionString))
{
    logger.LogError(
        "No database connection string configured. Set Batch:ConnectionString in "
        + "appsettings.json or the CARDDEMO_Batch__ConnectionString environment variable.");
    return 12;
}

// Sub-command "load <path>" performs the one-time ETL of the legacy flat file.
if (args.Length >= 1 && string.Equals(args[0], "load", StringComparison.OrdinalIgnoreCase))
{
    if (args.Length < 2)
    {
        logger.LogError("Usage: CardDemo.Batch.Cbact01c load <path-to-account-file>");
        return 12;
    }

    var loader = new AccountDatabaseLoader(options.ConnectionString);
    var records = AccountFileLoader.ReadRecords(args[1]);
    var inserted = await loader.LoadAsync(records);
    var total = await loader.CountAsync();
    logger.LogInformation("ETL complete: inserted {Inserted} record(s); table now holds {Total}", inserted, total);
    return 0;
}

// Default: the CBACT01C batch report.
var repository = new PostgresAccountRepository(options.ConnectionString);
var processor = new AccountBatchProcessor(repository, Console.Out, logger);

return await processor.RunAsync();
