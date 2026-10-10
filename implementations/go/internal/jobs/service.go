// service.go 實作職缺查詢的驗證與 application flow。
package jobs

import (
	"context"
	"errors"
)

// ErrInvalidParameter 表示 API query parameter 不符合規格。
var ErrInvalidParameter = errors.New("invalid parameter")

// Service 實作職缺查詢的業務流程。
type Service struct {
	repository Repository
}

// NewService 建立職缺 Service。
func NewService(repository Repository) *Service {
	return &Service{repository: repository}
}

// List 驗證查詢條件並透過 Repository 取得職缺列表。
func (s *Service) List(ctx context.Context, query ListJobsQuery) ([]Job, error) {
	if err := validateListJobsQuery(&query); err != nil {
		return nil, err
	}

	return s.repository.List(ctx, query)
}

// validateListJobsQuery 套用查詢預設值並驗證查詢條件。
func validateListJobsQuery(query *ListJobsQuery) error {
	if query.Page == 0 {
		query.Page = 1
	}
	if query.PageSize == 0 {
		query.PageSize = 30
	}
	if query.Page < 1 {
		return errors.Join(ErrInvalidParameter, errors.New("page must be at least 1"))
	}
	if query.PageSize < 30 || query.PageSize > 50 {
		return errors.Join(ErrInvalidParameter, errors.New("page_size must be between 30 and 50"))
	}
	if query.SortBy == "" {
		query.SortBy = "posted_at"
	}
	if !isAllowedSortBy(query.SortBy) {
		return errors.Join(ErrInvalidParameter, errors.New("invalid sort_by"))
	}
	if query.SortOrder == "" {
		query.SortOrder = "desc"
	}
	if query.SortOrder != "asc" && query.SortOrder != "desc" {
		return errors.Join(ErrInvalidParameter, errors.New("sort_order must be asc or desc"))
	}
	return nil
}

// isAllowedSortBy 判斷排序欄位是否在允許的 whitelist 中。
func isAllowedSortBy(value string) bool {
	switch value {
	case "posted_at", "first_seen_at", "last_seen_at", "salary_min", "salary_max", "title", "company":
		return true
	default:
		return false
	}
}
