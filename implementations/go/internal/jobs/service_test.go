// service_test.go 驗證職缺 Service 的預設值與查詢條件驗證。
package jobs

import (
	"context"
	"errors"
	"testing"
)

type fakeRepository struct {
	jobs  []Job
	query ListJobsQuery
	err   error
}

// List 回傳 fake repository 的資料並記錄收到的查詢條件。
func (f *fakeRepository) List(_ context.Context, query ListJobsQuery) ([]Job, error) {
	f.query = query
	return f.jobs, f.err
}

// TestServiceListAppliesDefaults 驗證 Service 會套用查詢預設值。
func TestServiceListAppliesDefaults(t *testing.T) {
	repository := &fakeRepository{}
	service := NewService(repository)

	_, err := service.List(context.Background(), ListJobsQuery{})
	if err != nil {
		t.Fatalf("List() error = %v", err)
	}

	if repository.query.Page != 1 || repository.query.PageSize != 30 {
		t.Fatalf("pagination = %d/%d, want 1/30", repository.query.Page, repository.query.PageSize)
	}
	if repository.query.SortBy != "posted_at" || repository.query.SortOrder != "desc" {
		t.Fatalf("sort = %s/%s, want posted_at/desc", repository.query.SortBy, repository.query.SortOrder)
	}
}

// TestServiceListRejectsInvalidPageSize 驗證 Service 會拒絕不合法的 page size。
func TestServiceListRejectsInvalidPageSize(t *testing.T) {
	service := NewService(&fakeRepository{})

	_, err := service.List(context.Background(), ListJobsQuery{PageSize: 20})
	if !errors.Is(err, ErrInvalidParameter) {
		t.Fatalf("List() error = %v, want ErrInvalidParameter", err)
	}
}
