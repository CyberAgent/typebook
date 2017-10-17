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
