using CardDemo.Core.Entities;
using CardDemo.Core.Interfaces;
using CardDemo.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Repositories;

public class UserRepository : IUserRepository
{
    private readonly CardDemoDbContext _context;

    public UserRepository(CardDemoDbContext context)
    {
        _context = context;
    }

    public async Task<User?> GetByIdAsync(string userId)
    {
        return await _context.Users.FindAsync(userId);
    }

    public async Task<(IReadOnlyList<User> Users, int TotalCount)> GetUsersAsync(
        string? userIdPrefix, int page, int pageSize)
    {
        var query = _context.Users.AsQueryable();

        if (!string.IsNullOrEmpty(userIdPrefix))
        {
            query = query.Where(u => u.UserId.StartsWith(userIdPrefix));
        }

        var totalCount = await query.CountAsync();
        var users = await query
            .OrderBy(u => u.UserId)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return (users, totalCount);
    }

    public async Task AddAsync(User user)
    {
        await _context.Users.AddAsync(user);
        await _context.SaveChangesAsync();
    }

    public async Task UpdateAsync(User user)
    {
        _context.Users.Update(user);
        await _context.SaveChangesAsync();
    }

    public async Task DeleteAsync(string userId)
    {
        var user = await _context.Users.FindAsync(userId);
        if (user != null)
        {
            _context.Users.Remove(user);
            await _context.SaveChangesAsync();
        }
    }
}
