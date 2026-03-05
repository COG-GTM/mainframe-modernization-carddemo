using CardDemo.Core.Entities;
using CardDemo.Infrastructure.Data;
using CardDemo.Infrastructure.Repositories;
using FluentAssertions;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Tests;

public class RepositoryTests
{
    private static CardDemoDbContext CreateContext(string dbName)
    {
        var options = new DbContextOptionsBuilder<CardDemoDbContext>()
            .UseInMemoryDatabase(databaseName: dbName)
            .Options;

        return new CardDemoDbContext(options);
    }

    #region UserRepository Tests

    [Fact]
    public async Task UserRepository_GetByIdAsync_ReturnsUser()
    {
        using var context = CreateContext(nameof(UserRepository_GetByIdAsync_ReturnsUser));
        await context.Database.EnsureCreatedAsync();
        var repo = new UserRepository(context);

        var user = await repo.GetByIdAsync("ADMIN001");

        user.Should().NotBeNull();
        user!.UserType.Should().Be("A");
    }

    [Fact]
    public async Task UserRepository_GetByIdAsync_ReturnsNull_WhenNotFound()
    {
        using var context = CreateContext(nameof(UserRepository_GetByIdAsync_ReturnsNull_WhenNotFound));
        await context.Database.EnsureCreatedAsync();
        var repo = new UserRepository(context);

        var user = await repo.GetByIdAsync("NONEXIST");

        user.Should().BeNull();
    }

    [Fact]
    public async Task UserRepository_GetUsersAsync_WithPrefix_FiltersCorrectly()
    {
        using var context = CreateContext(nameof(UserRepository_GetUsersAsync_WithPrefix_FiltersCorrectly));
        await context.Database.EnsureCreatedAsync();
        var repo = new UserRepository(context);

        var (users, totalCount) = await repo.GetUsersAsync("ADMIN", 1, 10);

        users.Should().HaveCount(1);
        totalCount.Should().Be(1);
        users[0].UserId.Should().Be("ADMIN001");
    }

    [Fact]
    public async Task UserRepository_GetUsersAsync_WithPagination_ReturnsCorrectPage()
    {
        using var context = CreateContext(nameof(UserRepository_GetUsersAsync_WithPagination_ReturnsCorrectPage));
        await context.Database.EnsureCreatedAsync();
        var repo = new UserRepository(context);

        var (users, totalCount) = await repo.GetUsersAsync(null, 1, 1);

        users.Should().HaveCount(1);
        totalCount.Should().Be(2);
    }

    [Fact]
    public async Task UserRepository_AddAsync_PersistsUser()
    {
        using var context = CreateContext(nameof(UserRepository_AddAsync_PersistsUser));
        await context.Database.EnsureCreatedAsync();
        var repo = new UserRepository(context);

        var newUser = new User
        {
            UserId = "NEWUSER1",
            FirstName = "New",
            LastName = "User",
            Password = "NEWPASS1",
            UserType = "U"
        };
        await repo.AddAsync(newUser);

        var retrieved = await context.Users.FindAsync("NEWUSER1");
        retrieved.Should().NotBeNull();
        retrieved!.FirstName.Should().Be("New");
    }

    [Fact]
    public async Task UserRepository_UpdateAsync_ModifiesUser()
    {
        using var context = CreateContext(nameof(UserRepository_UpdateAsync_ModifiesUser));
        await context.Database.EnsureCreatedAsync();
        var repo = new UserRepository(context);

        var user = await context.Users.FindAsync("USER0001");
        user!.FirstName = "Updated";
        await repo.UpdateAsync(user);

        var retrieved = await context.Users.FindAsync("USER0001");
        retrieved!.FirstName.Should().Be("Updated");
    }

    [Fact]
    public async Task UserRepository_DeleteAsync_RemovesUser()
    {
        using var context = CreateContext(nameof(UserRepository_DeleteAsync_RemovesUser));
        await context.Database.EnsureCreatedAsync();
        var repo = new UserRepository(context);

        await repo.DeleteAsync("USER0001");

        var retrieved = await context.Users.FindAsync("USER0001");
        retrieved.Should().BeNull();
    }

    #endregion

    #region AccountRepository Tests

    [Fact]
    public async Task AccountRepository_GetByIdAsync_ReturnsAccountWithRelations()
    {
        using var context = CreateContext(nameof(AccountRepository_GetByIdAsync_ReturnsAccountWithRelations));
        await context.Database.EnsureCreatedAsync();
        var repo = new AccountRepository(context);

        var account = await repo.GetByIdAsync(80000000001);

        account.Should().NotBeNull();
        account!.AccountStatus.Should().Be("Y");
        account.Cards.Should().NotBeEmpty();
        account.CrossReferences.Should().NotBeEmpty();
    }

    #endregion

    #region CustomerRepository Tests

    [Fact]
    public async Task CustomerRepository_GetByIdAsync_ReturnsCustomerWithRelations()
    {
        using var context = CreateContext(nameof(CustomerRepository_GetByIdAsync_ReturnsCustomerWithRelations));
        await context.Database.EnsureCreatedAsync();
        var repo = new CustomerRepository(context);

        var customer = await repo.GetByIdAsync(100000001);

        customer.Should().NotBeNull();
        customer!.FirstName.Should().Be("John");
        customer.CrossReferences.Should().NotBeEmpty();
    }

    #endregion

    #region CardDataRepository Tests

    [Fact]
    public async Task CardDataRepository_GetByCardNumberAsync_ReturnsCardWithAccount()
    {
        using var context = CreateContext(nameof(CardDataRepository_GetByCardNumberAsync_ReturnsCardWithAccount));
        await context.Database.EnsureCreatedAsync();
        var repo = new CardDataRepository(context);

        var card = await repo.GetByCardNumberAsync("4111111111111111");

        card.Should().NotBeNull();
        card!.Account.Should().NotBeNull();
        card.AccountId.Should().Be(80000000001);
    }

    [Fact]
    public async Task CardDataRepository_GetByAccountIdAsync_ReturnsCards()
    {
        using var context = CreateContext(nameof(CardDataRepository_GetByAccountIdAsync_ReturnsCards));
        await context.Database.EnsureCreatedAsync();
        var repo = new CardDataRepository(context);

        var cards = await repo.GetByAccountIdAsync(80000000001);

        cards.Should().HaveCount(1);
        cards[0].CardNumber.Should().Be("4111111111111111");
    }

    #endregion

    #region CardCrossReferenceRepository Tests

    [Fact]
    public async Task CardCrossReferenceRepository_GetByCardNumberAsync_ReturnsWithRelations()
    {
        using var context = CreateContext(nameof(CardCrossReferenceRepository_GetByCardNumberAsync_ReturnsWithRelations));
        await context.Database.EnsureCreatedAsync();
        var repo = new CardCrossReferenceRepository(context);

        var xref = await repo.GetByCardNumberAsync("4111111111111111");

        xref.Should().NotBeNull();
        xref!.Account.Should().NotBeNull();
        xref.Customer.Should().NotBeNull();
        xref.AccountId.Should().Be(80000000001);
        xref.CustomerId.Should().Be(100000001);
    }

    [Fact]
    public async Task CardCrossReferenceRepository_GetByAccountIdAsync_ReturnsXrefs()
    {
        using var context = CreateContext(nameof(CardCrossReferenceRepository_GetByAccountIdAsync_ReturnsXrefs));
        await context.Database.EnsureCreatedAsync();
        var repo = new CardCrossReferenceRepository(context);

        var xrefs = await repo.GetByAccountIdAsync(80000000001);

        xrefs.Should().HaveCount(1);
    }

    [Fact]
    public async Task CardCrossReferenceRepository_GetByCustomerIdAsync_ReturnsXrefs()
    {
        using var context = CreateContext(nameof(CardCrossReferenceRepository_GetByCustomerIdAsync_ReturnsXrefs));
        await context.Database.EnsureCreatedAsync();
        var repo = new CardCrossReferenceRepository(context);

        var xrefs = await repo.GetByCustomerIdAsync(100000001);

        xrefs.Should().HaveCount(1);
    }

    #endregion

    #region TransactionRepository Tests

    [Fact]
    public async Task TransactionRepository_GetByCardNumberAsync_ReturnsPaginated()
    {
        using var context = CreateContext(nameof(TransactionRepository_GetByCardNumberAsync_ReturnsPaginated));
        await context.Database.EnsureCreatedAsync();
        var repo = new TransactionRepository(context);

        var (transactions, totalCount) = await repo.GetByCardNumberAsync("4111111111111111", 1, 10);

        transactions.Should().HaveCount(1);
        totalCount.Should().Be(1);
    }

    [Fact]
    public async Task TransactionRepository_GetByCardNumberAsync_ReturnsEmpty_WhenNoMatch()
    {
        using var context = CreateContext(nameof(TransactionRepository_GetByCardNumberAsync_ReturnsEmpty_WhenNoMatch));
        await context.Database.EnsureCreatedAsync();
        var repo = new TransactionRepository(context);

        var (transactions, totalCount) = await repo.GetByCardNumberAsync("0000000000000000", 1, 10);

        transactions.Should().BeEmpty();
        totalCount.Should().Be(0);
    }

    #endregion

    #region TransactionTypeRepository Tests

    [Fact]
    public async Task TransactionTypeRepository_GetAllAsync_ReturnsAllTypes()
    {
        using var context = CreateContext(nameof(TransactionTypeRepository_GetAllAsync_ReturnsAllTypes));
        await context.Database.EnsureCreatedAsync();
        var repo = new TransactionTypeRepository(context);

        var types = await repo.GetAllAsync();

        types.Should().HaveCount(5);
        types[0].TypeCode.Should().Be("01"); // Ordered by TypeCode
    }

    [Fact]
    public async Task TransactionTypeRepository_GetByCodeAsync_ReturnsType()
    {
        using var context = CreateContext(nameof(TransactionTypeRepository_GetByCodeAsync_ReturnsType));
        await context.Database.EnsureCreatedAsync();
        var repo = new TransactionTypeRepository(context);

        var type = await repo.GetByCodeAsync("01");

        type.Should().NotBeNull();
        type!.TypeDescription.Should().Be("Purchase");
    }

    [Fact]
    public async Task TransactionTypeRepository_GetByCodeAsync_ReturnsNull_WhenNotFound()
    {
        using var context = CreateContext(nameof(TransactionTypeRepository_GetByCodeAsync_ReturnsNull_WhenNotFound));
        await context.Database.EnsureCreatedAsync();
        var repo = new TransactionTypeRepository(context);

        var type = await repo.GetByCodeAsync("ZZ");

        type.Should().BeNull();
    }

    #endregion
}
