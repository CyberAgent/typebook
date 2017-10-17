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

	"github.com/parnurzeal/gorequest"

	"github.com/cyberagent/typebook/client/go/model"
)

type baseClient struct {
	host string
	*gorequest.SuperAgent
}

// Get constructs a HTTP GET request as a `gorequest.SuperAgent`.
func (bc *baseClient) Get(path string) *gorequest.SuperAgent {
	return bc.SuperAgent.Get(fmt.Sprintf("http://%s%s", bc.host, path))
}

// Post constructs a HTTP POST request as a `gorequest.SuperAgent`.
func (bc *baseClient) Post(path string) *gorequest.SuperAgent {
	return bc.SuperAgent.Post(fmt.Sprintf("http://%s%s", bc.host, path))
}

// Put constructs a HTTP PUT request as a `gorequest.SuperAgent`.
func (bc *baseClient) Put(path string) *gorequest.SuperAgent {
	return bc.SuperAgent.Put(fmt.Sprintf("http://%s%s", bc.host, path))
}

// Delete constructs a HTTP DELETE request as a `gorequest.SuperAgent`
func (bc *baseClient) Delete(path string) *gorequest.SuperAgent {
	return bc.SuperAgent.Delete(fmt.Sprintf("http://%s%s", bc.host, path))
}

// checkError checks the response of `gorequest.EndBytes()` and returns *model.Error
// if either client or server have caused errors.
// Iff no error is occurred to both client and server, this will return nil.
func checkError(response gorequest.Response, body []byte, errs []error) *model.Error {
	if len(errs) != 0 {
		return model.NewError(nil, errs)
	}
	if err := checkServerError(response, body); err != nil {
		return model.NewError(err, nil)
	}
	return nil
}

// checkServerError checks the status code of response from typebook server.
// if it is 200 or 201, it will return nil, otherwise model.ServerError is created from the response body.
func checkServerError(response gorequest.Response, body []byte) *model.ServerError {
	switch response.StatusCode {
	case 200, 201:
		return nil
	default:
		serverErr := new(model.ServerError)
		err := json.Unmarshal(body, serverErr)
		if err != nil {
			panic(err)
		}
		return serverErr
	}
}
