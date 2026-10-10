using Microsoft.AspNetCore.Mvc;

namespace BackendApiLanguageLab.CSharp.Jobs;

/// <summary>HTTP controller for the jobs read-only API.</summary>
[ApiController]
[Route("jobs")]
public sealed class JobsController : ControllerBase
{
    private readonly JobsService _service;
    private readonly ILogger<JobsController> _logger;

    /// <summary>Creates the controller with constructor-injected dependencies.</summary>
    public JobsController(JobsService service, ILogger<JobsController> logger)
    {
        _service = service;
        _logger = logger;
    }

    /// <summary>Returns jobs matching the query parameters.</summary>
    [HttpGet]
    public async Task<ActionResult<IReadOnlyList<JobResponse>>> ListAsync(
        CancellationToken cancellationToken)
    {
        try
        {
            var jobs = await _service.ListAsync(Request.Query, cancellationToken);
            return Ok(jobs.Select(JobResponseMapper.ToResponse).ToList());
        }
        catch (InvalidParameterException exception)
        {
            return BadRequest(new ErrorResponse(
                new ErrorDetails("INVALID_PARAMETER", exception.Message)));
        }
        catch (Exception exception)
        {
            _logger.LogError(exception, "List jobs failed");
            return StatusCode(500, new ErrorResponse(
                new ErrorDetails("INTERNAL_SERVER_ERROR", "failed to query jobs")));
        }
    }
}
