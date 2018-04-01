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

import { IsInt } from 'class-validator';

const semanticVersionRegExp: RegExp = /^v((0|[1-9][0-9]*)\.){2}(0|[1-9][0-9]*)$/;
const majorVersionRegExp: RegExp = /^v(0|[1-9][0-9]*)$/;

export function isSemanticVersion(version: string): boolean {
    return semanticVersionRegExp.test(version);
}

export function isMajorVersion(version: string): boolean {
    return majorVersionRegExp.test(version);
}

export class SemanticVersion {

    public static fromString(ver: string): SemanticVersion {
        if (!isSemanticVersion(ver)) {
            throw new EvalError(`invalid format as semantic version: ${ver}`);
        }

        const verNums = ver.substring(1).split('.');
        const major = parseInt(verNums[0], 10);
        const minor = parseInt(verNums[1], 10);
        const patch = parseInt(verNums[2], 10);

        return new SemanticVersion(major, minor, patch);
    }

    @IsInt()
    readonly major: number;

    @IsInt()
    readonly minor: number;

    @IsInt()
    readonly patch: number;

    constructor(
        major: number,
        minor: number,
        patch: number
    ) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public toString(): string {
        return `v${this.major}.${this.minor}.${this.patch}`;
    }
}
