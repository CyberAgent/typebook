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

package _go

import (
	"encoding/json"
	"fmt"

	"github.com/cyberagent/typebook/client/go/model"
)

type schemaClient struct {
	*baseClient
}

// RegisterSchema issues a POST /subjects/(subject string)/versions request with a schema definition in its body to a typebook server.
// It will register a new schema under the given subject according to the given definition.
// This method returns model.SchemaId that represents id for the created schema on success, otherwise non-nil model.Error is returned.
func (sc *schemaClient) RegisterSchema(subject, definition string) (*model.SchemaId, *model.Error) {
	response, body, errs := sc.baseClient.
		Post(fmt.Sprintf("/subjects/%s/versions", subject)).
		Type("json").
		Send(definition).
		EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	id := new(model.SchemaId)
	if err := json.Unmarshal(body, id); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return id, nil
}

// LookupSchema issues a POST /subjects/(subject string)/schema/lookup request with a schema definition to lookup in its body to a typebook server.
// It will lookup a schema by its definition within the given subject.
// If multiple schemas are found, the latest one is chosen.
// This method returns model.Schema whose definition conforms to the given one if found otherwise it returns non-nil model.Error.
func (sc *schemaClient) LookupSchema(subject, definition string) (*model.Schema, *model.Error) {
	response, body, errs := sc.baseClient.
		Post(fmt.Sprintf("/subjects/%s/schema/lookup", subject)).
		Type("json").
		Send(definition).
		EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	schema := new(model.Schema)
	if err := json.Unmarshal(body, schema); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return schema, nil
}

// LookupAllSchemas issues a POST /subjects/(subject string)/schema/lookupAll request with a schema definition to lookup in its body to a typebook server.
// It will lookup all schemas whose definition conforms to the given one within the given subject.
// If multiple schemas are found, all schemas are returned.
// This method returns non-nil model.Error on failure.
func (sc *schemaClient) LookupAllSchemas(subject, definition string) ([]model.Schema, *model.Error) {
	response, body, errs := sc.baseClient.
		Post(fmt.Sprintf("/subjects/%s/schema/lookupAll", subject)).
		Type("json").
		Send(definition).
		EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	schemas := make([]model.Schema, 0)
	if err := json.Unmarshal(body, &schemas); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return schemas, nil
}

// GetSchemaById issue a GET /schemas/ids/(id int64) request to a typebook server.
// It will retrieve the schema that matches the given id.
// This method returns model.Schema on success, otherwise non-nil model.Error is returned.
func (sc *schemaClient) GetSchemaById(id int64) (*model.Schema, *model.Error) {
	response, body, errs := sc.baseClient.Get(fmt.Sprintf("/schemas/ids/%d", id)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	schema := new(model.Schema)
	if err := json.Unmarshal(body, schema); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return schema, nil
}

func (sc *schemaClient) getSchemaByVersionString(subject, version string) (*model.Schema, *model.Error) {
	response, body, errs := sc.baseClient.Get(fmt.Sprintf("/subjects/%s/versions/%s", subject, version)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	schema := new(model.Schema)
	if err := json.Unmarshal(body, schema); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return schema, nil
}

// GetLatestSchema issues a GET /subjects/(subject string)/versions/latest request to a typebook server.
// It will retrieve the latest schema under the given subject.
// This method returns model.Schema on success otherwise non-nil model.Error is returned.
func (sc *schemaClient) GetLatestSchema(subject string) (*model.Schema, *model.Error) {
	return sc.getSchemaByVersionString(subject, "latest")
}

// GetSchemaByMajorVersion issues a GET /subjects/(subject string)/versions/v(majorVersion int) request to a typebook server.
// It will retrieve a latest schema that has designated major version under the given subject.
// This method returns model.Schema on success otherwise non-nil model.Error is returned.
func (sc *schemaClient) GetSchemaByMajorVersion(subject string, majorVersion int) (*model.Schema, *model.Error) {
	return sc.getSchemaByVersionString(subject, fmt.Sprintf("v%d", majorVersion))
}

// GetSchemaBySemVer issues a GET /subjects/(subject string)/versions/(semver string) request to a typebook server.
// It will retrieve a schema that has designated semver under the given subject.
// This method returns model.Schema on success otherwise non-nil model.Error is returned.
func (sc *schemaClient) GetSchemaBySemVer(subject string, semver model.SemVer) (*model.Schema, *model.Error) {
	return sc.getSchemaByVersionString(subject, semver.String())
}

// ListVersions issues a GET /subjects/(subject string)/versions request to a typebook server.
// It retrieves all existing versions under the given subject.
// This method returns a list of model.SemVer on success, otherwise non-nil model.Error is returned.
func (sc *schemaClient) ListVersions(subject string) ([]model.SemVer, *model.Error) {
	response, body, errs := sc.baseClient.Get(fmt.Sprintf("/subjects/%s/versions", subject)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	versions := make([]string, 0)
	if err := json.Unmarshal(body, &versions); err != nil {
		return nil, model.NewError(nil, []error{err})
	}

	semvers := make([]model.SemVer, 0)
	for _, ver := range versions {
		semver, err := model.NewSemVer(ver)
		if err != nil {
			return nil, model.NewError(nil, []error{err})
		}
		semvers = append(semvers, *semver)
	}
	return semvers, nil
}

func (sc *schemaClient) checkCompatibilityWithVersion(subject, version, definition string) (*model.Compatibility, *model.Error) {
	response, body, errs := sc.baseClient.
		Post(fmt.Sprintf("/compatibility/subjects/%s/versions/%s", subject, version)).
		Type("json").
		Send(definition).
		EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	compatibility := new(model.Compatibility)
	if err := json.Unmarshal(body, compatibility); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return compatibility, nil
}

// CheckCompatibilityWithLatest issues POST /compatibility/subjects/(subject string)/versions/latest with a schema definition in its body to a typebook server.
// It will check if the posted schema is compatible with the latest one under the given subject.
// This method returns model.Compatibility on success otherwise non-nil model.Error is returned.
func (sc *schemaClient) CheckCompatibilityWithLatest(subject, definition string) (*model.Compatibility, *model.Error) {
	return sc.checkCompatibilityWithVersion(subject, "latest", definition)
}

// CheckCompatibilityWithMajorVersion issues POST /compatibility/subjects/(subject string)/versions/v(majorVersion int) with a schema definition in its body to a typebook server.
// It will check if the posted schema is compatible with the latest one that has the designated major version under the given subject.
// This method returns model.Compatibility on success otherwise non-nil model.Error is returned.
func (sc *schemaClient) CheckCompatibilityWithMajorVersion(subject string, majorVersion int, definition string) (*model.Compatibility, *model.Error) {
	return sc.checkCompatibilityWithVersion(subject, fmt.Sprintf("v%d", majorVersion), definition)
}

// CheckCompatibilityWithSemVer issues POST /compatibility/subjects/(subject string)/versions/(semver string) with a schema definition in its body to a typebook server.
// It will check if the posted schema is compatible with the one that has the designated semver under the given subject.
// This method returns model.Compatibility on success otherwise non-nil model.Error is returned.
func (sc *schemaClient) CheckCompatibilityWithSemVer(subject string, semver model.SemVer, definition string) (*model.Compatibility, *model.Error) {
	return sc.checkCompatibilityWithVersion(subject, semver.String(), definition)
}
