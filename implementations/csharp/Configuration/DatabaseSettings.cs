using Npgsql;

namespace BackendApiLanguageLab.CSharp.Configuration;

/// <summary>PostgreSQL connection settings.</summary>
public sealed record DatabaseSettings(string ConnectionString);

/// <summary>Converts the shared PostgreSQL URL into Npgsql settings.</summary>
public static class DatabaseConnectionString
{
    /// <summary>Converts a postgres:// URL into an Npgsql key-value string.</summary>
    public static string FromUrl(string databaseUrl)
    {
        if (!Uri.TryCreate(databaseUrl, UriKind.Absolute, out var uri)
            || (uri.Scheme != "postgres" && uri.Scheme != "postgresql")
            || string.IsNullOrEmpty(uri.UserInfo))
        {
            throw new InvalidOperationException("DATABASE_URL must be a valid PostgreSQL URL");
        }

        var credentials = uri.UserInfo.Split(':', 2);
        if (credentials.Length != 2)
        {
            throw new InvalidOperationException("DATABASE_URL must include username and password");
        }

        var builder = new NpgsqlConnectionStringBuilder
        {
            Host = uri.Host,
            Port = uri.IsDefaultPort ? 5432 : uri.Port,
            Database = uri.AbsolutePath.TrimStart('/'),
            Username = Uri.UnescapeDataString(credentials[0]),
            Password = Uri.UnescapeDataString(credentials[1])
        };

        var sslMode = GetQueryValue(uri, "sslmode");
        if (string.Equals(sslMode, "disable", StringComparison.OrdinalIgnoreCase))
        {
            builder.SslMode = SslMode.Disable;
        }

        return builder.ConnectionString;
    }

    private static string? GetQueryValue(Uri uri, string key)
    {
        var queryParts = uri.Query.TrimStart('?').Split('&', StringSplitOptions.RemoveEmptyEntries);
        foreach (var part in queryParts)
        {
            var pair = part.Split('=', 2);
            if (pair.Length == 2 && pair[0].Equals(key, StringComparison.OrdinalIgnoreCase))
            {
                return Uri.UnescapeDataString(pair[1]);
            }
        }

        return null;
    }
}
