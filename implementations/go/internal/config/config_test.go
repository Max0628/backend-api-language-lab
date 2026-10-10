// config_test.go 驗證環境變數設定的載入與預設值。
package config

import "testing"

// TestLoadRequiresDatabaseURL 驗證缺少 DATABASE_URL 時會回傳錯誤。
func TestLoadRequiresDatabaseURL(t *testing.T) {
	t.Setenv("DATABASE_URL", "")

	_, err := Load()
	if err == nil {
		t.Fatal("Load() error = nil, want an error")
	}
}

// TestLoadUsesDefaultServerPort 驗證未設定 SERVER_PORT 時使用 8080。
func TestLoadUsesDefaultServerPort(t *testing.T) {
	t.Setenv("DATABASE_URL", "postgres://localhost/jobs")
	t.Setenv("SERVER_PORT", "")

	got, err := Load()
	if err != nil {
		t.Fatalf("Load() error = %v", err)
	}

	if got.ServerPort != "8080" {
		t.Fatalf("ServerPort = %q, want %q", got.ServerPort, "8080")
	}
}
