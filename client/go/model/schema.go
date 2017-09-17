package model

import (
	"encoding/json"
	"fmt"
	"strconv"
)

type Schema struct {
	Id      int64  `json:"id"`
	Subject string `json:"subject"`
	Version SemVer `json:"version"`
	Schema  string `json:"schema"`
}

func (s Schema) MarshalJSON() ([]byte, error) {
	return json.Marshal(map[string]interface{}{
		"id":      s.Id,
		"subject": s.Subject,
		"version": s.Version.String(),
		"schema":  s.Schema,
	})
}

func (s *Schema) UnmarshalJSON(data []byte) error {
	intermediate := make(map[string]string)
	err := json.Unmarshal(data, &intermediate)
	if err != nil {
		return err
	}

	subject := intermediate["subject"]
	schema := intermediate["schema"]

	id, err := strconv.ParseInt(intermediate["id"], 10, 64)
	if err != nil {
		return err
	}
	if subject == "" {
		return fmt.Errorf("missing field `subject`")
	}
	version, err := NewSemVer(intermediate["version"])
	if err != nil {
		return err
	}
	if schema == "" {
		return fmt.Errorf("missing field `schema`")
	}

	s.Id = id
	s.Subject = subject
	s.Version = *version
	s.Schema = schema
	return nil
}

type intermediate struct {
	Id      int64  `json:"id"`
	Subject string `json:"subject"`
	Version string `json:"version"`
	Schema  string `json:"schema"`
}

type SchemaId struct {
	Id int64 `json:"id"`
}
