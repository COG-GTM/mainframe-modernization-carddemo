using CardDemo.Core.Entities;
using CardDemo.Core.Interfaces;
using CardDemo.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace CardDemo.Infrastructure.Repositories;

public class CustomerRepository : ICustomerRepository
{
    private readonly CardDemoDbContext _context;

    public CustomerRepository(CardDemoDbContext context)
    {
        _context = context;
    }

    public async Task<Customer?> GetByIdAsync(long customerId)
    {
        return await _context.Customers
            .Include(c => c.CrossReferences)
            .FirstOrDefaultAsync(c => c.CustomerId == customerId);
    }
}
