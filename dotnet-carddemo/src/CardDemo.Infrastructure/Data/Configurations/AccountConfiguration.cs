using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class AccountConfiguration : IEntityTypeConfiguration<Account>
{
    public void Configure(EntityTypeBuilder<Account> builder)
    {
        builder.ToTable("accounts");

        builder.HasKey(a => a.AccountId);

        builder.Property(a => a.AccountId)
            .ValueGeneratedNever();

        builder.Property(a => a.AccountStatus)
            .HasMaxLength(1)
            .IsRequired();

        builder.Property(a => a.CurrentBalance)
            .HasPrecision(18, 2);

        builder.Property(a => a.CreditLimit)
            .HasPrecision(18, 2);

        builder.Property(a => a.CashCreditLimit)
            .HasPrecision(18, 2);

        builder.Property(a => a.OpenDate)
            .HasMaxLength(10)
            .IsRequired();

        builder.Property(a => a.ExpirationDate)
            .HasMaxLength(10)
            .IsRequired();

        builder.Property(a => a.ReissueDate)
            .HasMaxLength(10)
            .IsRequired();

        builder.Property(a => a.CurrentCycleCredit)
            .HasPrecision(18, 2);

        builder.Property(a => a.CurrentCycleDebit)
            .HasPrecision(18, 2);

        builder.Property(a => a.AddressZip)
            .HasMaxLength(10);

        builder.Property(a => a.GroupId)
            .HasMaxLength(10);

        builder.HasMany(a => a.Cards)
            .WithOne(c => c.Account)
            .HasForeignKey(c => c.AccountId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasMany(a => a.CrossReferences)
            .WithOne(cr => cr.Account)
            .HasForeignKey(cr => cr.AccountId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
