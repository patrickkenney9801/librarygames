package util

import (
	pbs "github.com/patrickkenney9801/librarygames/internal/pbs/v1"
	healthpb "google.golang.org/grpc/health/grpc_health_v1"
)

var (
	PublicGRPCMethods = map[string]bool{
		healthpb.Health_Check_FullMethodName:           true,
		pbs.Login_Login_FullMethodName:                 true,
		pbs.CreateAccount_CreateAccount_FullMethodName: true,
	}
)

func GetGameType(gameType int32) pbs.GameType {
	switch gameType {
	case int32(pbs.GameType_GO_9X9):
		return pbs.GameType_GO_9X9
	case int32(pbs.GameType_GO_13X13):
		return pbs.GameType_GO_13X13
	case int32(pbs.GameType_GO_19X19):
		return pbs.GameType_GO_19X19
	default:
		return pbs.GameType_UNSPECIFIED
	}
}

func GetIntGameType(gameType pbs.GameType) int32 {
	return int32(gameType)
}
