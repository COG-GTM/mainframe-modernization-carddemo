using CardDemo.Infrastructure.Data;
using FluentAssertions;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Tests;

public class DbContextTests
{
    private static CardDemoDbContext CreateContext(string dbName)
    {
        var options = new DbContextOptionsBuilder<CardDemoDbContext>()
            .UseInMemoryDatabase(databaseName: dbName)
            .Options;

        return new CardDemoDbContext(options);
    }

    [Fact]
    public async Task DbContext_CanBeCreated_AndModelIsValid()
    {
        // Arrange & Act
        using var context = CreateContext(nameof(DbContext_CanBeCreated_AndModelIsValid));
        var created = await context.Database.EnsureCreatedAsync();

        // Assert - no exception means the model is valid
        created.Should().BeTrue();
    }

    [Fact]
    public async Task DbContext_SeedData_IsApplied()
    {
        // Arrange
        using var context = CreateContext(nameof(DbContext_SeedData_IsApplied));
        await context.Database.EnsureCreatedAsync();

        // Assert - verify seed data exists
        var users = await context.Users.ToListAsync();
        users.Should().HaveCount(2);
        users.Should().Contain(u => u.UserId == "ADMIN001" && u.UserType == "A");
        users.Should().Contain(u => u.UserId == "USER0001" && u.UserType == "U");

        var customers = await context.Customers.ToListAsync();
        customers.Should().HaveCount(3);

        var accounts = await context.Accounts.ToListAsync();
        accounts.Should().HaveCount(3);

        var cards = await context.Cards.ToListAsync();
        cards.Should().HaveCount(3);

        var crossRefs = await context.CardCrossReferences.ToListAsync();
        crossRefs.Should().HaveCount(3);

        var transactionTypes = await context.TransactionTypes.ToListAsync();
        transactionTypes.Should().HaveCount(5);

        var transactions = await context.Transactions.ToListAsync();
        transactions.Should().HaveCount(3);
    }

    [Fact]
    public async Task DbContext_AllDbSets_AreAccessible()
    {
        // Arrange
        using var context = CreateContext(nameof(DbContext_AllDbSets_AreAccessible));
        await context.Database.EnsureCreatedAsync();

        // Act & Assert - verify all DbSets are accessible
        context.Users.Should().NotBeNull();
        context.Accounts.Should().NotBeNull();
        context.Customers.Should().NotBeNull();
        context.Cards.Should().NotBeNull();
        context.CardCrossReferences.Should().NotBeNull();
        context.Transactions.Should().NotBeNull();
        context.TransactionTypes.Should().NotBeNull();
        context.DailyTransactions.Should().NotBeNull();
        context.DailyTransactionRejects.Should().NotBeNull();
    }
}
