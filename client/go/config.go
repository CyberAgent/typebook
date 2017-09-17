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
	"strconv"

	"github.com/cyberagent/typebook/client/go/model"
)

type configClient struct {
	*baseClient
}

// SetConfig issue a PUT /config/(subject string) request with config json in its body to a typebook server.
// It will create or update a whole config of the subject with the given name.
// This method returns the number of updated rows in the backend database otherwise non-nil model.Error is returned on failure.
func (cc *configClient) SetConfig(subject string, config model.Config) (int64, *model.Error) {
	response, body, errs := cc.baseClient.
		Put(fmt.Sprintf("/config/%s", subject)).
		Type("json").
		Send(config).
		EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	affectedRows, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return affectedRows, nil
}

// SetConfig issues a PUT /config/(subject string)/properties/(property string) request with its value in its body to a typebook server.
// It will create or update a specific property of config of a subject which has the given name.
// This method returns the number of updated rows so normally it is 1 for success, otherwise non-nil model.Error is returned.
func (cc *configClient) SetProperty(subject, name, value string) (int64, *model.Error) {
	response, body, errs := cc.baseClient.
		Put(fmt.Sprintf("/config/%s/properties/%s", subject, name)).
		Type("text").
		Send(value).
		EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	affectedRows, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return affectedRows, nil
}

// GetConfig issues a GET /config/(subject string) request to a typebook server.
// It will retrieve a whole config belongs to a subject with the given name.
// If it fails, non-nil model.Error is returned.
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

// GetProperty issues a GET /config/(subject string)/properties/(property string) request to a typebook server.
// It will retrieve a specific property of config belongs to a subject with the given name.
// If it fails, non-nil model.Error is returned.
func (cc *configClient) GetProperty(subject, property string) (string, *model.Error) {
	response, body, errs := cc.baseClient.Get(fmt.Sprintf("/config/%s/properties/%s", subject, property)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return "", err
	}
	return string(body), nil
}

// DeleteConfig issues a DELETE /config/(subject string) request to a typebook server.
// It will delete a whole config belongs to a subject with the given name.
// This method returns the number of deleted rows in the backend database or returns non-nil model.Error on failure.
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

// DeleteProperty issues a DELETE /config/(subject string)/properties/(property string) request to a typebook server.
// It will delete a specific property of config belongs to a subject with the given name.
// This method returns the number of deleted rows in the backend database so normally it is 1.
// Otherwise non-nil model.Error is returned.
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
