package _go

import (
	"errors"
	"net/http"
)

var defaultTransport *http.Transport

func init() {
	var ok bool
	defaultTransport, ok = http.DefaultTransport.(*http.Transport)
	if !ok {
		panic(errors.New("failed to cast DefaultTransport to *http.Transport"))
	}
}

type Client struct {
	*subjectClient
	*configClient
	*schemaClient
}

func NewClient(host string, transport *http.Transport) *Client {
	baseClient := &baseClient{host, transport}
	if transport == nil {
		baseClient.transport = defaultTransport
	}
	return &Client{
		&subjectClient{baseClient},
		&configClient{baseClient},
		&schemaClient{baseClient},
	}
}
