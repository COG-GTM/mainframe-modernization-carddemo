namespace CardDemo.Data.Database;

/// <summary>
/// Connection settings for the modernized CardDemo relational store.
/// Populated from appsettings.json / environment variables.
/// </summary>
public sealed class DatabaseOptions
{
    public const string SectionName = "Database";

    /// <summary>Npgsql connection string for the target PostgreSQL instance.</summary>
    public string ConnectionString { get; set; } = string.Empty;
}
