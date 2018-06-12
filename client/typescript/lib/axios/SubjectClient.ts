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
import { SubjectApi as SubjectClientInterface } from '../api/SubjectApi';
import * as model from '../model/Model';
import validateObject, { validateNumber, validateStringArray } from '../model/validation/validate';
import ResponseHandler, { identity } from './AxiosResponseHandler';

export default class SubjectClient implements SubjectClientInterface {

    private client: AxiosInstance;

    createSubject(name: string, description?: string): Promise<number> {
        return ResponseHandler.handle(
            this.client.post<number>(`/subjects/${name}`, description),
            identity,
            validateNumber
        );
    }

    getSubject(name: string): Promise<model.Subject> {
        return ResponseHandler.handle(
            this.client.get<model.Subject>(`/subjects/${name}`),
            (s) => new model.Subject(s.name, s.description),
            validateObject
        );
    }

    listSubjects(): Promise<Array<string>> {
        return ResponseHandler.handle(
            this.client.get<Array<string>>('/subjects'),
            identity,
            validateStringArray
        );
    }

    updateDescription(name: string, description?: string): Promise<number> {
        return ResponseHandler.handle(
            this.client.put<number>(`/subjects/${name}`, description),
            identity,
            validateNumber
        );
    }

    deleteSubject(name: string): Promise<number> {
        return ResponseHandler.handle(
            this.client.delete(`/subjects/${name}`),
            identity,
            validateNumber
        );
    }
}
