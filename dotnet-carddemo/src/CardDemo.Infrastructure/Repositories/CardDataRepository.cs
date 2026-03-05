using CardDemo.Core.Entities;
using CardDemo.Core.Interfaces;
using CardDemo.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Repositories;

public class CardDataRepository : ICardDataRepository
{
    private readonly CardDemoDbContext _context;

    public CardDataRepository(CardDemoDbContext context)
    {
        _context = context;
    }

    public async Task<CardData?> GetByCardNumberAsync(string cardNumber)
    {
        return await _context.Cards
            .Include(c => c.Account)
            .FirstOrDefaultAsync(c => c.CardNumber == cardNumber);
    }

    public async Task<IReadOnlyList<CardData>> GetByAccountIdAsync(long accountId)
    {
        return await _context.Cards
            .Where(c => c.AccountId == accountId)
            .ToListAsync();
    }
}
