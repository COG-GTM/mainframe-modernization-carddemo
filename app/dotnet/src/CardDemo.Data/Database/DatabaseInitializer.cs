using Dapper;
using Npgsql;

namespace CardDemo.Data.Database;

/// <summary>Creates the CardDemo schema if it does not already exist.</summary>
public static class DatabaseInitializer
{
    public static void EnsureSchema(DatabaseOptions options)
    {
        ArgumentNullException.ThrowIfNull(options);
        using var connection = new NpgsqlConnection(options.ConnectionString);
        connection.Open();
        connection.Execute(SchemaScript.CreateTables);
    }
}
