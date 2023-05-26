package util

import (
	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/trace"
)

const (
	libraryGamesTracer = "github.com/patrickkenney9801/librarygames"
)

func GetTracer() trace.Tracer {
	return otel.GetTracerProvider().Tracer(libraryGamesTracer)
}

func EndSpan(span trace.Span, err *error) {
	if err != nil {
		span.RecordError(*err)
	}
	span.End()
}
