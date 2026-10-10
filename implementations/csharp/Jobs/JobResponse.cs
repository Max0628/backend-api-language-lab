namespace BackendApiLanguageLab.CSharp.Jobs;

/// <summary>Public API response DTO for a job.</summary>
public sealed record JobResponse(
    string Source,
    string SourceJobId,
    string Title,
    string? Company,
    string Url,
    DateTime? PostedAt,
    SalaryResponse Salary,
    LocationResponse Location);

/// <summary>Public API response DTO for salary information.</summary>
public sealed record SalaryResponse(long? Min, long? Max, string? Currency);

/// <summary>Public API response DTO for location information.</summary>
public sealed record LocationResponse(string? City, string? District);

/// <summary>Maps domain models into public response DTOs.</summary>
public static class JobResponseMapper
{
    /// <summary>Maps one domain job into its API response representation.</summary>
    public static JobResponse ToResponse(Job job) => new(
        job.Source,
        job.SourceJobId,
        job.Title,
        job.Company,
        job.Url,
        job.PostedAt,
        new SalaryResponse(job.Salary.Minimum, job.Salary.Maximum, job.Salary.Currency),
        new LocationResponse(job.Location.City, job.Location.District));
}
