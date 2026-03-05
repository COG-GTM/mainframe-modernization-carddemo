using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class CustomerConfiguration : IEntityTypeConfiguration<Customer>
{
    public void Configure(EntityTypeBuilder<Customer> builder)
    {
        builder.ToTable("customers");

        builder.HasKey(c => c.CustomerId);

        builder.Property(c => c.CustomerId)
            .ValueGeneratedNever();

        builder.Property(c => c.FirstName)
            .HasMaxLength(25)
            .IsRequired();

        builder.Property(c => c.MiddleName)
            .HasMaxLength(25);

        builder.Property(c => c.LastName)
            .HasMaxLength(25)
            .IsRequired();

        builder.Property(c => c.AddressLine1)
            .HasMaxLength(50);

        builder.Property(c => c.AddressLine2)
            .HasMaxLength(50);

        builder.Property(c => c.AddressLine3)
            .HasMaxLength(50);

        builder.Property(c => c.State)
            .HasMaxLength(2);

        builder.Property(c => c.CountryCode)
            .HasMaxLength(3);

        builder.Property(c => c.ZipCode)
            .HasMaxLength(10);

        builder.Property(c => c.PhoneNumber1)
            .HasMaxLength(15);

        builder.Property(c => c.PhoneNumber2)
            .HasMaxLength(15);

        builder.Property(c => c.Ssn)
            .HasMaxLength(9);

        builder.Property(c => c.GovtIssuedId)
            .HasMaxLength(20);

        builder.Property(c => c.DateOfBirth)
            .HasMaxLength(10);

        builder.Property(c => c.EftAccountId)
            .HasMaxLength(10);

        builder.Property(c => c.PrimaryCardHolderIndicator)
            .HasMaxLength(1);

        builder.HasMany(c => c.CrossReferences)
            .WithOne(cr => cr.Customer)
            .HasForeignKey(cr => cr.CustomerId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
