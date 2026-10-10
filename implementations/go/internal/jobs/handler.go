// handler.go 負責職缺 API 的 HTTP request parsing、response 與 error mapping。
package jobs

import (
	"errors"
	"log/slog"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
)

// Handler 負責職缺 API 的 HTTP request handling。
type Handler struct {
	service *Service
	logger  *slog.Logger
}

// NewHandler 建立職缺 API Handler。
func NewHandler(service *Service, logger *slog.Logger) *Handler {
	return &Handler{service: service, logger: logger}
}

// RegisterRoutes 將職缺 API route 註冊到 Gin router。
func (h *Handler) RegisterRoutes(router gin.IRouter) {
	router.GET("/jobs", h.list)
}

// list 處理 GET /jobs request。
func (h *Handler) list(c *gin.Context) {
	query, err := parseListJobsQuery(c)
	if err != nil {
		writeError(c, http.StatusBadRequest, err)
		return
	}

	jobs, err := h.service.List(c.Request.Context(), query)
	if err != nil {
		if errors.Is(err, ErrInvalidParameter) {
			writeError(c, http.StatusBadRequest, err)
			return
		}
		h.logger.ErrorContext(c.Request.Context(), "list jobs failed", "error", err)
		writeError(c, http.StatusInternalServerError, errors.New("failed to query jobs"))
		return
	}

	response := make([]JobResponse, 0, len(jobs))
	for _, job := range jobs {
		response = append(response, toJobResponse(job))
	}

	c.JSON(http.StatusOK, response)
}

// parseListJobsQuery 將 HTTP query parameters 解析成 ListJobsQuery。
func parseListJobsQuery(c *gin.Context) (ListJobsQuery, error) {
	query := ListJobsQuery{
		Source:         c.Query("source"),
		City:           c.Query("city"),
		District:       c.Query("district"),
		EmploymentType: c.Query("employment_type"),
		JobType:        c.Query("job_type"),
		Title:          c.Query("title"),
		SortBy:         c.Query("sort_by"),
		SortOrder:      c.Query("sort_order"),
	}

	var err error
	query.Page, err = parseOptionalInt(c, "page")
	if err != nil {
		return ListJobsQuery{}, err
	}
	query.PageSize, err = parseOptionalInt(c, "page_size")
	if err != nil {
		return ListJobsQuery{}, err
	}
	query.IncludeClosed, err = parseOptionalBool(c, "include_closed")
	if err != nil {
		return ListJobsQuery{}, err
	}

	return query, nil
}

// parseOptionalInt 解析 optional integer query parameter。
func parseOptionalInt(c *gin.Context, name string) (int, error) {
	value := c.Query(name)
	if value == "" {
		return 0, nil
	}

	parsed, err := strconv.Atoi(value)
	if err != nil {
		return 0, errors.New(name + " must be an integer")
	}
	return parsed, nil
}

// parseOptionalBool 解析 optional boolean query parameter。
func parseOptionalBool(c *gin.Context, name string) (bool, error) {
	value := c.Query(name)
	if value == "" {
		return false, nil
	}

	parsed, err := strconv.ParseBool(value)
	if err != nil {
		return false, errors.New(name + " must be a boolean")
	}
	return parsed, nil
}

// writeError 將 application error 寫成 API error response。
func writeError(c *gin.Context, status int, err error) {
	c.JSON(status, gin.H{
		"error": gin.H{
			"code":    errorCode(status),
			"message": err.Error(),
		},
	})
}

// errorCode 將 HTTP status 轉換成 API error code。
func errorCode(status int) string {
	if status == http.StatusBadRequest {
		return "INVALID_PARAMETER"
	}
	return "INTERNAL_SERVER_ERROR"
}
