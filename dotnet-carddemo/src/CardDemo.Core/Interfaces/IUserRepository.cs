using CardDemo.Core.Entities;

namespace CardDemo.Core.Interfaces;

public interface IUserRepository
{
    Task<User?> GetByIdAsync(string userId);
    Task<(IReadOnlyList<User> Users, int TotalCount)> GetUsersAsync(string? userIdPrefix, int page, int pageSize);
    Task AddAsync(User user);
    Task UpdateAsync(User user);
    Task DeleteAsync(string userId);
}
