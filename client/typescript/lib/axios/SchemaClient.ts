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

import { AxiosInstance } from 'axios';
import { SchemaApi as SchemaClientInterface } from '../api/SchemaApi';
import * as model from '../model/Model';
import validateObject, { validateArray } from '../model/validation/validate';
import ResponseHandler from './AxiosResponseHandler';

export default class SchemaClient implements SchemaClientInterface {

    private client: AxiosInstance;

    registerSchema(subject: string, definition: string): Promise<model.SchemaId> {
        return ResponseHandler.handle(
            this.client.post(`/subjects/${subject}/versions`, definition),
            (id) => new model.SchemaId(id.id),
            validateObject
        );
    }

    lookupSchema(subject: string, definition: string): Promise<model.Schema> {
        return ResponseHandler.handle(
            this.client.post<model.Schema>(`/subjects/${subject}/schema/lookup`, definition),
            (s) => new model.Schema(
                s.id,
                s.subject,
                model.SemanticVersion.fromString(s.version.toString()),
                s.schema
            ),
            validateObject
        );
    }

    lookupAllSchemas(subject: string, definition: string): Promise<Array<model.Schema>> {
        return ResponseHandler.handle(
            this.client.post<Array<model.Schema>>(`/subjects/${subject}/schema/lookupAll`, definition),
            (arr) => arr.map((s) => new model.Schema(
                s.id,
                s.subject,
                model.SemanticVersion.fromString(s.version.toString()),
                s.schema)
            ),
            validateArray
        );
    }

    getSchemaById(id: number): Promise<model.Schema> {
        return ResponseHandler.handle(
            this.client.get<model.Schema>(`/schemas/ids/${id}`),
            (s) => new model.Schema(s.id, s.subject, model.SemanticVersion.fromString(s.version.toString()), s.schema),
            validateObject
        );
    }

    getLatestSchema(subject: string): Promise<model.Schema> {
        return ResponseHandler.handle(
            this.client.get<model.Schema>(`/subjects/${subject}/versions/latest`),
            (s) => new model.Schema(s.id, s.subject, model.SemanticVersion.fromString(s.version.toString()), s.schema),
            validateObject
        );
    }

    getSchemaByMajorVersion(subject: string, major: number): Promise<model.Schema> {
        return ResponseHandler.handle(
            this.client.get<model.Schema>(`/subjects/${subject}/versions/v${major}`),
            (s) => new model.Schema(s.id, s.subject, model.SemanticVersion.fromString(s.version.toString()), s.schema),
            validateObject
        );
    }

    getSchemaByVersion(subject: string, version: model.SemanticVersion): Promise<model.Schema> {
        return ResponseHandler.handle(
            this.client.get<model.Schema>(`/subjects/${subject}/versions/${version.toString()}`),
            (s) => new model.Schema(s.id, s.subject, model.SemanticVersion.fromString(s.version.toString()), s.schema),
            validateObject
        );
    }

    listVersions(subject: string): Promise<Array<model.SemanticVersion>> {
        return ResponseHandler.handle(
            this.client.get<Array<string>>(`/subjects/${subject}/versions`),
            (arr) => arr.map((v) => model.SemanticVersion.fromString(v)),
            validateArray
        );
    }

    checkCompatibilityWithLatest(subject: string, definition: string): Promise<model.Compatibility> {
        return ResponseHandler.handle(
            this.client.post<model.Compatibility>(`/compatibility/subjects/${subject}/versions/latest`, definition),
            (c) => new model.Compatibility(c.isCompatible),
            validateObject
        );
    }

    checkCompatibilityWithMajorVersion(subject: string, major: number, definition: string): Promise<model.Compatibility> {
        return ResponseHandler.handle(
            this.client.post<model.Compatibility>(`/compatibility/subjects/${subject}/versions/v${major}`, definition),
            (c) => new model.Compatibility(c.isCompatible),
            validateObject
        );
    }

    checkCompatibilityWithVersion(subject: string, version: model.SemanticVersion, definition: string): Promise<model.Compatibility> {
        return ResponseHandler.handle(
            this.client.post<model.Compatibility>(`/compatibility/subjects/${subject}/versions/${version.toString()}`, definition),
            (c) => new model.Compatibility(c.isCompatible),
            validateObject
        );
    }

}
