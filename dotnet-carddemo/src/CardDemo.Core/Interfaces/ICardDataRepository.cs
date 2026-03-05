using CardDemo.Core.Entities;

namespace CardDemo.Core.Interfaces;

public interface ICardDataRepository
{
    Task<CardData?> GetByCardNumberAsync(string cardNumber);
    Task<IReadOnlyList<CardData>> GetByAccountIdAsync(long accountId);
}
