package _go

import (
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/cyberagent/typebook/client/go/model"
)

type configClient struct {
	*baseClient
}

// PUT /config/(subject string) BODY config
func (cc *configClient) SetConfig(subject string, config model.Config) (int64, *model.Error) {
	response, body, errs := cc.baseClient.Put(fmt.Sprintf("/config/%s", subject)).SendStruct(config).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	affectedRows, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return affectedRows, nil
}

// PUT /config/(subject string)/properties/(property string) BODY value
func (cc *configClient) SetProperty(subject, name, value string) (int64, *model.Error) {
	response, body, errs := cc.baseClient.Put(fmt.Sprintf("/config/%s/properties/%s", subject, name)).SendString(value).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	affectedRows, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return affectedRows, nil
}

// GET /config/(subject string)
func (cc *configClient) GetConfig(subject string) (*model.Config, *model.Error) {
	response, body, errs := cc.baseClient.Get(fmt.Sprintf("/config/%s", subject)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	config := new(model.Config)
	if err := json.Unmarshal(body, config); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return config, nil
}

// GET /config/(subject string)/properties/(property string)
func (cc *configClient) GetProperty(subject, property string) (string, *model.Error) {
	response, body, errs := cc.baseClient.Get(fmt.Sprintf("/config/%s/properties/%s", subject, property)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return "", err
	}
	return string(body), nil
}

// DELETE /config/(subject string)
func (cc *configClient) DeleteConfig(subject string) (int64, *model.Error) {
	response, body, errs := cc.baseClient.Delete(fmt.Sprintf("/config/%s", subject)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	deletedRows, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return deletedRows, nil
}

// DELETE /config/(subject string)/properties/(property string)
func (cc *configClient) DeleteProperty(subject, property string) (int64, *model.Error) {
	response, body, errs := cc.baseClient.Delete(fmt.Sprintf("/config/%s/properties/%s", subject, property)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	deletedRows, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return deletedRows, nil
}
