using CardDemo.Core.Entities;

namespace CardDemo.Core.Interfaces;

public interface ICardCrossReferenceRepository
{
    Task<CardCrossReference?> GetByCardNumberAsync(string cardNumber);
    Task<IReadOnlyList<CardCrossReference>> GetByAccountIdAsync(long accountId);
    Task<IReadOnlyList<CardCrossReference>> GetByCustomerIdAsync(long customerId);
}
