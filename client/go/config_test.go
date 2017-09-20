package _go

import (
	"testing"

	"gopkg.in/h2non/gock.v1"

	"github.com/cyberagent/typebook/client/go/model"
)

var config = model.Config{Compatibility: "BACKWARD"}

func TestSetConfig(t *testing.T) {
	defer gock.Off()

	expect := 1
	gock.New(host).
		Put("/config/" + subject).
		JSON(config).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	if actual, err := client.SetConfig(subject, config); err != nil {
		t.Errorf(`SetConfig("%s", "%v") should not be an error. But an error was occurred: %v`, subject, config, err)
	} else if actual != int64(expect) {
		t.Errorf(`SetConfig("%s", "%v") = %v, wants %v`, subject, config, actual, expect)
	}
}

func TestSetProperty(t *testing.T) {
	defer gock.Off()

	expect := 1
	gock.New(host).
		Put("/config/" + subject + "/properties/compatibility").
		BodyString("FORWARD").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	if actual, err := client.SetProperty(subject, "compatibility", "FORWARD"); err != nil {
		t.Errorf(`SetProperty("%s", "compatibility", "FORWARD") should not be an error. But an error was occurred: %v`, subject, err)
	} else if actual != int64(expect) {
		t.Errorf(`SetProperty("%s", "compatibility", "FORWARD") = %v, wants %v`, subject, actual, expect)
	}
}

func TestGetConfig(t *testing.T) {
	defer gock.Off()

	expect := config
	gock.New(host).
		Get("/config/" + subject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "28",
		}).
		JSON(config)

	if actual, err := client.GetConfig(subject); err != nil {
		t.Errorf(`GetConfig("%s") should not be an error. But an error was occurred: %v"`, subject, err)
	} else if *actual != expect {
		t.Errorf(`GetConfig("%s") = %v, wants %v`, subject, actual, expect)
	}
}

func TestGetProperty(t *testing.T) {
	defer gock.Off()

	expect := config.Compatibility
	gock.New(host).
		Get("/config/" + subject + "/properties/compatibility").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "8",
		}).
		BodyString("BACKWARD")

	if actual, err := client.GetProperty(subject, "compatibility"); err != nil {
		t.Errorf(`GetProperty("%s", "compatibility") should not be an error. But an error was occurred: %v`, subject, err)
	} else if actual != expect {
		t.Errorf(`GetProperty("%s", "compatibility") = %s, wants %s`, subject, actual, expect)
	}
}

func TestDeleteConfig(t *testing.T) {
	defer gock.Off()

	expect := 1
	gock.New(host).
		Delete("/config/" + subject).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	if actual, err := client.DeleteConfig(subject); err != nil {
		t.Errorf(`DeleteConfig("%s") should not be an error. But an error was occurred: %v`, subject, err)
	} else if actual != int64(expect) {
		t.Errorf(`DeleteConfig("%s") = %d, wants %s`, subject, actual, expect)
	}
}

func TestDeleteProperty(t *testing.T) {
	defer gock.Off()

	expect := 1
	gock.New(host).
		Delete("/config/" + subject + "/properties/compatibility").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "text/plain",
			"Content-Length": "1",
		}).
		BodyString("1")

	if actual, err := client.DeleteProperty(subject, "compatibility"); err != nil {
		t.Errorf(`DeleteProperty("%s", "compatibility") should not be an error. But an error was occurred: %v`, subject, err)
	} else if actual != int64(expect) {
		t.Errorf(`DeleteProperty("%s", "compatibility") = %d, wants %s`, subject, actual, expect)
	}
}
