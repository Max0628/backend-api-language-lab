// handler_test.go 驗證職缺 Handler 的 HTTP status 與 JSON response 行為。
package jobs

import (
	"errors"
	"io"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
)

// TestHandlerListReturnsJSONList 驗證成功查詢會回傳 JSON list。
func TestHandlerListReturnsJSONList(t *testing.T) {
	gin.SetMode(gin.TestMode)

	repository := &fakeRepository{
		jobs: []Job{{
			Source:      "example",
			SourceJobID: "job-1",
			Title:       "Backend Engineer",
			URL:         "https://example.com/jobs/job-1",
		}},
	}
	router := gin.New()
	NewHandler(NewService(repository), testLogger()).RegisterRoutes(router)

	request := httptest.NewRequest(http.MethodGet, "/jobs", nil)
	recorder := httptest.NewRecorder()
	router.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusOK {
		t.Fatalf("status = %d, want %d", recorder.Code, http.StatusOK)
	}
	if !strings.Contains(recorder.Body.String(), `"source":"example"`) {
		t.Fatalf("response body = %s, want source field", recorder.Body.String())
	}
}

// TestHandlerListRejectsInvalidPageSize 驗證 invalid page size 會回傳 400。
func TestHandlerListRejectsInvalidPageSize(t *testing.T) {
	gin.SetMode(gin.TestMode)

	router := gin.New()
	NewHandler(NewService(&fakeRepository{}), testLogger()).RegisterRoutes(router)

	request := httptest.NewRequest(http.MethodGet, "/jobs?page_size=20", nil)
	recorder := httptest.NewRecorder()
	router.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusBadRequest {
		t.Fatalf("status = %d, want %d", recorder.Code, http.StatusBadRequest)
	}
}

// TestHandlerListReturnsInternalServerError 驗證內部錯誤會回傳 500 且不暴露細節。
func TestHandlerListReturnsInternalServerError(t *testing.T) {
	gin.SetMode(gin.TestMode)

	router := gin.New()
	NewHandler(NewService(&fakeRepository{err: errors.New("database unavailable")}), testLogger()).RegisterRoutes(router)

	request := httptest.NewRequest(http.MethodGet, "/jobs", nil)
	recorder := httptest.NewRecorder()
	router.ServeHTTP(recorder, request)

	if recorder.Code != http.StatusInternalServerError {
		t.Fatalf("status = %d, want %d", recorder.Code, http.StatusInternalServerError)
	}
	if strings.Contains(recorder.Body.String(), "database unavailable") {
		t.Fatalf("response exposed internal error: %s", recorder.Body.String())
	}
}

// testLogger 建立不輸出內容的測試 logger。
func testLogger() *slog.Logger {
	return slog.New(slog.NewTextHandler(io.Discard, nil))
}

var _ Repository = (*fakeRepository)(nil)
