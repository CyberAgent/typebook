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

func (sc *subjectClient) CreateSubject(name, description string) (int64, *model.Error) {
	request := sc.baseClient.Post(fmt.Sprintf("/subjects/%s", name))
	if description != "" {
		request.SendString(description)
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

func (sc *subjectClient) UpdateDescription(name, description string) (int64, *model.Error) {
	request := sc.baseClient.Put(fmt.Sprintf("/subjects/%s", name))
	if description != "" {
		request.SendString(description)
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
