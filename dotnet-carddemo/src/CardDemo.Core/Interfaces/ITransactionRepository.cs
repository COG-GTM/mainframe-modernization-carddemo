using CardDemo.Core.Entities;

namespace CardDemo.Core.Interfaces;

public interface ITransactionRepository
{
    Task<(IReadOnlyList<Transaction> Transactions, int TotalCount)> GetByCardNumberAsync(string cardNumber, int page, int pageSize);
}
