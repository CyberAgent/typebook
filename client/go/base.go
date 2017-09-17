package _go

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/parnurzeal/gorequest"

	"github.com/cyberagent/typebook/client/go/model"
)

type baseClient struct {
	host      string
	transport *http.Transport
}

func (bc *baseClient) Get(path string) *gorequest.SuperAgent {
	return createRequest(bc.transport).Get(fmt.Sprintf("http://%s%s", bc.host, path))
}

func (bc *baseClient) Post(path string) *gorequest.SuperAgent {
	return createRequest(bc.transport).Post(fmt.Sprintf("http://%s%s", bc.host, path))
}

func (bc *baseClient) Put(path string) *gorequest.SuperAgent {
	return createRequest(bc.transport).Put(fmt.Sprintf("http://%s%s", bc.host, path))
}

func (bc *baseClient) Delete(path string) *gorequest.SuperAgent {
	return createRequest(bc.transport).Delete(fmt.Sprintf("http://%s%s", bc.host, path))
}

func createRequest(transport *http.Transport) *gorequest.SuperAgent {
	request := gorequest.New()
	request.Transport = transport
	return request
}

func checkError(response gorequest.Response, body []byte, errs []error) *model.Error {
	if len(errs) != 0 {
		return model.NewError(nil, errs)
	}
	if err := checkServerError(response, body); err != nil {
		return model.NewError(err, nil)
	}
	return nil
}

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
