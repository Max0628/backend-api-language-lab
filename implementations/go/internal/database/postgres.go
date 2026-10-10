// postgres.go 建立並設定 PostgreSQL 的 database/sql connection pool。
package database

import (
	"database/sql"

	_ "github.com/jackc/pgx/v5/stdlib"
)

// Open 建立使用 pgx driver 的 database/sql connection pool。
func Open(databaseURL string) (*sql.DB, error) {
	db, err := sql.Open("pgx", databaseURL)
	if err != nil {
		return nil, err
	}

	db.SetMaxOpenConns(25)
	db.SetMaxIdleConns(10)

	return db, nil
}
