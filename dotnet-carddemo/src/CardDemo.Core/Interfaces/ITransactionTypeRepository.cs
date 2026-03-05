using CardDemo.Core.Entities;

namespace CardDemo.Core.Interfaces;

public interface ITransactionTypeRepository
{
    Task<IReadOnlyList<TransactionType>> GetAllAsync();
    Task<TransactionType?> GetByCodeAsync(string code);
}
