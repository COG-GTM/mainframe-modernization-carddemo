using CardDemo.Core.Entities;
using CardDemo.Core.Interfaces;
using CardDemo.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Repositories;

public class AccountRepository : IAccountRepository
{
    private readonly CardDemoDbContext _context;

    public AccountRepository(CardDemoDbContext context)
    {
        _context = context;
    }

    public async Task<Account?> GetByIdAsync(long accountId)
    {
        return await _context.Accounts
            .Include(a => a.Cards)
            .Include(a => a.CrossReferences)
            .FirstOrDefaultAsync(a => a.AccountId == accountId);
    }
}
