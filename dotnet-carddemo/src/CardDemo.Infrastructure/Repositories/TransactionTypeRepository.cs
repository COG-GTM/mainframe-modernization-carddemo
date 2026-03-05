using CardDemo.Core.Entities;
using CardDemo.Core.Interfaces;
using CardDemo.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Repositories;

public class TransactionTypeRepository : ITransactionTypeRepository
{
    private readonly CardDemoDbContext _context;

    public TransactionTypeRepository(CardDemoDbContext context)
    {
        _context = context;
    }

    public async Task<IReadOnlyList<TransactionType>> GetAllAsync()
    {
        return await _context.TransactionTypes
            .OrderBy(tt => tt.TypeCode)
            .ToListAsync();
    }

    public async Task<TransactionType?> GetByCodeAsync(string code)
    {
        return await _context.TransactionTypes.FindAsync(code);
    }
}
