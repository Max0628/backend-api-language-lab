using Npgsql;

namespace BackendApiLanguageLab.CSharp.Jobs;

/// <summary>PostgreSQL repository using Npgsql parameterized SQL.</summary>
public sealed class PostgresJobsRepository : IJobsRepository
{
    private readonly NpgsqlDataSource _dataSource;

    /// <summary>Creates a repository using the shared Npgsql data source.</summary>
    public PostgresJobsRepository(NpgsqlDataSource dataSource)
    {
        _dataSource = dataSource;
    }

    /// <summary>Executes the jobs query and maps rows into domain models.</summary>
    public async Task<IReadOnlyList<Job>> ListAsync(
        ListJobsQuery query,
        CancellationToken cancellationToken)
    {
        await using var connection = await _dataSource.OpenConnectionAsync(cancellationToken);
        await using var command = connection.CreateCommand();
        command.CommandText = BuildCommandText(query);
        AddParameters(command, query);

        var jobs = new List<Job>(query.PageSize);
        await using var reader = await command.ExecuteReaderAsync(cancellationToken);
        while (await reader.ReadAsync(cancellationToken))
        {
            jobs.Add(ReadJob(reader));
        }

        return jobs;
    }

    private static string BuildCommandText(ListJobsQuery query)
    {
        var conditions = new List<string> { "TRUE" };
        if (!query.IncludeClosed)
        {
            conditions.Add("status <> 'CLOSED'");
        }

        AddFilter(conditions, "source", query.Source);
        AddFilter(conditions, "city", query.City);
        AddFilter(conditions, "district", query.District);
        AddFilter(conditions, "employment_type", query.EmploymentType);
        AddFilter(conditions, "job_type", query.JobType);
        if (!string.IsNullOrEmpty(query.Title))
        {
            conditions.Add("to_tsvector('english', title) @@ plainto_tsquery('english', @title)");
        }

        var direction = query.SortOrder == SortOrder.Asc ? "ASC" : "DESC";
        var nulls = query.SortBy == "posted_at" ? " NULLS LAST" : string.Empty;
        var offset = (query.Page - 1) * query.PageSize;

        return $"""
            SELECT source, source_job_id, title, company, url,
                   posted_at, salary_min, salary_max, salary_currency,
                   city, district
            FROM jobs
            WHERE {string.Join(" AND ", conditions)}
            ORDER BY {SortColumn(query.SortBy)} {direction}{nulls}, id {direction}
            LIMIT @page_size OFFSET @offset
            """;
    }

    private static void AddParameters(NpgsqlCommand command, ListJobsQuery query)
    {
        AddParameter(command, "source", query.Source);
        AddParameter(command, "city", query.City);
        AddParameter(command, "district", query.District);
        AddParameter(command, "employment_type", query.EmploymentType);
        AddParameter(command, "job_type", query.JobType);
        AddParameter(command, "title", query.Title);
        command.Parameters.AddWithValue("page_size", query.PageSize);
        command.Parameters.AddWithValue("offset", (query.Page - 1) * query.PageSize);
    }

    private static void AddFilter(List<string> conditions, string column, string? value)
    {
        if (!string.IsNullOrEmpty(value))
        {
            conditions.Add($"{column} = @{column}");
        }
    }

    private static void AddParameter(NpgsqlCommand command, string name, string? value)
    {
        if (!string.IsNullOrEmpty(value))
        {
            command.Parameters.AddWithValue(name, value);
        }
    }

    private static string SortColumn(string sortBy)
    {
        return sortBy switch
        {
            "posted_at" => "posted_at",
            "first_seen_at" => "first_seen_at",
            "last_seen_at" => "last_seen_at",
            "salary_min" => "salary_min",
            "salary_max" => "salary_max",
            "title" => "title",
            "company" => "company",
            _ => throw new InvalidOperationException("sort column must be validated")
        };
    }

    private static Job ReadJob(NpgsqlDataReader reader)
    {
        return new Job(
            reader.GetFieldValue<string>(0),
            reader.GetFieldValue<string>(1),
            reader.GetFieldValue<string>(2),
            NullableString(reader, 3),
            reader.GetFieldValue<string>(4),
            NullableDateTime(reader, 5),
            new Salary(
                NullableInt64(reader, 6),
                NullableInt64(reader, 7),
                NullableString(reader, 8)),
            new Location(NullableString(reader, 9), NullableString(reader, 10)));
    }

    private static string? NullableString(NpgsqlDataReader reader, int ordinal)
    {
        return reader.IsDBNull(ordinal) ? null : reader.GetFieldValue<string>(ordinal);
    }

    private static long? NullableInt64(NpgsqlDataReader reader, int ordinal)
    {
        return reader.IsDBNull(ordinal) ? null : reader.GetFieldValue<long>(ordinal);
    }

    private static DateTime? NullableDateTime(NpgsqlDataReader reader, int ordinal)
    {
        return reader.IsDBNull(ordinal) ? null : reader.GetFieldValue<DateTime>(ordinal);
    }
}
