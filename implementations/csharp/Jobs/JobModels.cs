namespace BackendApiLanguageLab.CSharp.Jobs;

/// <summary>Domain model for a normalized job.</summary>
public sealed record Job(
    string Source,
    string SourceJobId,
    string Title,
    string? Company,
    string Url,
    DateTime? PostedAt,
    Salary Salary,
    Location Location);

/// <summary>Salary information in the domain model.</summary>
public sealed record Salary(long? Minimum, long? Maximum, string? Currency);

/// <summary>Location information in the domain model.</summary>
public sealed record Location(string? City, string? District);

/// <summary>Validated query used by service and repository layers.</summary>
public sealed record ListJobsQuery(
    int Page,
    int PageSize,
    string? Source,
    string? City,
    string? District,
    string? EmploymentType,
    string? JobType,
    string? Title,
    bool IncludeClosed,
    string SortBy,
    SortOrder SortOrder);

/// <summary>Allowed SQL sort directions.</summary>
public enum SortOrder
{
    Asc,
    Desc
}
