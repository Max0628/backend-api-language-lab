using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Primitives;
using Xunit;

namespace BackendApiLanguageLab.CSharp.Tests;

/// <summary>Tests query defaults and validation in the jobs service.</summary>
public sealed class JobsServiceTests
{
    [Fact]
    public async Task ListAsyncAppliesDefaultQueryValues()
    {
        var repository = new FakeJobsRepository();
        var service = new BackendApiLanguageLab.CSharp.Jobs.JobsService(repository);

        await service.ListAsync(new QueryCollection(), CancellationToken.None);

        Assert.NotNull(repository.ReceivedQuery);
        Assert.Equal(1, repository.ReceivedQuery!.Page);
        Assert.Equal(30, repository.ReceivedQuery.PageSize);
        Assert.Equal("posted_at", repository.ReceivedQuery.SortBy);
        Assert.Equal(BackendApiLanguageLab.CSharp.Jobs.SortOrder.Desc, repository.ReceivedQuery.SortOrder);
    }

    [Fact]
    public async Task ListAsyncRejectsInvalidPageSize()
    {
        var service = new BackendApiLanguageLab.CSharp.Jobs.JobsService(new FakeJobsRepository());
        var query = new QueryCollection(new Dictionary<string, StringValues>
        {
            ["page_size"] = "20"
        });

        var exception = await Assert.ThrowsAsync<BackendApiLanguageLab.CSharp.Jobs.InvalidParameterException>(
            () => service.ListAsync(query, CancellationToken.None));

        Assert.Equal("page_size must be between 30 and 50", exception.Message);
    }

    private sealed class FakeJobsRepository : BackendApiLanguageLab.CSharp.Jobs.IJobsRepository
    {
        public BackendApiLanguageLab.CSharp.Jobs.ListJobsQuery? ReceivedQuery { get; private set; }

        public Task<IReadOnlyList<BackendApiLanguageLab.CSharp.Jobs.Job>> ListAsync(
            BackendApiLanguageLab.CSharp.Jobs.ListJobsQuery query,
            CancellationToken cancellationToken)
        {
            ReceivedQuery = query;
            return Task.FromResult<IReadOnlyList<BackendApiLanguageLab.CSharp.Jobs.Job>>([]);
        }
    }
}
