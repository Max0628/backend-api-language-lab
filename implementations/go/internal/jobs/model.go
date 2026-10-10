// model.go 定義職缺 Domain Model 與查詢條件。
package jobs

import "time"

// Job 是職缺查詢使用的 Domain Model，不包含 API 或 database/sql 細節。
type Job struct {
	// Source 是職缺來源平台。
	Source string
	// SourceJobID 是來源平台提供的職缺識別碼。
	SourceJobID string
	// Title 是 normalized 職缺標題。
	Title string
	// Company 是公司名稱，可能為 nil。
	Company *string
	// URL 是職缺詳細頁或來源頁面的 URL。
	URL string
	// PostedAt 是來源提供的刊登或更新時間，可能為 nil。
	PostedAt *time.Time
	// Salary 是職缺的薪資資訊。
	Salary Salary
	// Location 是職缺的地點資訊。
	Location Location
}

// Salary 是職缺的薪資資訊。
type Salary struct {
	// Min 是最低薪資，可能為 nil。
	Min *int64
	// Max 是最高薪資，可能為 nil。
	Max *int64
	// Currency 是薪資使用的 currency code，可能為 nil。
	Currency *string
}

// Location 是職缺的地點資訊。
type Location struct {
	// City 是城市或縣市名稱，可能為 nil。
	City *string
	// District 是區或鄉鎮市名稱，可能為 nil。
	District *string
}

// ListJobsQuery 是職缺列表查詢的條件。
type ListJobsQuery struct {
	// Page 是從 1 開始的頁碼。
	Page int
	// PageSize 是每頁回傳的資料筆數。
	PageSize int
	// Source 是來源平台篩選條件。
	Source string
	// City 是城市或縣市篩選條件。
	City string
	// District 是區或鄉鎮市篩選條件。
	District string
	// EmploymentType 是 employment type 篩選條件。
	EmploymentType string
	// JobType 是 job type 篩選條件。
	JobType string
	// Title 是職缺標題的 full-text search 條件。
	Title string
	// IncludeClosed 指定是否包含 CLOSED 狀態的職缺。
	IncludeClosed bool
	// SortBy 是排序欄位。
	SortBy string
	// SortOrder 是排序方向，可為 asc 或 desc。
	SortOrder string
}
