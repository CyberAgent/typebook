package model

import (
	"fmt"
	"regexp"
	"strconv"
	"strings"
)

var SemVerRegExp = regexp.MustCompile("^v((0|[1-9][0-9]*)\\.){2}(0|[1-9][0-9]*)$")

type SemVer struct {
	Major int
	Minor int
	Patch int
}

func (sv *SemVer) String() string {
	return fmt.Sprintf("v%d.%d.%d", sv.Major, sv.Minor, sv.Patch)
}

func IsSemVer(version string) bool {
	return SemVerRegExp.MatchString(version)
}

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
