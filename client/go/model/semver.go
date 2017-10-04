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
	"fmt"
	"regexp"
	"strconv"
	"strings"
)

var SemVerRegExp = regexp.MustCompile("^v((0|[1-9][0-9]*)\\.){2}(0|[1-9][0-9]*)$")
var MajorVerRegExp = regexp.MustCompile("^v(0|[1-9][0-9]*)$")

// SemVer is the version that represents compatibility between schemas manged in typebook.
type SemVer struct {
	Major int
	Minor int
	Patch int
}

func (sv *SemVer) String() string {
	return fmt.Sprintf("v%d.%d.%d", sv.Major, sv.Minor, sv.Patch)
}

// IsSemVer checks if the given version string is valid form of semantic version and return true when it is valid.
func IsSemVer(version string) bool {
	return SemVerRegExp.MatchString(version)
}

func IsMajorVer(version string) bool {
	return MajorVerRegExp.MatchString(version)
}

// NewSemVer create a instance of SemVer from a given version string.
// If a given version string is not a valid format, this returns an error.
// Valid form has leading "v" and following 3 integers separated by "." (e.g. v1.0.0).
func NewSemVer(version string) (*SemVer, error) {
	if !IsSemVer(version) {
		return nil, fmt.Errorf("invalid format for semver: %s", version)
	}

	nums := strings.Split(version[1:], ".")
	major, _ := strconv.Atoi(nums[0])
	minor, _ := strconv.Atoi(nums[1])
	patch, _ := strconv.Atoi(nums[2])

	return &SemVer{
		Major: major,
		Minor: minor,
		Patch: patch,
	}, nil
}
