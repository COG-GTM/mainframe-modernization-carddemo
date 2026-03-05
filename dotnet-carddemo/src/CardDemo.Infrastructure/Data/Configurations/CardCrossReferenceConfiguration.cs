using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class CardCrossReferenceConfiguration : IEntityTypeConfiguration<CardCrossReference>
{
    public void Configure(EntityTypeBuilder<CardCrossReference> builder)
    {
        builder.ToTable("card_xref");

        builder.HasKey(cr => cr.CardNumber);

        builder.Property(cr => cr.CardNumber)
            .HasMaxLength(16)
            .IsRequired();

        builder.Property(cr => cr.CustomerId)
            .IsRequired();

        builder.Property(cr => cr.AccountId)
            .IsRequired();

        builder.HasOne(cr => cr.Account)
            .WithMany(a => a.CrossReferences)
            .HasForeignKey(cr => cr.AccountId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(cr => cr.Customer)
            .WithMany(c => c.CrossReferences)
            .HasForeignKey(cr => cr.CustomerId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasIndex(cr => cr.AccountId);
        builder.HasIndex(cr => cr.CustomerId);
    }
}
