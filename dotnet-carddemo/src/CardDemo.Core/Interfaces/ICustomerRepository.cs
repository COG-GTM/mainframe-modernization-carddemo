using CardDemo.Core.Entities;

namespace CardDemo.Core.Interfaces;

public interface ICustomerRepository
{
    Task<Customer?> GetByIdAsync(long customerId);
}
