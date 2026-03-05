using CardDemo.Core.Entities;
using CardDemo.Core.Interfaces;
using CardDemo.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Repositories;

public class CardCrossReferenceRepository : ICardCrossReferenceRepository
{
    private readonly CardDemoDbContext _context;

    public CardCrossReferenceRepository(CardDemoDbContext context)
    {
        _context = context;
    }

    public async Task<CardCrossReference?> GetByCardNumberAsync(string cardNumber)
    {
        return await _context.CardCrossReferences
            .Include(cr => cr.Account)
            .Include(cr => cr.Customer)
            .FirstOrDefaultAsync(cr => cr.CardNumber == cardNumber);
    }

    public async Task<IReadOnlyList<CardCrossReference>> GetByAccountIdAsync(long accountId)
    {
        return await _context.CardCrossReferences
            .Include(cr => cr.Customer)
            .Where(cr => cr.AccountId == accountId)
            .ToListAsync();
    }

    public async Task<IReadOnlyList<CardCrossReference>> GetByCustomerIdAsync(long customerId)
    {
        return await _context.CardCrossReferences
            .Include(cr => cr.Account)
            .Where(cr => cr.CustomerId == customerId)
            .ToListAsync();
    }
}
