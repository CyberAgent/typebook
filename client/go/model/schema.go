// The MIT License (MIT)
//
// Copyright © 2017 CyberAgent, Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package model

import (
	"bytes"
	"encoding/json"
	"fmt"
	"strconv"
)

type Schema struct {
	Id         int64  `json:"id"`
	Subject    string `json:"subject"`
	Version    SemVer `json:"version"`
	Definition string `json:"schema"`
}

func decode(data []byte, binder func(map[string]interface{}) error) error {
	decoder := json.NewDecoder(bytes.NewBuffer(data))
	decoder.UseNumber()

	jsonMap := make(map[string]interface{})
	if err := decoder.Decode(&jsonMap); err != nil {
		return err
	}
	return binder(jsonMap)
}

func decodeId(jsonMap map[string]interface{}) (int64, error) {
	idNum, ok := jsonMap["id"].(json.Number)
	if !ok {
		return 0, fmt.Errorf("the value of field `id` is not an integer, but %v", jsonMap["id"])
	}
	id, err := strconv.ParseInt(string(idNum), 10, 64)
	if err != nil {
		return 0, err
	}
	return id, nil
}

func (s Schema) MarshalJSON() ([]byte, error) {
	return json.Marshal(map[string]interface{}{
		"id":      s.Id,
		"subject": s.Subject,
		"version": s.Version.String(),
		"schema":  s.Definition,
	})
}

func (s *Schema) UnmarshalJSON(data []byte) error {

	return decode(data, func(jsonMap map[string]interface{}) error {
		id, err := decodeId(jsonMap)
		if err != nil {
			return err
		}
		subject, ok := jsonMap["subject"].(string)
		if !ok || subject == "" {
			return fmt.Errorf("the value of field `subject` is not a string, but %v", jsonMap["subject"])
		}
		if subject == "" {
			return fmt.Errorf("missing field `subject`")
		}
		schema, ok := jsonMap["schema"].(string)
		if !ok {
			return fmt.Errorf("the value of field `schema` is not a string, but %v", jsonMap["schema"])
		}
		if schema == "" {
			return fmt.Errorf("missing field `schema`")
		}
		versionStr, ok := jsonMap["version"].(string)
		if !ok {
			return fmt.Errorf("the value of field `version` is not a string, but %v", jsonMap["version"])
		}
		version, err := NewSemVer(versionStr)
		if err != nil {
			return err
		}

		s.Id = id
		s.Subject = subject
		s.Version = *version
		s.Definition = schema
		return nil
	})
}

type SchemaId struct {
	Id int64 `json:"id"`
}

func (sid *SchemaId) UnmarshalJSON(data []byte) error {
	return decode(data, func(jsonMap map[string]interface{}) error {
		id, err := decodeId(jsonMap)
		if err != nil {
			return err
		}
		sid.Id = id
		return nil
	})
}
