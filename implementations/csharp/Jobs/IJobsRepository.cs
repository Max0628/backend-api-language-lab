namespace BackendApiLanguageLab.CSharp.Jobs;

/// <summary>Minimal data access contract required by the jobs service.</summary>
public interface IJobsRepository
{
    /// <summary>Lists jobs using validated query conditions.</summary>
    Task<IReadOnlyList<Job>> ListAsync(ListJobsQuery query, CancellationToken cancellationToken);
}
