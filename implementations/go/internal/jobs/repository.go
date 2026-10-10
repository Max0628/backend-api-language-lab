// repository.go 定義職缺資料存取介面與 PostgreSQL 實作。
package jobs

import (
	"context"
	"database/sql"
	"fmt"
	"strings"
	"time"
)

// Repository 定義職缺資料存取行為。
type Repository interface {
	// List 依照查詢條件回傳職缺列表。
	List(ctx context.Context, query ListJobsQuery) ([]Job, error)
}

// PostgresRepository 使用 PostgreSQL 實作 Repository。
type PostgresRepository struct {
	db *sql.DB
}

// NewPostgresRepository 建立 PostgreSQL Repository。
func NewPostgresRepository(db *sql.DB) *PostgresRepository {
	return &PostgresRepository{db: db}
}

// List 從 jobs table 查詢符合條件的職缺。
func (r *PostgresRepository) List(ctx context.Context, queryParams ListJobsQuery) ([]Job, error) {
	query := strings.Builder{}
	query.WriteString(`
		SELECT source, source_job_id, title, company, url,
		       posted_at, salary_min, salary_max, salary_currency,
		       city, district
		FROM jobs
		WHERE 1 = 1`)

	args := make([]any, 0, 8)
	addCondition := func(condition string, value any) {
		args = append(args, value)
		query.WriteString(fmt.Sprintf(" AND %s$%d", condition, len(args)))
	}

	if !queryParams.IncludeClosed {
		query.WriteString(" AND status <> 'CLOSED'")
	}
	if queryParams.Source != "" {
		addCondition("source = ", queryParams.Source)
	}
	if queryParams.City != "" {
		addCondition("city = ", queryParams.City)
	}
	if queryParams.District != "" {
		addCondition("district = ", queryParams.District)
	}
	if queryParams.EmploymentType != "" {
		addCondition("employment_type = ", queryParams.EmploymentType)
	}
	if queryParams.JobType != "" {
		addCondition("job_type = ", queryParams.JobType)
	}
	if queryParams.Title != "" {
		args = append(args, queryParams.Title)
		query.WriteString(fmt.Sprintf(
			" AND to_tsvector('english', title) @@ plainto_tsquery('english', $%d)",
			len(args),
		))
	}

	query.WriteString(" ORDER BY ")
	query.WriteString(sortColumn(queryParams.SortBy))
	query.WriteByte(' ')
	query.WriteString(strings.ToUpper(queryParams.SortOrder))
	if queryParams.SortBy == "posted_at" {
		query.WriteString(" NULLS LAST")
	}
	query.WriteString(", id ")
	query.WriteString(strings.ToUpper(queryParams.SortOrder))
	query.WriteString(" LIMIT $")
	args = append(args, queryParams.PageSize)
	query.WriteString(fmt.Sprint(len(args)))
	query.WriteString(" OFFSET $")
	args = append(args, (queryParams.Page-1)*queryParams.PageSize)
	query.WriteString(fmt.Sprint(len(args)))

	rows, err := r.db.QueryContext(ctx, query.String(), args...)
	if err != nil {
		return nil, fmt.Errorf("list jobs: %w", err)
	}
	defer rows.Close()

	result := make([]Job, 0, queryParams.PageSize)
	for rows.Next() {
		var row jobRow

		if err := rows.Scan(
			&row.source,
			&row.sourceJobID,
			&row.title,
			&row.company,
			&row.url,
			&row.postedAt,
			&row.salaryMin,
			&row.salaryMax,
			&row.salaryCurrency,
			&row.city,
			&row.district,
		); err != nil {
			return nil, fmt.Errorf("scan job: %w", err)
		}
		result = append(result, row.toDomain())
	}
	if err := rows.Err(); err != nil {
		return nil, fmt.Errorf("iterate jobs: %w", err)
	}

	return result, nil
}

type jobRow struct {
	source         string
	sourceJobID    string
	title          string
	company        sql.NullString
	url            string
	postedAt       sql.NullTime
	salaryMin      sql.NullInt64
	salaryMax      sql.NullInt64
	salaryCurrency sql.NullString
	city           sql.NullString
	district       sql.NullString
}

// toDomain 將 database row 轉換成不依賴 database/sql 的 Domain Model。
func (r jobRow) toDomain() Job {
	return Job{
		Source:      r.source,
		SourceJobID: r.sourceJobID,
		Title:       r.title,
		Company:     nullableString(r.company),
		URL:         r.url,
		PostedAt:    nullableTime(r.postedAt),
		Salary: Salary{
			Min:      nullableInt64(r.salaryMin),
			Max:      nullableInt64(r.salaryMax),
			Currency: nullableString(r.salaryCurrency),
		},
		Location: Location{
			City:     nullableString(r.city),
			District: nullableString(r.district),
		},
	}
}

// sortColumn 將 API 支援的排序名稱轉換成安全的 SQL 欄位名稱。
func sortColumn(sortBy string) string {
	columns := map[string]string{
		"posted_at":     "posted_at",
		"first_seen_at": "first_seen_at",
		"last_seen_at":  "last_seen_at",
		"salary_min":    "salary_min",
		"salary_max":    "salary_max",
		"title":         "title",
		"company":       "company",
	}
	return columns[sortBy]
}

// nullableString 將 sql.NullString 轉換成 nullable string pointer。
func nullableString(value sql.NullString) *string {
	if !value.Valid {
		return nil
	}
	return &value.String
}

// nullableInt64 將 sql.NullInt64 轉換成 nullable int64 pointer。
func nullableInt64(value sql.NullInt64) *int64 {
	if !value.Valid {
		return nil
	}
	return &value.Int64
}

// nullableTime 將 sql.NullTime 轉換成 nullable time pointer。
func nullableTime(value sql.NullTime) *time.Time {
	if !value.Valid {
		return nil
	}
	return &value.Time
}
