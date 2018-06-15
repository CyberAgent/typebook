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

export interface SchemaApi {

    /**
     * Register new schema under a specified subject.
     * @param {string} subject a name of subject to register a schema with
     * @param {string} definition a definition of schema
     * @returns {Promise<SchemaId>} Unique ID of registered schema
     */
    registerSchema(subject: string, definition: string): Promise<model.SchemaId>;

    /**
     * Look up a metadata of schema such as version and ID by subject name and its definition.
     * Unlike lookupAllSchemas, this method return the first one that matches the condition.
     * @param {string} subject a name of subject to look in.
     * @param {string} definition a definition of schema to look up
     * @returns {Promise<Schema>} a metadata of schema consisting of ID, version, name and definition.
     */
    lookupSchema(subject: string, definition: string): Promise<model.Schema>;

    /**
     * Lookup metadata of all schemas by subject name and its definition.
     * @param {string} subject a name of subject
     * @param {string} definition a definition of schema to look up
     * @returns {Promise<Array<Schema>>} metadata of schemas consisting of ID, version, name and definition.
     */
    lookupAllSchemas(subject: string, definition: string): Promise<Array<model.Schema>>;

    /**
     * Get a schema definition by ID.
     * @param {number} id ID of schema
     * @returns {Promise<Schema>} a definition of schema that has specified ID
     */
    getSchemaById(id: number): Promise<model.Schema>;

    /**
     * Get the latest version of schema under the specified subject.
     * @param {string} subject a name of subject
     * @returns {Promise<Schema>} a definition of latest schema
     */
    getLatestSchema(subject: string): Promise<model.Schema>;

    /**
     * Get the latest version of schema within the specified subject and major version.
     * @param {string} subject a name of subject
     * @param {number} major a major version of schema to retrieve
     * @returns {Promise<Schema>} a definition of the latest schema within the specified major version
     */
    getSchemaByMajorVersion(subject: string, major: number): Promise<model.Schema>;

    /**
     * Get a schema which exactly conforms to the specified semantic version.
     * @param {string} subject a name of subject
     * @param {SemanticVersion} version a version of schema to retrieve
     * @returns {Promise<Schema>} a definition of schema that conforms to specified semantic version.
     */
    getSchemaByVersion(subject: string, version: model.SemanticVersion): Promise<model.Schema>;

    /**
     * Get a full list of existing versions of schemas registered with a specified subject.
     * @param {string} subject a name of subject
     * @returns {Promise<Array<SemanticVersion>>} a list of existing versions
     */
    listVersions(subject: string): Promise<Array<model.SemanticVersion>>;

    /**
     * Check the compatibility of posted schema with the latest schema
     * @param {string} subject a name of subject
     * @param {string} definition a definition of schema that you would like to check the compatibility with registered one.
     * @returns {Promise<Compatibility>} whether two schemas are compatible or not
     */
    checkCompatibilityWithLatest(subject: string, definition: string): Promise<model.Compatibility>;

    /**
     * Check the compatibility of posted schema with the latest schema within the specified subject and major version.
     * @param {string} subject a name of subject
     * @param {number} major a major version
     * @param {string} definition a definition of schema that you would like to check the compatibility with registered one.
     * @returns {Promise<Compatibility>} whether two schemas are compatible or not
     */
    checkCompatibilityWithMajorVersion(subject: string, major: number, definition: string): Promise<model.Compatibility>;

    /**
     * Check the compatibility of posted schema with the specific version of schema.
     * @param {string} subject a name of subject
     * @param {SemanticVersion} version a semantic version
     * @param {string} definition a definition of schema that you would like to check the compatibility with registered one.
     * @returns {Promise<Compatibility>} whether two schemas are compatible or not
     */
    checkCompatibilityWithVersion(subject: string, version: model.SemanticVersion, definition: string): Promise<model.Compatibility>;
}
