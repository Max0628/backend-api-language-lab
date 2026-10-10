// main.go 組裝 API Server 的 dependencies，並負責啟動與關閉 HTTP Server。
package main

import (
	"context"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gin-gonic/gin"

	"github.com/Max0628/backend-api-language-lab/implementations/go/internal/config"
	"github.com/Max0628/backend-api-language-lab/implementations/go/internal/database"
	"github.com/Max0628/backend-api-language-lab/implementations/go/internal/jobs"
	"github.com/Max0628/backend-api-language-lab/implementations/go/internal/middleware"
)

// main 啟動 API Server，並在收到 shutdown signal 時優雅關閉。
func main() {
	logger := slog.New(slog.NewTextHandler(os.Stdout, &slog.HandlerOptions{
		Level: slog.LevelInfo,
	}))

	cfg, err := config.Load()
	if err != nil {
		logger.Error("load config failed", "error", err)
		os.Exit(1)
	}

	db, err := database.Open(cfg.DatabaseURL)
	if err != nil {
		logger.Error("open database failed", "error", err)
		os.Exit(1)
	}
	defer db.Close()

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := db.PingContext(ctx); err != nil {
		logger.Error("ping database failed", "error", err)
		os.Exit(1)
	}

	repository := jobs.NewPostgresRepository(db)
	service := jobs.NewService(repository)
	handler := jobs.NewHandler(service, logger)

	gin.SetMode(gin.ReleaseMode)
	router := gin.New()
	router.Use(middleware.RequestLogger(logger))
	router.Use(middleware.Recovery(logger))
	handler.RegisterRoutes(router)

	server := &http.Server{
		Addr:              ":" + cfg.ServerPort,
		Handler:           router,
		ReadTimeout:       10 * time.Second,
		ReadHeaderTimeout: 5 * time.Second,
		WriteTimeout:      10 * time.Second,
		IdleTimeout:       60 * time.Second,
	}

	logger.Info("server listening", "address", server.Addr)

	serverErrors := make(chan error, 1)
	go func() {
		serverErrors <- server.ListenAndServe()
	}()

	signals := make(chan os.Signal, 1)
	signal.Notify(signals, os.Interrupt, syscall.SIGTERM)
	defer signal.Stop(signals)

	select {
	case err := <-serverErrors:
		if err != nil && err != http.ErrServerClosed {
			logger.Error("server stopped unexpectedly", "error", err)
			os.Exit(1)
		}
	case sig := <-signals:
		logger.Info("shutdown signal received", "signal", sig.String())
		shutdownContext, shutdownCancel := context.WithTimeout(context.Background(), 5*time.Second)
		defer shutdownCancel()

		if err := server.Shutdown(shutdownContext); err != nil {
			logger.Error("server shutdown failed", "error", err)
			os.Exit(1)
		}
		logger.Info("server stopped")
	}
}
