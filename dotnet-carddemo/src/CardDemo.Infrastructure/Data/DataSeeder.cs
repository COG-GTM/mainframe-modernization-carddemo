using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Data;

/// <summary>
/// Seeds the database with initial data for development and testing.
/// </summary>
public static class DataSeeder
{
    public static void Seed(ModelBuilder modelBuilder)
    {
        // Seed Users
        modelBuilder.Entity<User>().HasData(
            new User
            {
                UserId = "ADMIN001",
                FirstName = "Admin",
                LastName = "User",
                Password = "CHANGEME",  // Placeholder — will be replaced with hashed passwords in Phase 2
                UserType = "A"
            },
            new User
            {
                UserId = "USER0001",
                FirstName = "Regular",
                LastName = "User",
                Password = "CHANGEME",  // Placeholder — will be replaced with hashed passwords in Phase 2
                UserType = "U"
            }
        );

        // Seed Customers
        modelBuilder.Entity<Customer>().HasData(
            new Customer
            {
                CustomerId = 100000001,
                FirstName = "John",
                MiddleName = "Michael",
                LastName = "Smith",
                AddressLine1 = "123 Main Street",
                AddressLine2 = "Apt 4B",
                AddressLine3 = "",
                State = "NY",
                CountryCode = "USA",
                ZipCode = "10001",
                PhoneNumber1 = "212-555-0101",
                PhoneNumber2 = "212-555-0102",
                Ssn = "123456789",
                GovtIssuedId = "NY-DL-12345678",
                DateOfBirth = "1985-03-15",
                EftAccountId = "EFT0000001",
                PrimaryCardHolderIndicator = "Y",
                FicoScore = 750
            },
            new Customer
            {
                CustomerId = 100000002,
                FirstName = "Jane",
                MiddleName = "Elizabeth",
                LastName = "Doe",
                AddressLine1 = "456 Oak Avenue",
                AddressLine2 = "",
                AddressLine3 = "",
                State = "CA",
                CountryCode = "USA",
                ZipCode = "90210",
                PhoneNumber1 = "310-555-0201",
                PhoneNumber2 = "",
                Ssn = "987654321",
                GovtIssuedId = "CA-DL-87654321",
                DateOfBirth = "1990-07-22",
                EftAccountId = "EFT0000002",
                PrimaryCardHolderIndicator = "Y",
                FicoScore = 680
            },
            new Customer
            {
                CustomerId = 100000003,
                FirstName = "Robert",
                MiddleName = "James",
                LastName = "Johnson",
                AddressLine1 = "789 Pine Road",
                AddressLine2 = "Suite 100",
                AddressLine3 = "",
                State = "TX",
                CountryCode = "USA",
                ZipCode = "75001",
                PhoneNumber1 = "214-555-0301",
                PhoneNumber2 = "214-555-0302",
                Ssn = "456789123",
                GovtIssuedId = "TX-DL-45678912",
                DateOfBirth = "1978-11-08",
                EftAccountId = "EFT0000003",
                PrimaryCardHolderIndicator = "Y",
                FicoScore = 720
            }
        );

        // Seed Accounts
        modelBuilder.Entity<Account>().HasData(
            new Account
            {
                AccountId = 80000000001,
                AccountStatus = "Y",
                CurrentBalance = 1500.50m,
                CreditLimit = 5000.00m,
                CashCreditLimit = 1500.00m,
                OpenDate = "2020-01-15",
                ExpirationDate = "2027-01-15",
                ReissueDate = "2025-01-15",
                CurrentCycleCredit = 200.00m,
                CurrentCycleDebit = 350.75m,
                AddressZip = "10001",
                GroupId = "GRP00001"
            },
            new Account
            {
                AccountId = 80000000002,
                AccountStatus = "Y",
                CurrentBalance = 3200.00m,
                CreditLimit = 10000.00m,
                CashCreditLimit = 3000.00m,
                OpenDate = "2019-06-20",
                ExpirationDate = "2026-06-20",
                ReissueDate = "2024-06-20",
                CurrentCycleCredit = 500.00m,
                CurrentCycleDebit = 1200.50m,
                AddressZip = "90210",
                GroupId = "GRP00002"
            },
            new Account
            {
                AccountId = 80000000003,
                AccountStatus = "Y",
                CurrentBalance = 750.25m,
                CreditLimit = 3000.00m,
                CashCreditLimit = 1000.00m,
                OpenDate = "2021-03-10",
                ExpirationDate = "2028-03-10",
                ReissueDate = "2026-03-10",
                CurrentCycleCredit = 100.00m,
                CurrentCycleDebit = 200.00m,
                AddressZip = "75001",
                GroupId = "GRP00001"
            }
        );

        // Seed CardData
        modelBuilder.Entity<CardData>().HasData(
            new CardData
            {
                CardNumber = "4111111111111111",
                AccountId = 80000000001,
                CvvCode = 123,
                EmbossedName = "JOHN M SMITH",
                ExpirationDate = "2027-01-15",
                CardStatus = "Y"
            },
            new CardData
            {
                CardNumber = "4222222222222222",
                AccountId = 80000000002,
                CvvCode = 456,
                EmbossedName = "JANE E DOE",
                ExpirationDate = "2026-06-20",
                CardStatus = "Y"
            },
            new CardData
            {
                CardNumber = "4333333333333333",
                AccountId = 80000000003,
                CvvCode = 789,
                EmbossedName = "ROBERT J JOHNSON",
                ExpirationDate = "2028-03-10",
                CardStatus = "Y"
            }
        );

        // Seed CardCrossReferences
        modelBuilder.Entity<CardCrossReference>().HasData(
            new CardCrossReference
            {
                CardNumber = "4111111111111111",
                CustomerId = 100000001,
                AccountId = 80000000001
            },
            new CardCrossReference
            {
                CardNumber = "4222222222222222",
                CustomerId = 100000002,
                AccountId = 80000000002
            },
            new CardCrossReference
            {
                CardNumber = "4333333333333333",
                CustomerId = 100000003,
                AccountId = 80000000003
            }
        );

        // Seed TransactionTypes
        modelBuilder.Entity<TransactionType>().HasData(
            new TransactionType { TypeCode = "01", TypeDescription = "Purchase" },
            new TransactionType { TypeCode = "02", TypeDescription = "Return" },
            new TransactionType { TypeCode = "03", TypeDescription = "Cash Advance" },
            new TransactionType { TypeCode = "04", TypeDescription = "Payment" },
            new TransactionType { TypeCode = "05", TypeDescription = "Balance Transfer" }
        );

        // Seed Transactions
        modelBuilder.Entity<Transaction>().HasData(
            new Transaction
            {
                TransactionId = "TRN0000000000001",
                TransactionType = "01",
                TransactionCategory = 5411,
                TransactionSource = "ONLINE",
                TransactionDescription = "Grocery Store Purchase",
                TransactionAmount = 125.50m,
                MerchantId = "100000001",
                MerchantName = "FreshMart Grocery",
                MerchantCity = "New York",
                MerchantZip = "10001",
                CardNumber = "4111111111111111",
                OriginTimestamp = new DateTime(2025, 12, 1, 10, 30, 0, DateTimeKind.Utc),
                ProcessingTimestamp = new DateTime(2025, 12, 1, 10, 30, 5, DateTimeKind.Utc)
            },
            new Transaction
            {
                TransactionId = "TRN0000000000002",
                TransactionType = "01",
                TransactionCategory = 5812,
                TransactionSource = "POS",
                TransactionDescription = "Restaurant Dinner",
                TransactionAmount = 85.75m,
                MerchantId = "100000002",
                MerchantName = "Italian Bistro",
                MerchantCity = "Los Angeles",
                MerchantZip = "90210",
                CardNumber = "4222222222222222",
                OriginTimestamp = new DateTime(2025, 12, 2, 19, 15, 0, DateTimeKind.Utc),
                ProcessingTimestamp = new DateTime(2025, 12, 2, 19, 15, 3, DateTimeKind.Utc)
            },
            new Transaction
            {
                TransactionId = "TRN0000000000003",
                TransactionType = "04",
                TransactionCategory = 6011,
                TransactionSource = "ATM",
                TransactionDescription = "Payment Received",
                TransactionAmount = 500.00m,
                MerchantId = "100000003",
                MerchantName = "First National Bank",
                MerchantCity = "Dallas",
                MerchantZip = "75001",
                CardNumber = "4333333333333333",
                OriginTimestamp = new DateTime(2025, 12, 3, 14, 0, 0, DateTimeKind.Utc),
                ProcessingTimestamp = new DateTime(2025, 12, 3, 14, 0, 2, DateTimeKind.Utc)
            }
        );
    }
}
