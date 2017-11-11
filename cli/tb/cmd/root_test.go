package cmd

import (
	"os"

	"gopkg.in/h2non/gock.v1"

	typebook "github.com/cyberagent/typebook/client/go"
)

const (
	hostForTest     = "foo.bar"
	testSubject     = "test-subject"
	testDescription = "This is test"
)

func init() {
	os.Setenv("TYPEBOOK_URL", hostForTest)
	gock.DisableNetworking()
	typebook.DisableTransportSwap = true // to avoid overwriting gock's intercept transport with gorequest's superagent transport
}
