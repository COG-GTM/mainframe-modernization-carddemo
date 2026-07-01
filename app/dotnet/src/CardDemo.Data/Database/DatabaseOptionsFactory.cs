using Microsoft.Extensions.Configuration;

namespace CardDemo.Data.Database;

/// <summary>
/// Builds <see cref="DatabaseOptions"/> from application configuration.
/// The connection string is read from the "Database:ConnectionString" key
/// (appsettings.json), and may be overridden via the environment variable
/// CARDDEMO_Database__ConnectionString.
/// </summary>
public static class DatabaseOptionsFactory
{
    public static DatabaseOptions Create(IConfiguration configuration)
    {
        ArgumentNullException.ThrowIfNull(configuration);

        var connectionString = configuration[$"{DatabaseOptions.SectionName}:ConnectionString"];
        if (string.IsNullOrWhiteSpace(connectionString))
        {
            throw new InvalidOperationException(
                "Missing database connection string. Set 'Database:ConnectionString' in " +
                "appsettings.json or the CARDDEMO_Database__ConnectionString environment variable.");
        }

        return new DatabaseOptions { ConnectionString = connectionString };
    }
}
