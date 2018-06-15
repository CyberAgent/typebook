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

export interface ConfigApi {

    /**
     * Set an entire RegistryConfig that includes schema revolution policy to a specified subject.
     * @param {string} subject name of subject to set a config
     * @param {RegistryConfig} config
     * @returns {Promise<number>}
     */
    setConfig(subject: string, config: model.RegistryConfig): Promise<number>;

    /**
     * Set a specific property of RegistryConfig to a specified subject.
     * @param {string} subject a name of subject to set a property.
     * @param {Property} property a name of property.
     * @param {string} value new value for property.
     * @returns {Promise<number>} the number of updated properties
     */

    setProperty(subject: string, property: model.Property, value: string): Promise<number>;

    /**
     * Get a Registry config set to a specified subject.
     * @param {string} subject a name of subject
     * @returns {Promise<RegistryConfig>} RegistryConfig of a subject
     */
    getConfig(subject: string): Promise<model.RegistryConfig>;

    /**
     * Get a specific property of RegistryConfig of a specified subject.
     * @param {string} subject a name of subject
     * @param {Property} property a name of property
     * @returns {Promise<string>}
     */
    getProperty(subject: string, property: model.Property): Promise<string>;

    /**
     * Delete a entire RegistryConfig from a specified subject.
     * @param {string} subject a name of subject
     * @returns {Promise<number>} the number of deleted properties
     */
    deleteConfig(subject: string): Promise<number>;

    /**
     * Delete a specific property of RegistryConfig from a specified subject.
     * @param {string} subject a name of subject
     * @param {Property} property a name of property
     * @returns {Promise<number>} the number of deleted property
     */
    deleteProperty(subject: string, property: model.Property): Promise<number>;

}
