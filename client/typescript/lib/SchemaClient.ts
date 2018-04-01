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

import * as model from './model/Model';

export interface SchemaClient {

    registerSchema(subject: string, definition: string): Promise<model.SchemaId>;

    lookupSchema(subject: string, definition: string): Promise<model.Schema>;

    lookupAllSchemas(subject: string, definition: string): Promise<Array<model.Schema>>;

    getSchemaById(id: number): Promise<model.Schema>;

    getLatestSchema(subject: string): Promise<model.Schema>;

    getSchemaByMajorVersion(subject: string, major: number): Promise<model.Schema>;

    getSchemaByVersion(subject: string, version: model.SemanticVersion): Promise<model.Schema>;

    listVersions(subject: string): Promise<Array<model.SemanticVersion>>;

    checkCompatibilityWithLatest(subject: string, definition: string): Promise<model.Compatibility>;

    checkCompatibilityWithMajorVersion(subject: string, major: number, definition: string): Promise<model.Compatibility>;

    checkCompatibilityWithVersion(subject: string, version: model.SemanticVersion, definition: string): Promise<model.Compatibility>;
}
