using CardDemo.Data.Database;
using CardDemo.Data.Parsing;
using CardDemo.Data.Repositories;
using Microsoft.Extensions.Configuration;

// One-time / repeatable data migration utility.
// Loads the CardDemo account and card flat files (ASCII, fixed-width, as
// produced from the mainframe VSAM/PS datasets) into the relational store.
//
// Usage:
//   CardDemo.DataMigration [--init-schema] [--accounts <file>] [--cards <file>]
//                          [--connection <npgsql-conn-string>]
//
// The connection string defaults to appsettings.json ("Database:ConnectionString")
// or the CARDDEMO_Database__ConnectionString environment variable, and can be
// overridden with --connection.

var parsed = CommandLine.Parse(args);
if (parsed.ShowHelp)
{
    CommandLine.PrintUsage();
    return 0;
}

try
{
    var configuration = new ConfigurationBuilder()
        .SetBasePath(AppContext.BaseDirectory)
        .AddJsonFile("appsettings.json", optional: true)
        .AddEnvironmentVariables(prefix: "CARDDEMO_")
        .Build();

    var options = parsed.ConnectionString is { Length: > 0 }
        ? new DatabaseOptions { ConnectionString = parsed.ConnectionString }
        : DatabaseOptionsFactory.Create(configuration);

    if (parsed.InitSchema)
    {
        Console.WriteLine("Ensuring database schema...");
        DatabaseInitializer.EnsureSchema(options);
        Console.WriteLine("Schema ready.");
    }

    if (parsed.AccountsFile is { Length: > 0 })
    {
        var count = LoadAccounts(parsed.AccountsFile, new AccountRepository(options));
        Console.WriteLine($"Loaded {count} account record(s) from {parsed.AccountsFile}");
    }

    if (parsed.CardsFile is { Length: > 0 })
    {
        var count = LoadCards(parsed.CardsFile, new CardRepository(options));
        Console.WriteLine($"Loaded {count} card record(s) from {parsed.CardsFile}");
    }

    if (parsed is { InitSchema: false, AccountsFile: null, CardsFile: null })
    {
        Console.Error.WriteLine("Nothing to do. Specify --init-schema, --accounts and/or --cards.");
        CommandLine.PrintUsage();
        return 1;
    }
}
catch (Exception ex)
{
    Console.Error.WriteLine($"Migration failed: {ex.Message}");
    return 12;
}

return 0;

static int LoadAccounts(string path, IAccountRepository repository)
{
    var count = 0;
    foreach (var line in ReadRecords(path))
    {
        repository.Upsert(AccountRecordParser.Parse(line));
        count++;
    }

    return count;
}

static int LoadCards(string path, ICardRepository repository)
{
    var count = 0;
    foreach (var line in ReadRecords(path))
    {
        repository.Upsert(CardRecordParser.Parse(line));
        count++;
    }

    return count;
}

static IEnumerable<string> ReadRecords(string path)
{
    if (!File.Exists(path))
    {
        throw new FileNotFoundException($"Input file not found: {path}", path);
    }

    foreach (var line in File.ReadLines(path))
    {
        if (!string.IsNullOrWhiteSpace(line))
        {
            yield return line;
        }
    }
}

internal sealed record MigrationArgs(
    bool InitSchema,
    string? AccountsFile,
    string? CardsFile,
    string? ConnectionString,
    bool ShowHelp);

internal static class CommandLine
{
    public static MigrationArgs Parse(string[] args)
    {
        var initSchema = false;
        string? accounts = null;
        string? cards = null;
        string? connection = null;
        var help = false;

        for (var i = 0; i < args.Length; i++)
        {
            switch (args[i])
            {
                case "--init-schema":
                    initSchema = true;
                    break;
                case "--accounts":
                    accounts = RequireValue(args, ref i);
                    break;
                case "--cards":
                    cards = RequireValue(args, ref i);
                    break;
                case "--connection":
                    connection = RequireValue(args, ref i);
                    break;
                case "-h":
                case "--help":
                    help = true;
                    break;
                default:
                    throw new ArgumentException($"Unknown argument: {args[i]}");
            }
        }

        return new MigrationArgs(initSchema, accounts, cards, connection, help);
    }

    private static string RequireValue(string[] args, ref int i)
    {
        if (i + 1 >= args.Length)
        {
            throw new ArgumentException($"Missing value for {args[i]}");
        }

        return args[++i];
    }

    public static void PrintUsage()
    {
        Console.WriteLine(
            "Usage: CardDemo.DataMigration [--init-schema] [--accounts <file>] " +
            "[--cards <file>] [--connection <npgsql-conn-string>]");
    }
}
