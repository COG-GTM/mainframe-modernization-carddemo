using CardDemo.Infrastructure;
using CardDemo.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Configure CardDemo infrastructure services
if (builder.Environment.IsDevelopment())
{
    builder.Services.AddCardDemoInfrastructure(options =>
        options.UseInMemoryDatabase("CardDemoDevDb"));
}
else
{
    var connectionString = builder.Configuration.GetConnectionString("CardDemoDb")
        ?? throw new InvalidOperationException("Connection string 'CardDemoDb' not found.");
    builder.Services.AddCardDemoInfrastructure(connectionString);
}

var app = builder.Build();

// Apply migrations / ensure database is created on startup in development
if (app.Environment.IsDevelopment())
{
    using var scope = app.Services.CreateScope();
    var dbContext = scope.ServiceProvider.GetRequiredService<CardDemoDbContext>();
    await dbContext.Database.EnsureCreatedAsync();
}

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.MapGet("/health", () => Results.Ok(new { Status = "Healthy", Timestamp = DateTime.UtcNow }))
    .WithName("HealthCheck")
    .WithOpenApi();

app.Run();
