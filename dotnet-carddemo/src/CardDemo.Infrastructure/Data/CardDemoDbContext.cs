using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Data;

/// <summary>
/// Entity Framework Core DbContext for the CardDemo application.
/// Provides access to all entity sets and applies entity configurations.
/// </summary>
public class CardDemoDbContext : DbContext
{
    public CardDemoDbContext(DbContextOptions<CardDemoDbContext> options)
        : base(options)
    {
    }

    public DbSet<User> Users => Set<User>();
    public DbSet<Account> Accounts => Set<Account>();
    public DbSet<Customer> Customers => Set<Customer>();
    public DbSet<CardData> Cards => Set<CardData>();
    public DbSet<CardCrossReference> CardCrossReferences => Set<CardCrossReference>();
    public DbSet<Transaction> Transactions => Set<Transaction>();
    public DbSet<TransactionType> TransactionTypes => Set<TransactionType>();
    public DbSet<DailyTransaction> DailyTransactions => Set<DailyTransaction>();
    public DbSet<DailyTransactionReject> DailyTransactionRejects => Set<DailyTransactionReject>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);
        modelBuilder.ApplyConfigurationsFromAssembly(typeof(CardDemoDbContext).Assembly);
        DataSeeder.Seed(modelBuilder);
    }
}
