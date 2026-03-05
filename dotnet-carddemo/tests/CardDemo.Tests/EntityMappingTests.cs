using CardDemo.Core.Entities;
using CardDemo.Infrastructure.Data;
using FluentAssertions;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Tests;

public class EntityMappingTests
{
    private static CardDemoDbContext CreateContext(string dbName)
    {
        var options = new DbContextOptionsBuilder<CardDemoDbContext>()
            .UseInMemoryDatabase(databaseName: dbName)
            .Options;

        return new CardDemoDbContext(options);
    }

    [Fact]
    public async Task User_CanBePersisted_AndRetrieved()
    {
        using var context = CreateContext(nameof(User_CanBePersisted_AndRetrieved));
        await context.Database.EnsureCreatedAsync();

        var user = new User
        {
            UserId = "TESTUSER",
            FirstName = "Test",
            LastName = "User",
            Password = "PASS1234",
            UserType = "U"
        };

        context.Users.Add(user);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(User_CanBePersisted_AndRetrieved));
        var retrieved = await readContext.Users.FindAsync("TESTUSER");
        retrieved.Should().NotBeNull();
        retrieved!.FirstName.Should().Be("Test");
        retrieved.LastName.Should().Be("User");
        retrieved.UserType.Should().Be("U");
    }

    [Fact]
    public async Task Account_CanBePersisted_AndRetrieved()
    {
        using var context = CreateContext(nameof(Account_CanBePersisted_AndRetrieved));
        await context.Database.EnsureCreatedAsync();

        var account = new Account
        {
            AccountId = 99999999999,
            AccountStatus = "Y",
            CurrentBalance = 5000.50m,
            CreditLimit = 10000.00m,
            CashCreditLimit = 3000.00m,
            OpenDate = "2024-01-01",
            ExpirationDate = "2030-01-01",
            ReissueDate = "2028-01-01",
            CurrentCycleCredit = 100.00m,
            CurrentCycleDebit = 200.00m,
            AddressZip = "12345",
            GroupId = "GRPTEST"
        };

        context.Accounts.Add(account);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(Account_CanBePersisted_AndRetrieved));
        var retrieved = await readContext.Accounts.FindAsync(99999999999L);
        retrieved.Should().NotBeNull();
        retrieved!.CurrentBalance.Should().Be(5000.50m);
        retrieved.CreditLimit.Should().Be(10000.00m);
    }

    [Fact]
    public async Task Customer_CanBePersisted_AndRetrieved()
    {
        using var context = CreateContext(nameof(Customer_CanBePersisted_AndRetrieved));
        await context.Database.EnsureCreatedAsync();

        var customer = new Customer
        {
            CustomerId = 999999999,
            FirstName = "Test",
            MiddleName = "M",
            LastName = "Customer",
            AddressLine1 = "123 Test St",
            AddressLine2 = "",
            AddressLine3 = "",
            State = "TX",
            CountryCode = "USA",
            ZipCode = "75001",
            PhoneNumber1 = "555-0100",
            PhoneNumber2 = "",
            Ssn = "111223333",
            GovtIssuedId = "TX-DL-99999999",
            DateOfBirth = "1990-01-01",
            EftAccountId = "EFT9999999",
            PrimaryCardHolderIndicator = "Y",
            FicoScore = 700
        };

        context.Customers.Add(customer);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(Customer_CanBePersisted_AndRetrieved));
        var retrieved = await readContext.Customers.FindAsync(999999999L);
        retrieved.Should().NotBeNull();
        retrieved!.FirstName.Should().Be("Test");
        retrieved.FicoScore.Should().Be(700);
    }

    [Fact]
    public async Task CardData_CanBePersisted_WithAccountRelationship()
    {
        using var context = CreateContext(nameof(CardData_CanBePersisted_WithAccountRelationship));
        await context.Database.EnsureCreatedAsync();

        // Use seed data account
        var card = new CardData
        {
            CardNumber = "9999888877776666",
            AccountId = 80000000001, // Seed data account
            CvvCode = 999,
            EmbossedName = "TEST CARD",
            ExpirationDate = "2030-12-31",
            CardStatus = "Y"
        };

        context.Cards.Add(card);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(CardData_CanBePersisted_WithAccountRelationship));
        var retrieved = await readContext.Cards
            .Include(c => c.Account)
            .FirstOrDefaultAsync(c => c.CardNumber == "9999888877776666");
        retrieved.Should().NotBeNull();
        retrieved!.Account.Should().NotBeNull();
        retrieved.Account.AccountId.Should().Be(80000000001);
    }

    [Fact]
    public async Task CardCrossReference_CanBePersisted_WithRelationships()
    {
        using var context = CreateContext(nameof(CardCrossReference_CanBePersisted_WithRelationships));
        await context.Database.EnsureCreatedAsync();

        var xref = new CardCrossReference
        {
            CardNumber = "8888777766665555",
            AccountId = 80000000001, // Seed data account
            CustomerId = 100000001   // Seed data customer
        };

        context.CardCrossReferences.Add(xref);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(CardCrossReference_CanBePersisted_WithRelationships));
        var retrieved = await readContext.CardCrossReferences
            .Include(cr => cr.Account)
            .Include(cr => cr.Customer)
            .FirstOrDefaultAsync(cr => cr.CardNumber == "8888777766665555");
        retrieved.Should().NotBeNull();
        retrieved!.Account.Should().NotBeNull();
        retrieved.Customer.Should().NotBeNull();
    }

    [Fact]
    public async Task Transaction_CanBePersisted_AndRetrieved()
    {
        using var context = CreateContext(nameof(Transaction_CanBePersisted_AndRetrieved));
        await context.Database.EnsureCreatedAsync();

        var transaction = new Transaction
        {
            TransactionId = "TRN9999999999999",
            TransactionType = "01",
            TransactionCategory = 5411,
            TransactionSource = "ONLINE",
            TransactionDescription = "Test Purchase",
            TransactionAmount = 99.99m,
            MerchantId = "999999999",
            MerchantName = "Test Merchant",
            MerchantCity = "Test City",
            MerchantZip = "99999",
            CardNumber = "4111111111111111",
            OriginTimestamp = new DateTime(2025, 6, 1, 12, 0, 0, DateTimeKind.Utc),
            ProcessingTimestamp = new DateTime(2025, 6, 1, 12, 0, 5, DateTimeKind.Utc)
        };

        context.Transactions.Add(transaction);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(Transaction_CanBePersisted_AndRetrieved));
        var retrieved = await readContext.Transactions.FindAsync("TRN9999999999999");
        retrieved.Should().NotBeNull();
        retrieved!.TransactionAmount.Should().Be(99.99m);
    }

    [Fact]
    public async Task DailyTransaction_CanBePersisted_AndRetrieved()
    {
        using var context = CreateContext(nameof(DailyTransaction_CanBePersisted_AndRetrieved));
        await context.Database.EnsureCreatedAsync();

        var dailyTran = new DailyTransaction
        {
            TransactionId = "DTR0000000000001",
            TransactionType = "01",
            TransactionCategory = 5411,
            TransactionSource = "BATCH",
            TransactionDescription = "Daily batch transaction",
            TransactionAmount = 50.00m,
            MerchantId = "100000001",
            MerchantName = "Batch Merchant",
            MerchantCity = "Batch City",
            MerchantZip = "10001",
            CardNumber = "4111111111111111",
            OriginTimestamp = DateTime.UtcNow,
            ProcessingTimestamp = DateTime.UtcNow
        };

        context.DailyTransactions.Add(dailyTran);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(DailyTransaction_CanBePersisted_AndRetrieved));
        var retrieved = await readContext.DailyTransactions.FirstAsync();
        retrieved.Should().NotBeNull();
        retrieved.TransactionAmount.Should().Be(50.00m);
    }

    [Fact]
    public async Task DailyTransactionReject_CanBePersisted_AndRetrieved()
    {
        using var context = CreateContext(nameof(DailyTransactionReject_CanBePersisted_AndRetrieved));
        await context.Database.EnsureCreatedAsync();

        var reject = new DailyTransactionReject
        {
            TransactionId = "REJ0000000000001",
            TransactionType = "01",
            TransactionCategory = 5411,
            TransactionSource = "BATCH",
            TransactionDescription = "Rejected transaction",
            TransactionAmount = 25.00m,
            MerchantId = "100000001",
            MerchantName = "Reject Merchant",
            MerchantCity = "Reject City",
            MerchantZip = "10001",
            CardNumber = "4111111111111111",
            OriginTimestamp = DateTime.UtcNow,
            ProcessingTimestamp = DateTime.UtcNow,
            RejectReasonCode = 1,
            RejectReasonDescription = "Invalid card number"
        };

        context.DailyTransactionRejects.Add(reject);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(DailyTransactionReject_CanBePersisted_AndRetrieved));
        var retrieved = await readContext.DailyTransactionRejects.FirstAsync();
        retrieved.Should().NotBeNull();
        retrieved.RejectReasonCode.Should().Be(1);
        retrieved.RejectReasonDescription.Should().Be("Invalid card number");
    }

    [Fact]
    public async Task TransactionType_CanBePersisted_AndRetrieved()
    {
        using var context = CreateContext(nameof(TransactionType_CanBePersisted_AndRetrieved));
        await context.Database.EnsureCreatedAsync();

        var type = new TransactionType
        {
            TypeCode = "99",
            TypeDescription = "Test Transaction Type"
        };

        context.TransactionTypes.Add(type);
        await context.SaveChangesAsync();

        using var readContext = CreateContext(nameof(TransactionType_CanBePersisted_AndRetrieved));
        var retrieved = await readContext.TransactionTypes.FindAsync("99");
        retrieved.Should().NotBeNull();
        retrieved!.TypeDescription.Should().Be("Test Transaction Type");
    }
}
