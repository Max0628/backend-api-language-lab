// response.go 定義職缺 API response DTO 與 Domain Model mapping。
package jobs

import "time"

// JobResponse 是 GET /jobs 回傳的職缺 DTO。
type JobResponse struct {
	// Source 是職缺來源平台。
	Source string `json:"source"`
	// SourceJobID 是來源平台提供的職缺識別碼。
	SourceJobID string `json:"source_job_id"`
	// Title 是職缺標題。
	Title string `json:"title"`
	// Company 是公司名稱，可能為 null。
	Company *string `json:"company"`
	// URL 是職缺詳細頁 URL。
	URL string `json:"url"`
	// PostedAt 是職缺刊登或更新時間，可能為 null。
	PostedAt *time.Time `json:"posted_at"`
	// Salary 是薪資資訊。
	Salary SalaryResponse `json:"salary"`
	// Location 是地點資訊。
	Location LocationResponse `json:"location"`
}

// SalaryResponse 是 API response 使用的薪資 DTO。
type SalaryResponse struct {
	// Min 是最低薪資，可能為 null。
	Min *int64 `json:"min"`
	// Max 是最高薪資，可能為 null。
	Max *int64 `json:"max"`
	// Currency 是薪資使用的 currency code，可能為 null。
	Currency *string `json:"currency"`
}

// LocationResponse 是 API response 使用的地點 DTO。
type LocationResponse struct {
	// City 是城市或縣市名稱，可能為 null。
	City *string `json:"city"`
	// District 是區或鄉鎮市名稱，可能為 null。
	District *string `json:"district"`
}

// toJobResponse 將職缺 Domain Model 轉換成 API response DTO。
func toJobResponse(job Job) JobResponse {
	return JobResponse{
		Source:      job.Source,
		SourceJobID: job.SourceJobID,
		Title:       job.Title,
		Company:     job.Company,
		URL:         job.URL,
		PostedAt:    job.PostedAt,
		Salary: SalaryResponse{
			Min:      job.Salary.Min,
			Max:      job.Salary.Max,
			Currency: job.Salary.Currency,
		},
		Location: LocationResponse{
			City:     job.Location.City,
			District: job.Location.District,
		},
	}
}
