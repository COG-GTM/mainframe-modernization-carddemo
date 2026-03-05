using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class TransactionTypeConfiguration : IEntityTypeConfiguration<TransactionType>
{
    public void Configure(EntityTypeBuilder<TransactionType> builder)
    {
        builder.ToTable("transaction_types");

        builder.HasKey(tt => tt.TypeCode);

        builder.Property(tt => tt.TypeCode)
            .HasMaxLength(2)
            .IsRequired();

        builder.Property(tt => tt.TypeDescription)
            .HasMaxLength(100)
            .IsRequired();
    }
}
