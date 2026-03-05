using CardDemo.Core.Interfaces;
using CardDemo.Infrastructure.Data;
using CardDemo.Infrastructure.Repositories;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace CardDemo.Infrastructure;

/// <summary>
/// Extension methods for configuring CardDemo infrastructure services in the DI container.
/// </summary>
public static class ServiceCollectionExtensions
{
    /// <summary>
    /// Adds CardDemo infrastructure services with PostgreSQL provider.
    /// </summary>
    public static IServiceCollection AddCardDemoInfrastructure(
        this IServiceCollection services, string connectionString)
    {
        services.AddDbContext<CardDemoDbContext>(options =>
            options.UseNpgsql(connectionString));

        RegisterRepositories(services);
        return services;
    }

    /// <summary>
    /// Adds CardDemo infrastructure services with a custom DbContext options configuration.
    /// Useful for InMemory or other providers in testing/development.
    /// </summary>
    public static IServiceCollection AddCardDemoInfrastructure(
        this IServiceCollection services, Action<DbContextOptionsBuilder> configureOptions)
    {
        services.AddDbContext<CardDemoDbContext>(configureOptions);

        RegisterRepositories(services);
        return services;
    }

    private static void RegisterRepositories(IServiceCollection services)
    {
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IAccountRepository, AccountRepository>();
        services.AddScoped<ICustomerRepository, CustomerRepository>();
        services.AddScoped<ICardDataRepository, CardDataRepository>();
        services.AddScoped<ICardCrossReferenceRepository, CardCrossReferenceRepository>();
        services.AddScoped<ITransactionRepository, TransactionRepository>();
        services.AddScoped<ITransactionTypeRepository, TransactionTypeRepository>();
    }
}
