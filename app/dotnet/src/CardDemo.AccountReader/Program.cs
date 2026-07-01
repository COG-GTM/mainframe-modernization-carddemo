using CardDemo.Data.Database;
using CardDemo.Data.Display;
using CardDemo.Data.Repositories;
using Microsoft.Extensions.Configuration;

// Modernized replacement for the batch COBOL program CBACT01C.
// Reads every account record (previously from the ACCTFILE VSAM KSDS, now from
// the relational 'account' table) in key order and prints each one.

const string ProgramName = "CBACT01C";

Console.WriteLine($"START OF EXECUTION OF PROGRAM {ProgramName}");

try
{
    var configuration = new ConfigurationBuilder()
        .SetBasePath(AppContext.BaseDirectory)
        .AddJsonFile("appsettings.json", optional: false)
        .AddEnvironmentVariables(prefix: "CARDDEMO_")
        .Build();

    var options = DatabaseOptionsFactory.Create(configuration);
    IAccountRepository repository = new AccountRepository(options);

    foreach (var account in repository.ReadAll())
    {
        Console.WriteLine(RecordFormatter.FormatAccount(account));
    }
}
catch (Exception ex)
{
    // Equivalent to the COBOL error path (9910/9999 ABEND) — report and abort.
    Console.Error.WriteLine("ERROR READING ACCOUNT FILE");
    Console.Error.WriteLine(ex.Message);
    Console.WriteLine("ABENDING PROGRAM");
    return 12;
}

Console.WriteLine($"END OF EXECUTION OF PROGRAM {ProgramName}");
return 0;
