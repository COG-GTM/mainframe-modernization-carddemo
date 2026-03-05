using CardDemo.Core.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace CardDemo.Infrastructure.Data.Configurations;

public class UserConfiguration : IEntityTypeConfiguration<User>
{
    public void Configure(EntityTypeBuilder<User> builder)
    {
        builder.ToTable("users");

        builder.HasKey(u => u.UserId);

        builder.Property(u => u.UserId)
            .HasMaxLength(8)
            .IsRequired();

        builder.Property(u => u.FirstName)
            .HasMaxLength(20)
            .IsRequired();

        builder.Property(u => u.LastName)
            .HasMaxLength(20)
            .IsRequired();

        builder.Property(u => u.Password)
            .HasMaxLength(8)
            .IsRequired();

        builder.Property(u => u.UserType)
            .HasMaxLength(1)
            .IsRequired();
    }
}
