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

package model

import "strings"

type ServerError struct {
	ErrorCode int    `json:"error_code"`
	Message   string `json:"message"`
}

func (se *ServerError) Error() string {
	if se == nil {
		return "<nil>"
	}
	return se.Message
}

type ClientError = []error

type Error struct {
	*ServerError
	ClientError
}

func (err *Error) Messages() []string {
	msgs := make([]string, 0)
	if err.ServerError != nil {
		msgs = append(msgs, err.ServerError.Error())
	}
	for _, e := range err.ClientError {
		msgs = append(msgs, e.Error())
	}
	return msgs
}

func (err *Error) Error() string {
	if err == nil {
		return "<nil>"
	}
	return strings.Join(err.Messages(), "\n")
}

func NewError(serverError *ServerError, clientError []error) *Error {
	return &Error{
		ServerError: serverError,
		ClientError: clientError,
	}
}
