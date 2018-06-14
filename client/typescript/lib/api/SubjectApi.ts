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

import * as model from '../model/Model';

export interface SubjectApi {

    /**
     * Create a new subject under which schemas are evolved.
     * @param {string} name a name of subject.
     * @param {string} description a description of subject.
     * @returns {Promise<number>} the number of created subject
     */
    createSubject(name: string, description?: string): Promise<number>;

    /**
     * Get a subject by name.
     * @param {string} name a name of subject
     * @returns {Promise<Subject>} subject name and description.
     */
    getSubject(name: string): Promise<model.Subject>;

    /**
     * Get a list of all subjects registered with this TypeBook.
     * @returns {Promise<Array<string>>} a list of subject names registered with this TypeBook.
     */
    listSubjects(): Promise<Array<string>>;

    /**
     * Update a description of specified subject.
     * @param {string} name a name of subject
     * @param {string} description new description for the subject
     * @returns {Promise<number>} the number of updated descriptions = 1
     */
    updateDescription(name: string, description?: string): Promise<number>;

    /**
     * Delete a specified subject.
     * @param {string} name a name of subject to be deleted
     * @returns {Promise<number>} the number of deleted subject.
     */
    deleteSubject(name: string): Promise<number>;
}
