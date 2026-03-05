using CardDemo.Core.Entities;

namespace CardDemo.Core.Interfaces;

public interface IAccountRepository
{
    Task<Account?> GetByIdAsync(long accountId);
}
