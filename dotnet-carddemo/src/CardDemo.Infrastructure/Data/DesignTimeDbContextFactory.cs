using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace CardDemo.Infrastructure.Data;

/// <summary>
/// Design-time factory for creating CardDemoDbContext instances during EF Core migrations.
/// Used by the EF Core CLI tools (dotnet ef migrations add, etc.).
/// </summary>
public class DesignTimeDbContextFactory : IDesignTimeDbContextFactory<CardDemoDbContext>
{
    public CardDemoDbContext CreateDbContext(string[] args)
    {
        var optionsBuilder = new DbContextOptionsBuilder<CardDemoDbContext>();
        // Connection string for design-time migration generation only.
        // Override via environment variable CARDDEMO_CONNECTION_STRING if needed.
        var connectionString = Environment.GetEnvironmentVariable("CARDDEMO_CONNECTION_STRING")
            ?? "Host=localhost;Database=carddemo_design;Username=carddemo;Password=REPLACE_WITH_YOUR_PASSWORD";
        optionsBuilder.UseNpgsql(connectionString);

        return new CardDemoDbContext(optionsBuilder.Options);
    }
}
