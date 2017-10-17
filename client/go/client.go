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
	"github.com/parnurzeal/gorequest"
)

type Client struct {
	*subjectClient
	*configClient
	*schemaClient
}

// NewClient create and instantiate a new Client object which can interact with
// typebook server at the designated endpoint.
// endpoint should be in the form of `host:port`.
// Client instances should create for each goroutine to send multiple requests concurrently.
func NewClient(endpoint string) *Client {
	baseClient := &baseClient{endpoint, gorequest.New()}
	return &Client{
		&subjectClient{baseClient},
		&configClient{baseClient},
		&schemaClient{baseClient},
	}
}