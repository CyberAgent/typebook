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

import { AxiosPromise } from 'axios';
import { Validator } from 'class-validator';
import { ErrorResponse } from '../model/ErrorResponse';
import { ValidationResult } from '../model/validation/validate';

const validator = new Validator();

export default class AxiosResponseHandler {

    static handle<T, O>(promise: AxiosPromise<T>, mapper: (T) => O, validate: (O) => ValidationResult): Promise<O> {
        return new Promise<O>((resolve, reject) => {
            promise
                .then((response) => {
                    if (validator.isDefined(response.data)) {
                        const data = mapper(response.data);
                        validate(data).then((errors) => {
                            if (errors.length === 0) {
                                resolve(data);
                            }
                            else {
                                reject(errors);
                            }
                        }).catch((error) => reject(error));
                    }
                    else {
                        reject(new Error('empty response'));
                    }
                })
                .catch((error) => {
                    if (!error.response) {
                        reject(error);
                    }
                    else {
                        const code = error.response.data.error_code;
                        const message = error.response.data.message;
                        reject(new ErrorResponse(code, message));
                    }
                });
        });
    }
}

export function identity<T>(x: T): T {
    return x;
}
