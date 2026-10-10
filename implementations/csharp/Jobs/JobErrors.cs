namespace BackendApiLanguageLab.CSharp.Jobs;

/// <summary>Represents a query parameter validation failure.</summary>
public sealed class InvalidParameterException : Exception
{
    /// <summary>Creates an invalid parameter exception.</summary>
    public InvalidParameterException(string message) : base(message) { }
}

/// <summary>Standard API error response.</summary>
public sealed record ErrorResponse(ErrorDetails Error);

/// <summary>Machine-readable error details.</summary>
public sealed record ErrorDetails(string Code, string Message);
