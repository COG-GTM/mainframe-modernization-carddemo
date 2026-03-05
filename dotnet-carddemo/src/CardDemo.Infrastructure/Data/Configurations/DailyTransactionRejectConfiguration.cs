using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class DailyTransactionRejectConfiguration : IEntityTypeConfiguration<DailyTransactionReject>
{
    public void Configure(EntityTypeBuilder<DailyTransactionReject> builder)
    {
        builder.ToTable("daily_transaction_rejects");

        builder.HasKey(dtr => dtr.DailyTransactionRejectId);

        builder.Property(dtr => dtr.DailyTransactionRejectId)
            .ValueGeneratedOnAdd();

        builder.Property(dtr => dtr.TransactionId)
            .HasMaxLength(16)
            .IsRequired();

        builder.Property(dtr => dtr.TransactionType)
            .HasMaxLength(2)
            .IsRequired();

        builder.Property(dtr => dtr.TransactionCategory);

        builder.Property(dtr => dtr.TransactionSource)
            .HasMaxLength(10);

        builder.Property(dtr => dtr.TransactionDescription)
            .HasMaxLength(100);

        builder.Property(dtr => dtr.TransactionAmount)
            .HasPrecision(18, 2);

        builder.Property(dtr => dtr.MerchantId)
            .HasMaxLength(9);

        builder.Property(dtr => dtr.MerchantName)
            .HasMaxLength(50);

        builder.Property(dtr => dtr.MerchantCity)
            .HasMaxLength(50);

        builder.Property(dtr => dtr.MerchantZip)
            .HasMaxLength(10);

        builder.Property(dtr => dtr.CardNumber)
            .HasMaxLength(16)
            .IsRequired();

        builder.Property(dtr => dtr.RejectReasonCode)
            .IsRequired();

        builder.Property(dtr => dtr.RejectReasonDescription)
            .HasMaxLength(250);

        builder.HasIndex(dtr => dtr.CardNumber);
    }
}
