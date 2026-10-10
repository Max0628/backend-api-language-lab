// ASP.NET Core API server entrypoint and dependency composition root.
using BackendApiLanguageLab.CSharp.Configuration;
using BackendApiLanguageLab.CSharp.Jobs;
using Npgsql;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();

var databaseUrl = builder.Configuration["DATABASE_URL"]
    ?? throw new InvalidOperationException("DATABASE_URL is required");
var dataSource = NpgsqlDataSource.Create(DatabaseConnectionString.FromUrl(databaseUrl));
builder.Services.AddSingleton(dataSource);
builder.Services.AddScoped<IJobsRepository, PostgresJobsRepository>();
builder.Services.AddScoped<JobsService>();

var app = builder.Build();

app.MapControllers();
app.Run();

await dataSource.DisposeAsync();
