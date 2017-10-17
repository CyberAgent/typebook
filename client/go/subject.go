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

type subjectClient struct {
	*baseClient
}

// CreateSubject issues a POST /subjects/(subject string) request with description in its body to a typebook server.
// It will create a new subject with given name and description.
// This method returns 0 for success otherwise returns non-nil model.Error.
func (sc *subjectClient) CreateSubject(name, description string) (int64, *model.Error) {
	request := sc.baseClient.Post(fmt.Sprintf("/subjects/%s", name)).Type("text")
	if description != "" {
		request.Send(description)
	}
	response, body, errs := request.EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	id, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return id, nil
}

// GetSubject issues a GET /subjects/(subject string) request to a typebook server.
// It will retrieve a subject which has the given name.
// If it fails, non-nil model.Error is returned.
func (sc *subjectClient) GetSubject(name string) (*model.Subject, *model.Error) {
	response, body, errs := sc.baseClient.Get(fmt.Sprintf("/subjects/%s", name)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	subject := new(model.Subject)
	if err := json.Unmarshal(body, subject); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return subject, nil
}

// ListSubjects issues a GET /subjects request to a typebook server.
// It will retrieve a list of names of existing subjects.
// If it fails, non-nil model.Error is returned.
func (sc *subjectClient) ListSubjects() ([]string, *model.Error) {
	response, body, errs := sc.baseClient.Get("/subjects").EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return nil, err
	}

	names := make([]string, 0)
	if err := json.Unmarshal(body, &names); err != nil {
		return nil, model.NewError(nil, []error{err})
	}
	return names, nil
}

// UpdateDescription issues a PUT /subjects/(subject string) request with new description in its body to a typebook server.
// It will update a description of a subject which has the given name.
// This method returns the number of updated rows in the backend database so normally it is 1 for success.
// Otherwise non-nil model.Error is returned.
func (sc *subjectClient) UpdateDescription(name, description string) (int64, *model.Error) {
	request := sc.baseClient.Put(fmt.Sprintf("/subjects/%s", name)).Type("text")
	if description != "" {
		request.Send(description)
	}
	response, body, errs := request.EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	updatedRows, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return updatedRows, nil
}

// DeleteSubject issues a DELETE /subjects/(subject string) to a typebook server.
// It will delete a subject which has the given name.
// This method returns the number of deleted rows in the backend database so normally it is 1 for success.
// Otherwise non-nil model.Error is returned
func (sc *subjectClient) DeleteSubject(name string) (int64, *model.Error) {
	response, body, errs := sc.baseClient.Delete(fmt.Sprintf("/subjects/%s", name)).EndBytes()
	if err := checkError(response, body, errs); err != nil {
		return -1, err
	}

	deletedRows, err := strconv.ParseInt(string(body), 10, 64)
	if err != nil {
		return -1, model.NewError(nil, []error{err})
	}
	return deletedRows, nil
}
