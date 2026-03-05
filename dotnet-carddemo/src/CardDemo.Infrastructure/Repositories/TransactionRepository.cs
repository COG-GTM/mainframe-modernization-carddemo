using CardDemo.Core.Entities;
using CardDemo.Core.Interfaces;
using CardDemo.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Repositories;

public class TransactionRepository : ITransactionRepository
{
    private readonly CardDemoDbContext _context;

    public TransactionRepository(CardDemoDbContext context)
    {
        _context = context;
    }

    public async Task<(IReadOnlyList<Transaction> Transactions, int TotalCount)> GetByCardNumberAsync(
        string cardNumber, int page, int pageSize)
    {
        var query = _context.Transactions
            .Where(t => t.CardNumber == cardNumber);

        var totalCount = await query.CountAsync();
        var transactions = await query
            .OrderByDescending(t => t.OriginTimestamp)
            .Skip((page - 1) * pageSize)
            .Take(pageSize)
            .ToListAsync();

        return (transactions, totalCount);
    }
}
