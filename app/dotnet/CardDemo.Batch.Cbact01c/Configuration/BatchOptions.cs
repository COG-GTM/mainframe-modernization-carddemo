namespace CardDemo.Batch.Cbact01c.Configuration;

/// <summary>
/// Runtime configuration, replacing the DD statements / dataset names of READACCT.jcl.
/// Bound from appsettings.json and environment variables.
/// </summary>
public sealed class BatchOptions
{
    /// <summary>PostgreSQL connection string for the account database.</summary>
    public string ConnectionString { get; set; } = string.Empty;
}
