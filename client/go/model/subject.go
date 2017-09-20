package model

type Subject struct {
	Name string `json:"name"`
	Description string `json:"description,omitempty"`
}
