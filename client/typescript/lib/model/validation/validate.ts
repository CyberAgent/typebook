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

import { validate as cvalidate, ValidationError as ClassValidationError, Validator } from 'class-validator';

export type ValidationErrors = Array<ClassValidationError | Error>;
export type ValidationResult = Promise<ValidationErrors>;
const ValidationResult = Promise as {
    new (executor: (
        resolve: (value?: ValidationErrors | PromiseLike<ValidationErrors>) => void,
        reject: (reason?: any) => void) => void
    ): ValidationResult;
};

const validator: Validator = new Validator();

export default function validate(object: Object): ValidationResult {
    return cvalidate(object, {
        skipMissingProperties: false,
        forbidUnknownValues: true
    });
}

export function validateNumber(num: number): ValidationResult {
    return new ValidationResult( (resolve) => {
        if (validator.isInt(num)) {
            resolve([]);
        } else {
            resolve([new TypeError(`invalid type of response: ${num}`)]);
        }
    });
}

export function validateString(str: Object): ValidationResult {
    return new ValidationResult((resolve) => {
        if (validator.isString(str) && validator.isNotEmpty(str)) {
            resolve([]);
        } else {
            resolve([new TypeError(`invalid type of response: ${str}`)]);
        }
    });
}

export function validateStringArray(strArr: Array<string>): ValidationResult {
    return new ValidationResult((resolve) => {
        if (
            validator.isArray(strArr) &&
            strArr.every( (element) => validator.isString(element) && validator.isNotEmpty(element) )
        ) {
            resolve([]);
        } else  {
            resolve([new TypeError(`invalid type of response: ${strArr}`)]);
        }
    });
}

export function validateArray<T>(arr: Array<T>): ValidationResult {
    return new ValidationResult( (resolve) => {
        if (!validator.isArray(arr)) {
            resolve([new TypeError(`invalid type of response ${arr}`)]);
        } else {
            const validation: Promise<Array<ValidationErrors>> = Promise.all(arr.map( (element) => validate(element)));
            validation.then((errs: Array<ValidationErrors>) => {
                let arr: Array<ClassValidationError | Error> = [];
                errs.forEach((el) => arr = arr.concat(el));
                resolve(arr as ValidationErrors);
            }).catch((err) => resolve([err]));
        }
    });
}
