package _go

import (
	"encoding/json"
	"fmt"

	"github.com/cyberagent/typebook/client/go/model"
)

type schemaClient struct {
	*baseClient
}

func (sc *schemaClient) RegisterSchema(subject, definition string) (*model.SchemaId, *model.Error) {
	response, body, errs := sc.baseClient.Post(fmt.Sprintf("/subjects/%s/versions", subject)).SendString(definition).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	id := new(model.SchemaId)
	if err := json.Unmarshal(body, id); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return id, nil
}

func (sc *schemaClient) LookupSchema(subject, definition string) (*model.Schema, *model.Error) {
	response, body, errs := sc.baseClient.Post(fmt.Sprintf("/subjects/%s/schema/lookup", subject)).SendString(definition).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	schema := new(model.Schema)
	if err := json.Unmarshal(body, schema); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return schema, nil
}

func (sc *schemaClient) LookupAllSchemas(subject, definition string) ([]model.Schema, *model.Error) {
	response, body, errs := sc.baseClient.Post(fmt.Sprintf("/subjects/%s/schema/lookupAll", subject)).SendString(definition).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	schemas := make([]model.Schema, 0)
	if err := json.Unmarshal(body, &schemas); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return schemas, nil
}

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

func (sc *schemaClient) GetLatestSchema(subject string) (*model.Schema, *model.Error) {
	return sc.getSchemaByVersionString(subject, "latest")
}

func (sc *schemaClient) GetSchemaByMajorVersion(subject string, majorVersion int) (*model.Schema, *model.Error) {
	return sc.getSchemaByVersionString(subject, fmt.Sprintf("v%d", majorVersion))
}

func (sc *schemaClient) GetSchemaBySemVer(subject string, semver model.SemVer) (*model.Schema, *model.Error) {
	return sc.getSchemaByVersionString(subject, semver.String())
}

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

func (sc *schemaClient) checkCompatibilityWithVersion(subject, version, definition string) (*model.Compatibility, *model.Error) {
	response, body, errs := sc.baseClient.
		Post(fmt.Sprintf("/compatibility/subjects/%s/versions/%s", subject, version)).
		SendString(definition).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	compatibility := new(model.Compatibility)
	if err := json.Unmarshal(body, compatibility); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return compatibility, nil
}

func (sc *schemaClient) CheckCompatibilityWithLatest(subject, definition string) (*model.Compatibility, *model.Error) {
	return sc.checkCompatibilityWithVersion(subject, "latest", definition)
}

func (sc *schemaClient) CheckCompatibilityWithMajorVersion(subject string, majorVersion int, definition string) (*model.Compatibility, *model.Error) {
	return sc.checkCompatibilityWithVersion(subject, fmt.Sprintf("v%d", majorVersion), definition)
}

func (sc *schemaClient) CheckCompatibilityWithSemVer(subject string, semver model.SemVer, definition string) (*model.Compatibility, *model.Error) {
	return sc.checkCompatibilityWithVersion(subject, semver.String(), definition)
}
