namespace BackendApiLanguageLab.CSharp.Jobs;

/// <summary>Validates jobs queries and coordinates the repository call.</summary>
public sealed class JobsService
{
    private static readonly HashSet<string> AllowedSortColumns =
    [
        "posted_at",
        "first_seen_at",
        "last_seen_at",
        "salary_min",
        "salary_max",
        "title",
        "company"
    ];

    private readonly IJobsRepository _repository;

    /// <summary>Creates a jobs service with constructor-injected dependencies.</summary>
    public JobsService(IJobsRepository repository)
    {
        _repository = repository;
    }

    /// <summary>Validates raw query values and returns matching jobs.</summary>
    public Task<IReadOnlyList<Job>> ListAsync(
        IQueryCollection query,
        CancellationToken cancellationToken)
    {
        var validatedQuery = ParseQuery(query);
        return _repository.ListAsync(validatedQuery, cancellationToken);
    }

    private static ListJobsQuery ParseQuery(IQueryCollection query)
    {
        var page = ParseInteger(query["page"], "page", 1);
        var pageSize = ParseInteger(query["page_size"], "page_size", 30);
        if (page < 1)
        {
            throw new InvalidParameterException("page must be at least 1");
        }

        if (pageSize is < 30 or > 50)
        {
            throw new InvalidParameterException("page_size must be between 30 and 50");
        }

        var sortBy = query["sort_by"].FirstOrDefault() ?? "posted_at";
        if (!AllowedSortColumns.Contains(sortBy))
        {
            throw new InvalidParameterException("invalid sort_by");
        }

        var sortOrder = query["sort_order"].FirstOrDefault() switch
        {
            null or "desc" => SortOrder.Desc,
            "asc" => SortOrder.Asc,
            _ => throw new InvalidParameterException("sort_order must be asc or desc")
        };

        return new ListJobsQuery(
            page,
            pageSize,
            OptionalString(query["source"]),
            OptionalString(query["city"]),
            OptionalString(query["district"]),
            OptionalString(query["employment_type"]),
            OptionalString(query["job_type"]),
            OptionalString(query["title"]),
            ParseBoolean(query["include_closed"]),
            sortBy,
            sortOrder);
    }

    private static int ParseInteger(string? value, string name, int defaultValue)
    {
        if (string.IsNullOrEmpty(value))
        {
            return defaultValue;
        }

        if (!int.TryParse(value, out var result))
        {
            throw new InvalidParameterException($"{name} must be an integer");
        }

        return result;
    }

    private static bool ParseBoolean(string? value)
    {
        return value switch
        {
            null or "" => false,
            "true" or "1" => true,
            "false" or "0" => false,
            _ => throw new InvalidParameterException("include_closed must be a boolean")
        };
    }

    private static string? OptionalString(string? value)
    {
        return string.IsNullOrEmpty(value) ? null : value;
    }
}
