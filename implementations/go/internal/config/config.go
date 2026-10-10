// config.go 定義並載入 API Server 的環境變數設定。
package config

import (
	"fmt"
	"os"
)

// Config 是 API Server 的執行設定。
type Config struct {
	// DatabaseURL 是 PostgreSQL connection URL。
	DatabaseURL string
	// ServerPort 是 HTTP Server 監聽的 port。
	ServerPort string
}

// Load 從環境變數載入 API Server 設定。
func Load() (Config, error) {
	databaseURL := os.Getenv("DATABASE_URL")
	if databaseURL == "" {
		return Config{}, fmt.Errorf("DATABASE_URL is required")
	}

	serverPort := os.Getenv("SERVER_PORT")
	if serverPort == "" {
		serverPort = "8080"
	}

	return Config{
		DatabaseURL: databaseURL,
		ServerPort:  serverPort,
	}, nil
}
