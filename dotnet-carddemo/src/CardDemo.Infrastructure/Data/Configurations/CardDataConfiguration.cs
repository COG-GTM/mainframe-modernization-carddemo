using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class CardDataConfiguration : IEntityTypeConfiguration<CardData>
{
    public void Configure(EntityTypeBuilder<CardData> builder)
    {
        builder.ToTable("card_data");

        builder.HasKey(c => c.CardNumber);

        builder.Property(c => c.CardNumber)
            .HasMaxLength(16)
            .IsRequired();

        builder.Property(c => c.AccountId)
            .IsRequired();

        builder.Property(c => c.CvvCode);

        builder.Property(c => c.EmbossedName)
            .HasMaxLength(50);

        builder.Property(c => c.ExpirationDate)
            .HasMaxLength(10);

        builder.Property(c => c.CardStatus)
            .HasMaxLength(1);

        builder.HasOne(c => c.Account)
            .WithMany(a => a.Cards)
            .HasForeignKey(c => c.AccountId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasIndex(c => c.AccountId);
    }
}
