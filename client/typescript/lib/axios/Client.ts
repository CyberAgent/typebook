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
import { ClientInterface } from '../ClientInterface';
import SubjectClient from './SubjectClient';
import ConfigClient from './ConfigClient';
import SchemaClient from './SchemaClient';
import * as model from '../model/Model';
import mixin from '../mixin';

export default class Client implements ClientInterface {

    private client: AxiosInstance;

    constructor(client: AxiosInstance) {
        this.client = client;
    }

    createSubject(name: string, description?: string): Promise<number> {
        return new Promise<number>(() => 0);
    }

    getSubject(name: string): Promise<model.Subject> {
        return new Promise<model.Subject>( () => new model.Subject('') );
    }

    listSubjects(): Promise<Array<string>> {
        return this.client.get<Array<string>>('/subjects').then((r) => r.data);
    }

    updateDescription(name: string, description?: string): Promise<number> {
        return new Promise<number>(() => 0);
    }

    deleteSubject(name: string): Promise<number> {
        return new Promise<number>(() => 0);
    }

    setConfig(subject: string, config: model.RegistryConfig): Promise<number> {
        return new Promise<number>(() => 0);
    }

    setProperty(subject: string, property: string, value: string): Promise<number> {
        return new Promise<number>(() => 0);
    }

    getConfig(subject: string): Promise<model.RegistryConfig> {
        return new Promise<model.RegistryConfig>(() => new model.RegistryConfig('FULL'));
    }

    getProperty(subject: string, property: string): Promise<string> {
        return new Promise<string>(() => '');
    }

    deleteConfig(subject: string): Promise<number> {
        return new Promise<number>( () => 0);
    }

    deleteProperty(subject: string, property: string): Promise<number> {
        return new Promise<number>( () => 0);
    }

    registerSchema(subject: string, definition: string): Promise<model.SchemaId> {
        return new Promise<model.SchemaId>( () => new model.SchemaId(0) );
    }

    lookupSchema(subject: string, definition: string): Promise<model.Schema> {
        return new Promise<model.Schema>( () => new model.Schema(0, '', new model.SemanticVersion(1, 0, 0), ''));
    }

    lookupAllSchemas(subject: string, definition: string): Promise<Array<model.Schema>> {
        return new Promise<Array<model.Schema>>( () => [] );
    }

    getSchemaById(id: number): Promise<model.Schema> {
        return new Promise<model.Schema>( () => new model.Schema(0, '', new model.SemanticVersion(1, 0, 0), ''));
    }

    getLatestSchema(subject: string): Promise<model.Schema> {
        return new Promise<model.Schema>( () => new model.Schema(0, '', new model.SemanticVersion(1, 0, 0), ''));
    }

    getSchemaByMajorVersion(subject: string, major: number): Promise<model.Schema> {
        return new Promise<model.Schema>( () => new model.Schema(0, '', new model.SemanticVersion(1, 0, 0), ''));
    }

    getSchemaByVersion(subject: string, version: model.SemanticVersion): Promise<model.Schema> {
        return new Promise<model.Schema>( () => new model.Schema(0, '', new model.SemanticVersion(1, 0, 0), ''));
    }

    listVersions(subject: string): Promise<Array<model.SemanticVersion>> {
        return new Promise<Array<model.SemanticVersion>>( () => [] );
    }

    checkCompatibilityWithLatest(subject: string, definition: string): Promise<model.Compatibility> {
        return new Promise<model.Compatibility>( () => new model.Compatibility(false) );
    }

    checkCompatibilityWithMajorVersion(subject: string, major: number, definition: string): Promise<model.Compatibility> {
        return new Promise<model.Compatibility>( () => new model.Compatibility(false) );
    }

    checkCompatibilityWithVersion(subject: string, version: model.SemanticVersion, definition: string): Promise<model.Compatibility> {
        return new Promise<model.Compatibility>( () => new model.Compatibility(false) );
    }

}

mixin(Client, [SubjectClient, SchemaClient, SchemaClient, ConfigClient]);
