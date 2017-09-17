package _go

import (
	"fmt"
	"reflect"
	"testing"

	"github.com/parnurzeal/gorequest"
	"gopkg.in/h2non/gock.v1"

	"github.com/cyberagent/typebook/client/go/model"
)

const (
	host        = "foo.bar"
	subject     = "test-subject"
	description = "This is test"
)

var (
	client *Client = NewClient(host, nil)
)

func init() {
	gock.DisableNetworking()
	gorequest.DisableTransportSwap = true
}

// POST /subjects/(subject name) BODY description
func TestCreateSubject(t *testing.T) {
	defer gock.Off()

	expect := 0
	gock.New(host).
		Post("/subjects/" + subject).
		Reply(201).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("0")

	if actual, err := client.CreateSubject(subject, description); err != nil {
		t.Errorf(`CreateSubject("%s", "%s") should not be an error. But an error was occurred: %v`, subject, description, err)
	} else if actual != int64(expect) {
		t.Errorf(`CreateSubject("%s", "%s") = %v, wants %v"`, subject, description, actual, expect)
	}
}

// GET /subjects/(subject string)
func TestGetSubject(t *testing.T) {
	defer gock.Off()

	expect := model.Subject{Name: subject, Description: description}
	gock.New(host).
		Get("/subjects/" + subject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "52",
		}).
		JSON(map[string]string{"name": subject, "description": description})

	if actual, err := client.GetSubject("test-subject"); err != nil {
		t.Errorf(`GetSubject("%s"") should not be an error. But an error was occurred: %v`, subject, err)
	} else if *actual != expect {
		t.Errorf(`GetSubject("%s") = %v, wants %v"`, subject, *actual, expect)
	}

	errExpect := model.Error{ServerError: &model.ServerError{ErrorCode: 404, Message: "Subject Not Found"}, ClientError: nil}
	gock.New(host).
		Get("/subjects/non-existent").
		Reply(404).
		SetHeaders(map[string]string{
			"Content-Length": "59",
			"Content-Type":   "application/json",
		}).
		JSON(errExpect.ServerError)

	if _, errActual := client.GetSubject("non-existent"); *errActual.ServerError != *errExpect.ServerError || !reflect.DeepEqual(errActual.ClientError,  errExpect.ClientError) {
		t.Errorf(`GetSubject("non-existent") = %v, watns %v`, *errActual, errExpect)
	}
}

// GET /subjects
func TestListSubjects(t *testing.T) {
	defer gock.Off()

	expect := []string{"another-subject", "test-subject"}
	gock.New(host).
		Get("/subjects").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "34",
		}).
		JSON(expect)

	if actual, err := client.ListSubjects(); err != nil {
		t.Errorf(`ListSubjects() should not be an error. But an error was occurred: %v`, err)
	} else if !reflect.DeepEqual(actual, expect) {
		t.Errorf(`ListSubject() = %v, wants %v"`, actual, expect)
	}
}

// PUT /subjects/(subject string) BODY description
func TestUpdateDescription(t *testing.T) {
	defer gock.Off()

	expect := 1
	gock.New(host).
		Put("/subjects/" + subject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString(fmt.Sprintf("%d", expect))

	if actual, err := client.UpdateDescription(subject, "Updated description"); err != nil {
		t.Errorf(`UpdateDescription("%s", "Updated description") should not be an error. But an error was occurred: %v)`, subject, err)
	} else if actual != int64(expect) {
		t.Errorf(`UpdateDescription("%s", "Updated description") = %v, wants %v`, subject, actual, expect)
	}
}

// DELETE /subjects/(subject string)
func TestDeleteSubject(t *testing.T) {
	defer gock.Off()

	expect := 1
	gock.New(host).
		Delete("/subjects/" + subject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString(fmt.Sprintf("%d", expect))

	if actual, err := client.DeleteSubject(subject); err != nil {
		t.Errorf(`DeleteSubject("%s") should not be an error. But an error was occurred: %v)`, subject, err)
	} else if actual != int64(expect) {
		t.Errorf(`DeleteSubject("%s") = %v, wants %v`, subject, actual, expect)
	}
}
