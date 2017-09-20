package model

type ServerError struct {
	ErrorCode int    `json:"error_code"`
	Message   string `json:"message"`
}

type ClientError = []error

type Error struct {
	*ServerError
	ClientError
}

func NewError(serverError *ServerError, clientError []error) *Error {
	return &Error {
		ServerError: serverError,
		ClientError: clientError,
	}
}
