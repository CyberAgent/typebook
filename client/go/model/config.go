package model

type Property struct {
	Subject  string `json:"subject"`
	Property string `json:"property"`
	Value    string `json:"value"`
}

type Config struct {
	Compatibility string `json:"compatibility"`
}
