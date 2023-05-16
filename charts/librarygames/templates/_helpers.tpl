{{/*
Expand the name of the chart.
*/}}
{{- define "librarygames.name" -}}
{{- default .Chart.Name .Values.librarygames.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "librarygames.fullname" -}}
{{- if .Values.librarygames.fullnameOverride }}
{{- .Values.librarygames.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.librarygames.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "librarygames.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "librarygames.labels" -}}
helm.sh/chart: {{ include "librarygames.chart" . }}
{{ include "librarygames.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "librarygames.selectorLabels" -}}
app.kubernetes.io/name: {{ include "librarygames.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "librarygames.serviceAccountName" -}}
{{- if .Values.librarygames.serviceAccount.create }}
{{- default (include "librarygames.fullname" .) .Values.librarygames.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.librarygames.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Create the name of the service monitor to use
*/}}
{{- define "librarygames.serviceMonitorName" -}}
{{ include "librarygames.fullname" . }}-service-monitor
{{- end }}

{{/*
LibraryGames MySQL server address
*/}}
{{- define "librarygames.mysqlAddress" -}}
{{- if .Values.global.mysql.enabled }}
  {{- $address := print .Release.Name "-mysql:" .Values.mysql.primary.service.ports.mysql }}
  {{- (dig "librarygames" "mysql" "address" (printf "%s" $address) (.Values | merge (dict))) | default (printf "%s" $address) }}
{{- else }}
  {{- default "" .Values.librarygames.mysql.address }}
{{- end }}
{{- end }}

{{/*
Template environment variables to inject a containers resources
*/}}
{{- define "kubernetes.envContainerResources" -}}
- name: K8S_CPU_REQUEST
  valueFrom:
    resourceFieldRef:
      containerName: {{ .container }}
      resource: requests.cpu
- name: K8S_CPU_LIMIT
  valueFrom:
    resourceFieldRef:
      containerName: {{ .container }}
      resource: limits.cpu
- name: K8S_MEM_REQUEST
  valueFrom:
    resourceFieldRef:
      containerName: {{ .container }}
      resource: requests.memory
- name: K8S_MEM_LIMIT
  valueFrom:
    resourceFieldRef:
      containerName: {{ .container }}
      resource: limits.memory
{{- end }}

{{/*
Template environment variables to inject a containers metadata
*/}}
{{- define "kubernetes.envPodInformation" -}}
- name: K8S_NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
- name: K8S_NODE_IP
  valueFrom:
    fieldRef:
      fieldPath: status.hostIP
- name: K8S_POD_IP
  valueFrom:
    fieldRef:
      fieldPath: status.podIP
- name: K8S_POD_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
{{- end }}

{{/*
Template environment variables to inject into server
*/}}
{{- define "librarygames.envVars" -}}
- name: LIBRARY_GAMES_SERVER
  value: "true"
- name: MYSQL_SERVER_ADDRESS
  value: "{{ include "librarygames.mysqlAddress" . }}"
- name: MYSQL_DATABASE_USERNAME
  value: "{{ .Values.mysql.auth.username }}"
- name: MYSQL_DATABASE_PASSWORD
  value: "{{ .Values.mysql.auth.password }}"
- name: LIBRARYGAMES_GRPC_PORT
  value: "{{ .Values.librarygames.service.port }}"
{{- end }}
