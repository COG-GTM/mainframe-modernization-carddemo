using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class DailyTransactionConfiguration : IEntityTypeConfiguration<DailyTransaction>
{
    public void Configure(EntityTypeBuilder<DailyTransaction> builder)
    {
        builder.ToTable("daily_transactions");

        builder.HasKey(dt => dt.DailyTransactionId);

        builder.Property(dt => dt.DailyTransactionId)
            .ValueGeneratedOnAdd();

        builder.Property(dt => dt.TransactionId)
            .HasMaxLength(16)
            .IsRequired();

        builder.Property(dt => dt.TransactionType)
            .HasMaxLength(2)
            .IsRequired();

        builder.Property(dt => dt.TransactionCategory);

        builder.Property(dt => dt.TransactionSource)
            .HasMaxLength(10);

        builder.Property(dt => dt.TransactionDescription)
            .HasMaxLength(100);

        builder.Property(dt => dt.TransactionAmount)
            .HasPrecision(18, 2);

        builder.Property(dt => dt.MerchantId)
            .HasMaxLength(9);

        builder.Property(dt => dt.MerchantName)
            .HasMaxLength(50);

        builder.Property(dt => dt.MerchantCity)
            .HasMaxLength(50);

        builder.Property(dt => dt.MerchantZip)
            .HasMaxLength(10);

        builder.Property(dt => dt.CardNumber)
            .HasMaxLength(16)
            .IsRequired();

        builder.HasIndex(dt => dt.CardNumber);
    }
}
