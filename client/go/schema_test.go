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
	"reflect"
	"strconv"
	"testing"

	"gopkg.in/h2non/gock.v1"

	"github.com/cyberagent/typebook/client/go/model"
)

const schemaDef = `
{
    "namespace": "com.example",
    "type": "record",
    "name": "Person",
    "fields": [
        {
            "name": "id",
            "type": "int"
        },
        {
            "name": "first_name",
            "type": "string"
        }
    ]
}
`

// POST /subjects/(subject string)/versions
func TestRegisterSchema(t *testing.T) {
	defer gock.Off()

	expect := model.SchemaId{Id: 1}
	gock.New(host).
		Post("/subjects/" + subject + "/versions").
		JSON(schemaDef).
		Reply(201).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "8",
		}).
		JSON(expect)

	if actual, err := client.RegisterSchema(subject, schemaDef); err != nil {
		t.Errorf(`RegisterSchema("%s", "%s") should not be an error. But an error was occurred: %v`, subject, schemaDef, err)
	} else if *actual != expect {
		t.Errorf(`RegisterSchema("%s", "%s") = %v, wants %v`, subject, schemaDef, actual, expect)
	}
}

func TestLookupSchema(t *testing.T) {
	defer gock.Off()

	expect := model.Schema{
		Id:         1,
		Subject:    subject,
		Version:    model.SemVer{Major: 1, Minor: 0, Patch: 0},
		Definition: schemaDef,
	}
	gock.New(host).
		Post("/subjects/" + subject + "/schema/lookup").
		JSON(schemaDef).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(expect)

	if actual, err := client.LookupSchema(subject, schemaDef); err != nil {
		t.Errorf(`LookupSchema("%s", "%s") should not be an error. But an error was occurred: %v`, subject, schemaDef, err)
	} else if *actual != expect {
		t.Errorf(`LookupSchema("%s", "%s") = %v, wants %v`, subject, schemaDef, actual, expect)
	}
}

func TestLookupAllSchemas(t *testing.T) {
	defer gock.Off()

	expect := []model.Schema{
		{
			Id:         1,
			Subject:    subject,
			Version:    model.SemVer{Major: 1, Minor: 0, Patch: 0},
			Definition: schemaDef,
		},
		{
			Id:         3,
			Subject:    subject,
			Version:    model.SemVer{Major: 1, Minor: 0, Patch: 2},
			Definition: schemaDef,
		},
	}

	gock.New(host).
		Post("/subjects/" + subject + "/schema/lookupAll").
		JSON(schemaDef).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "461",
		}).JSON(expect)

	if actual, err := client.LookupAllSchemas(subject, schemaDef); err != nil {
		t.Errorf(`LookupAllSchemas("%s", "%s") should not be an error. But an error was occurred: %v`, subject, schemaDef, err)
	} else if !reflect.DeepEqual(actual, expect) {
		t.Errorf(`LookupAllSchemas("%s", "%s") = %v, wants %v`, subject, schemaDef, actual, expect)
	}
}

func TestGetSchemaById(t *testing.T) {
	defer gock.Off()

	expect := model.Schema{
		Id:         1,
		Subject:    subject,
		Version:    model.SemVer{Major: 1, Minor: 0, Patch: 0},
		Definition: schemaDef,
	}

	gock.New(host).
		Get("/schemas/ids/" + strconv.FormatInt(expect.Id, 10)).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(expect)

	if actual, err := client.GetSchemaById(expect.Id); err != nil {
		t.Errorf(`GetSchemaById(%d) should not be an error. But an error was occurred: %v"`, expect.Id, err)
	} else if *actual != expect {
		t.Errorf(`GetSchemaById(%d) = %v, wants %v`, expect.Id, *actual, expect)
	}
}

func TestGetLatestSchema(t *testing.T) {
	defer gock.Off()

	expect := model.Schema{
		Id:         1,
		Subject:    subject,
		Version:    model.SemVer{Major: 1, Minor: 0, Patch: 0},
		Definition: schemaDef,
	}

	gock.New(host).
		Get("/subjects/" + subject + "/versions/latest").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(expect)

	if actual, err := client.GetLatestSchema(subject); err != nil {
		t.Errorf(`GetLatestSchema("%s") should not be an error. But an error was occurred: %v`, subject, err)
	} else if *actual != expect {
		t.Errorf(`GetLatestSchema(%s) = %v, wants %v`, subject, *actual, expect)
	}
}

func TestGetSchemaByMajorVersion(t *testing.T) {
	defer gock.Off()

	expect := model.Schema{
		Id:         1,
		Subject:    subject,
		Version:    model.SemVer{Major: 1, Minor: 0, Patch: 0},
		Definition: schemaDef,
	}

	gock.New(host).
		Get("/subjects/" + subject + "/versions/v1").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(expect)

	if actual, err := client.GetSchemaByMajorVersion(subject, 1); err != nil {
		t.Errorf(`GetSchemaByMajorVersion("%s", 1) should not be an error. But an error was occurred: %v`, subject, err)
	} else if *actual != expect {
		t.Errorf(`GetSchemaByMajorVersion("%s", 1) = %v, wants %v`, subject, *actual, expect)
	}
}

func TestGetSchemaBySemVer(t *testing.T) {
	defer gock.Off()

	expect := model.Schema{
		Id:         1,
		Subject:    subject,
		Version:    model.SemVer{Major: 1, Minor: 0, Patch: 0},
		Definition: schemaDef,
	}

	gock.New(host).
		Get("/subjects/" + subject + "/versions/" + expect.Version.String()).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "229",
		}).JSON(expect)

	if actual, err := client.GetSchemaBySemVer(subject, expect.Version); err != nil {
		t.Errorf(`GetSchemaBySemVer("%s", %s) should not be an error. But an error was occurred: %v`, subject, expect.Version.String(), err)
	} else if *actual != expect {
		t.Errorf(`GetSchemaBySemVer("%s", %s) = %v, wants %v`, subject, expect.Version.String(), *actual, expect)
	}
}

func TestListVersions(t *testing.T) {
	defer gock.Off()

	expect := []model.SemVer{
		{Major: 1, Minor: 0, Patch: 0},
		{Major: 1, Minor: 0, Patch: 1},
		{Major: 1, Minor: 0, Patch: 2},
	}

	gock.New(host).
		Get("/subjects/" + subject + "/versions").
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "28",
		}).JSON(`["v1.0.0", "v1.0.1", "v1.0.2"]`)

	if actual, err := client.ListVersions(subject); err != nil {
		t.Errorf(`ListVersions("%s") should not be an error. But an error was occurred: %v`, subject, err)
	} else if !reflect.DeepEqual(actual, expect) {
		t.Errorf(`ListVersions("%s") = %v, wants %v`, subject, actual, expect)
	}
}

func TestSchemaCheckCompatibilityWithLatest(t *testing.T) {
	defer gock.Off()

	expect := model.Compatibility{IsCompatible: true}

	gock.New(host).
		Post("/compatibility/subjects/" + subject + "/versions/latest").
		BodyString(schemaDef).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "135",
		}).JSON(expect)

	if actual, err := client.CheckCompatibilityWithLatest(subject, schemaDef); err != nil {
		t.Errorf(`CheckCompatibilityWithLatest("%s", "%s) should not be an error, but an error was occurred: %v"`, subject, schemaDef, err)
	} else if *actual != expect {
		t.Errorf(`CheckCompatibilityWithLatest("%s", "%s") = %v, wants %v`, subject, schemaDef, *actual, expect)
	}
}

func TestCheckCompatibilityWithMajorVersion(t *testing.T) {
	defer gock.Off()

	expect := model.Compatibility{IsCompatible: false}

	gock.New(host).
		Post("/compatibility/subjects/" + subject + "/versions/v1").
		BodyString(schemaDef).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "23",
		}).JSON(expect)

	if actual, err := client.CheckCompatibilityWithMajorVersion(subject, 1, schemaDef); err != nil {
		t.Errorf(`CheckCompatibilityWithMajorVersion("%s", 1, "%s") should not be an error, but an error was occurred: %v`, subject, schemaDef, err)
	} else if *actual != expect {
		t.Errorf(`CheckCompatibilityWithMajorVersion("%s", 1, "%s") = %v, wants %v`, subject, schemaDef, *actual, expect)
	}
}

func TestCheckCompatibilityWithSemVer(t *testing.T) {
	defer gock.Off()

	expect := model.Compatibility{IsCompatible: true}
	ver := model.SemVer{Major: 1, Minor: 0, Patch: 0}

	gock.New(host).
		Post("/compatibility/subjects/" + subject + "/versions/" + ver.String()).
		BodyString(schemaDef).
		Reply(200).
		SetHeaders(map[string]string{
			"Content-Type":   "application/json",
			"Content-Length": "135",
		}).JSON(expect)

	if actual, err := client.CheckCompatibilityWithSemVer(subject, model.SemVer{Major: 1, Minor: 0, Patch: 0}, schemaDef); err != nil {
		t.Errorf(`CheckCompatibilityWithSemVer("%s", %v, "%s") should not be an error, but an error was occurred: %v`, subject, ver, schemaDef, err)
	} else if *actual != expect {
		t.Errorf(`CheckCompatibilityWithSemVer("%s", %v, "%s") = %v, wants %v`, subject, ver, schemaDef, *actual, expect)
	}
}
