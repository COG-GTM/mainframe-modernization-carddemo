using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class TransactionConfiguration : IEntityTypeConfiguration<Transaction>
{
    public void Configure(EntityTypeBuilder<Transaction> builder)
    {
        builder.ToTable("transactions");

        builder.HasKey(t => t.TransactionId);

        builder.Property(t => t.TransactionId)
            .HasMaxLength(16)
            .IsRequired();

        builder.Property(t => t.TransactionType)
            .HasMaxLength(2)
            .IsRequired();

        builder.Property(t => t.TransactionCategory);

        builder.Property(t => t.TransactionSource)
            .HasMaxLength(10);

        builder.Property(t => t.TransactionDescription)
            .HasMaxLength(100);

        builder.Property(t => t.TransactionAmount)
            .HasPrecision(18, 2);

        builder.Property(t => t.MerchantId)
            .HasMaxLength(9);

        builder.Property(t => t.MerchantName)
            .HasMaxLength(50);

        builder.Property(t => t.MerchantCity)
            .HasMaxLength(50);

        builder.Property(t => t.MerchantZip)
            .HasMaxLength(10);

        builder.Property(t => t.CardNumber)
            .HasMaxLength(16)
            .IsRequired();

        builder.HasIndex(t => t.CardNumber);
    }
}
