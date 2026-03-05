using System;
using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

#pragma warning disable CA1814 // Prefer jagged arrays over multidimensional

namespace CardDemo.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "accounts",
                columns: table => new
                {
                    AccountId = table.Column<long>(type: "bigint", nullable: false),
                    AccountStatus = table.Column<string>(type: "character varying(1)", maxLength: 1, nullable: false),
                    CurrentBalance = table.Column<decimal>(type: "numeric(18,2)", precision: 18, scale: 2, nullable: false),
                    CreditLimit = table.Column<decimal>(type: "numeric(18,2)", precision: 18, scale: 2, nullable: false),
                    CashCreditLimit = table.Column<decimal>(type: "numeric(18,2)", precision: 18, scale: 2, nullable: false),
                    OpenDate = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    ExpirationDate = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    ReissueDate = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    CurrentCycleCredit = table.Column<decimal>(type: "numeric(18,2)", precision: 18, scale: 2, nullable: false),
                    CurrentCycleDebit = table.Column<decimal>(type: "numeric(18,2)", precision: 18, scale: 2, nullable: false),
                    AddressZip = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    GroupId = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_accounts", x => x.AccountId);
                });

            migrationBuilder.CreateTable(
                name: "customers",
                columns: table => new
                {
                    CustomerId = table.Column<long>(type: "bigint", nullable: false),
                    FirstName = table.Column<string>(type: "character varying(25)", maxLength: 25, nullable: false),
                    MiddleName = table.Column<string>(type: "character varying(25)", maxLength: 25, nullable: false),
                    LastName = table.Column<string>(type: "character varying(25)", maxLength: 25, nullable: false),
                    AddressLine1 = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    AddressLine2 = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    AddressLine3 = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    State = table.Column<string>(type: "character varying(2)", maxLength: 2, nullable: false),
                    CountryCode = table.Column<string>(type: "character varying(3)", maxLength: 3, nullable: false),
                    ZipCode = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    PhoneNumber1 = table.Column<string>(type: "character varying(15)", maxLength: 15, nullable: false),
                    PhoneNumber2 = table.Column<string>(type: "character varying(15)", maxLength: 15, nullable: false),
                    Ssn = table.Column<string>(type: "character varying(9)", maxLength: 9, nullable: false),
                    GovtIssuedId = table.Column<string>(type: "character varying(20)", maxLength: 20, nullable: false),
                    DateOfBirth = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    EftAccountId = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    PrimaryCardHolderIndicator = table.Column<string>(type: "character varying(1)", maxLength: 1, nullable: false),
                    FicoScore = table.Column<int>(type: "integer", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_customers", x => x.CustomerId);
                });

            migrationBuilder.CreateTable(
                name: "daily_transaction_rejects",
                columns: table => new
                {
                    DailyTransactionRejectId = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    TransactionId = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    TransactionType = table.Column<string>(type: "character varying(2)", maxLength: 2, nullable: false),
                    TransactionCategory = table.Column<int>(type: "integer", nullable: false),
                    TransactionSource = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    TransactionDescription = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    TransactionAmount = table.Column<decimal>(type: "numeric(18,2)", precision: 18, scale: 2, nullable: false),
                    MerchantId = table.Column<string>(type: "character varying(9)", maxLength: 9, nullable: false),
                    MerchantName = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    MerchantCity = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    MerchantZip = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    CardNumber = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    OriginTimestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    ProcessingTimestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    RejectReasonCode = table.Column<int>(type: "integer", nullable: false),
                    RejectReasonDescription = table.Column<string>(type: "character varying(250)", maxLength: 250, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_daily_transaction_rejects", x => x.DailyTransactionRejectId);
                });

            migrationBuilder.CreateTable(
                name: "daily_transactions",
                columns: table => new
                {
                    DailyTransactionId = table.Column<long>(type: "bigint", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    TransactionId = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    TransactionType = table.Column<string>(type: "character varying(2)", maxLength: 2, nullable: false),
                    TransactionCategory = table.Column<int>(type: "integer", nullable: false),
                    TransactionSource = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    TransactionDescription = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    TransactionAmount = table.Column<decimal>(type: "numeric(18,2)", precision: 18, scale: 2, nullable: false),
                    MerchantId = table.Column<string>(type: "character varying(9)", maxLength: 9, nullable: false),
                    MerchantName = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    MerchantCity = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    MerchantZip = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    CardNumber = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    OriginTimestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    ProcessingTimestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_daily_transactions", x => x.DailyTransactionId);
                });

            migrationBuilder.CreateTable(
                name: "transaction_types",
                columns: table => new
                {
                    TypeCode = table.Column<string>(type: "character varying(2)", maxLength: 2, nullable: false),
                    TypeDescription = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_transaction_types", x => x.TypeCode);
                });

            migrationBuilder.CreateTable(
                name: "transactions",
                columns: table => new
                {
                    TransactionId = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    TransactionType = table.Column<string>(type: "character varying(2)", maxLength: 2, nullable: false),
                    TransactionCategory = table.Column<int>(type: "integer", nullable: false),
                    TransactionSource = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    TransactionDescription = table.Column<string>(type: "character varying(100)", maxLength: 100, nullable: false),
                    TransactionAmount = table.Column<decimal>(type: "numeric(18,2)", precision: 18, scale: 2, nullable: false),
                    MerchantId = table.Column<string>(type: "character varying(9)", maxLength: 9, nullable: false),
                    MerchantName = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    MerchantCity = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    MerchantZip = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    CardNumber = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    OriginTimestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    ProcessingTimestamp = table.Column<DateTime>(type: "timestamp with time zone", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_transactions", x => x.TransactionId);
                });

            migrationBuilder.CreateTable(
                name: "users",
                columns: table => new
                {
                    UserId = table.Column<string>(type: "character varying(8)", maxLength: 8, nullable: false),
                    FirstName = table.Column<string>(type: "character varying(20)", maxLength: 20, nullable: false),
                    LastName = table.Column<string>(type: "character varying(20)", maxLength: 20, nullable: false),
                    Password = table.Column<string>(type: "character varying(8)", maxLength: 8, nullable: false),
                    UserType = table.Column<string>(type: "character varying(1)", maxLength: 1, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_users", x => x.UserId);
                });

            migrationBuilder.CreateTable(
                name: "card_data",
                columns: table => new
                {
                    CardNumber = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    AccountId = table.Column<long>(type: "bigint", nullable: false),
                    CvvCode = table.Column<int>(type: "integer", nullable: false),
                    EmbossedName = table.Column<string>(type: "character varying(50)", maxLength: 50, nullable: false),
                    ExpirationDate = table.Column<string>(type: "character varying(10)", maxLength: 10, nullable: false),
                    CardStatus = table.Column<string>(type: "character varying(1)", maxLength: 1, nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_card_data", x => x.CardNumber);
                    table.ForeignKey(
                        name: "FK_card_data_accounts_AccountId",
                        column: x => x.AccountId,
                        principalTable: "accounts",
                        principalColumn: "AccountId",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateTable(
                name: "card_xref",
                columns: table => new
                {
                    CardNumber = table.Column<string>(type: "character varying(16)", maxLength: 16, nullable: false),
                    CustomerId = table.Column<long>(type: "bigint", nullable: false),
                    AccountId = table.Column<long>(type: "bigint", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_card_xref", x => x.CardNumber);
                    table.ForeignKey(
                        name: "FK_card_xref_accounts_AccountId",
                        column: x => x.AccountId,
                        principalTable: "accounts",
                        principalColumn: "AccountId",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_card_xref_customers_CustomerId",
                        column: x => x.CustomerId,
                        principalTable: "customers",
                        principalColumn: "CustomerId",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.InsertData(
                table: "accounts",
                columns: new[] { "AccountId", "AccountStatus", "AddressZip", "CashCreditLimit", "CreditLimit", "CurrentBalance", "CurrentCycleCredit", "CurrentCycleDebit", "ExpirationDate", "GroupId", "OpenDate", "ReissueDate" },
                values: new object[,]
                {
                    { 80000000001L, "Y", "10001", 1500.00m, 5000.00m, 1500.50m, 200.00m, 350.75m, "2027-01-15", "GRP00001", "2020-01-15", "2025-01-15" },
                    { 80000000002L, "Y", "90210", 3000.00m, 10000.00m, 3200.00m, 500.00m, 1200.50m, "2026-06-20", "GRP00002", "2019-06-20", "2024-06-20" },
                    { 80000000003L, "Y", "75001", 1000.00m, 3000.00m, 750.25m, 100.00m, 200.00m, "2028-03-10", "GRP00001", "2021-03-10", "2026-03-10" }
                });

            migrationBuilder.InsertData(
                table: "customers",
                columns: new[] { "CustomerId", "AddressLine1", "AddressLine2", "AddressLine3", "CountryCode", "DateOfBirth", "EftAccountId", "FicoScore", "FirstName", "GovtIssuedId", "LastName", "MiddleName", "PhoneNumber1", "PhoneNumber2", "PrimaryCardHolderIndicator", "Ssn", "State", "ZipCode" },
                values: new object[,]
                {
                    { 100000001L, "123 Main Street", "Apt 4B", "", "USA", "1985-03-15", "EFT0000001", 750, "John", "NY-DL-12345678", "Smith", "Michael", "212-555-0101", "212-555-0102", "Y", "123456789", "NY", "10001" },
                    { 100000002L, "456 Oak Avenue", "", "", "USA", "1990-07-22", "EFT0000002", 680, "Jane", "CA-DL-87654321", "Doe", "Elizabeth", "310-555-0201", "", "Y", "987654321", "CA", "90210" },
                    { 100000003L, "789 Pine Road", "Suite 100", "", "USA", "1978-11-08", "EFT0000003", 720, "Robert", "TX-DL-45678912", "Johnson", "James", "214-555-0301", "214-555-0302", "Y", "456789123", "TX", "75001" }
                });

            migrationBuilder.InsertData(
                table: "transaction_types",
                columns: new[] { "TypeCode", "TypeDescription" },
                values: new object[,]
                {
                    { "01", "Purchase" },
                    { "02", "Return" },
                    { "03", "Cash Advance" },
                    { "04", "Payment" },
                    { "05", "Balance Transfer" }
                });

            migrationBuilder.InsertData(
                table: "transactions",
                columns: new[] { "TransactionId", "CardNumber", "MerchantCity", "MerchantId", "MerchantName", "MerchantZip", "OriginTimestamp", "ProcessingTimestamp", "TransactionAmount", "TransactionCategory", "TransactionDescription", "TransactionSource", "TransactionType" },
                values: new object[,]
                {
                    { "TRN0000000000001", "4111111111111111", "New York", "100000001", "FreshMart Grocery", "10001", new DateTime(2025, 12, 1, 10, 30, 0, 0, DateTimeKind.Utc), new DateTime(2025, 12, 1, 10, 30, 5, 0, DateTimeKind.Utc), 125.50m, 5411, "Grocery Store Purchase", "ONLINE", "01" },
                    { "TRN0000000000002", "4222222222222222", "Los Angeles", "100000002", "Italian Bistro", "90210", new DateTime(2025, 12, 2, 19, 15, 0, 0, DateTimeKind.Utc), new DateTime(2025, 12, 2, 19, 15, 3, 0, DateTimeKind.Utc), 85.75m, 5812, "Restaurant Dinner", "POS", "01" },
                    { "TRN0000000000003", "4333333333333333", "Dallas", "100000003", "First National Bank", "75001", new DateTime(2025, 12, 3, 14, 0, 0, 0, DateTimeKind.Utc), new DateTime(2025, 12, 3, 14, 0, 2, 0, DateTimeKind.Utc), 500.00m, 6011, "Payment Received", "ATM", "04" }
                });

            migrationBuilder.InsertData(
                table: "users",
                columns: new[] { "UserId", "FirstName", "LastName", "Password", "UserType" },
                values: new object[,]
                {
                    { "ADMIN001", "Admin", "User", "ADMIN123", "A" },
                    { "USER0001", "Regular", "User", "USER1234", "U" }
                });

            migrationBuilder.InsertData(
                table: "card_data",
                columns: new[] { "CardNumber", "AccountId", "CardStatus", "CvvCode", "EmbossedName", "ExpirationDate" },
                values: new object[,]
                {
                    { "4111111111111111", 80000000001L, "Y", 123, "JOHN M SMITH", "2027-01-15" },
                    { "4222222222222222", 80000000002L, "Y", 456, "JANE E DOE", "2026-06-20" },
                    { "4333333333333333", 80000000003L, "Y", 789, "ROBERT J JOHNSON", "2028-03-10" }
                });

            migrationBuilder.InsertData(
                table: "card_xref",
                columns: new[] { "CardNumber", "AccountId", "CustomerId" },
                values: new object[,]
                {
                    { "4111111111111111", 80000000001L, 100000001L },
                    { "4222222222222222", 80000000002L, 100000002L },
                    { "4333333333333333", 80000000003L, 100000003L }
                });

            migrationBuilder.CreateIndex(
                name: "IX_card_data_AccountId",
                table: "card_data",
                column: "AccountId");

            migrationBuilder.CreateIndex(
                name: "IX_card_xref_AccountId",
                table: "card_xref",
                column: "AccountId");

            migrationBuilder.CreateIndex(
                name: "IX_card_xref_CustomerId",
                table: "card_xref",
                column: "CustomerId");

            migrationBuilder.CreateIndex(
                name: "IX_daily_transaction_rejects_CardNumber",
                table: "daily_transaction_rejects",
                column: "CardNumber");

            migrationBuilder.CreateIndex(
                name: "IX_daily_transactions_CardNumber",
                table: "daily_transactions",
                column: "CardNumber");

            migrationBuilder.CreateIndex(
                name: "IX_transactions_CardNumber",
                table: "transactions",
                column: "CardNumber");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "card_data");

            migrationBuilder.DropTable(
                name: "card_xref");

            migrationBuilder.DropTable(
                name: "daily_transaction_rejects");

            migrationBuilder.DropTable(
                name: "daily_transactions");

            migrationBuilder.DropTable(
                name: "transaction_types");

            migrationBuilder.DropTable(
                name: "transactions");

            migrationBuilder.DropTable(
                name: "users");

            migrationBuilder.DropTable(
                name: "accounts");

            migrationBuilder.DropTable(
                name: "customers");
        }
    }
}
