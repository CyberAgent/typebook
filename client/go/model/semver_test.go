package model

import (
	"testing"
)

func TestNewSemVer(t *testing.T) {
	normalCases := []struct {
		version string
		expect  SemVer
	}{
		{
			version: "v1.2.3",
			expect:  SemVer{Major: 1, Minor: 2, Patch: 3},
		},
	}
	for _, testCase := range normalCases {
		actual, err := NewSemVer(testCase.version)
		if err != nil {
			t.Errorf("NewSemVer(%s) causes an error: %v", testCase.version, err)
		}
		if *actual != testCase.expect {
			t.Errorf("NewSemVer(%s) = %v, wants %v", testCase.version, *actual, testCase.expect)
		}
	}

	abnormalCase := []string{
		"v1",
		"2.3.1",
		"v1.2.3.4",
		"v01.2.3",
	}
	for _, testCase := range abnormalCase {
		if _, err := NewSemVer(testCase); err == nil {
			t.Errorf("NewSemVer(%s) should have caused an error. But no error occurred.", testCase)
		}
	}
}
