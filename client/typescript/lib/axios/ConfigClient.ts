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
import { ConfigApi as ConfigClientInterface } from '../api/ConfigApi';
import * as model from '../model/Model';
import validateObject, { validateNumber, validateString } from '../model/validation/validate';
import ResponseHandler, { identity } from './AxiosResponseHandler';

export default class ConfigClient implements ConfigClientInterface {

    private client: AxiosInstance;

    setConfig(subject: string, config: model.RegistryConfig): Promise<number> {
        return ResponseHandler.handle(
            this.client.put<number>(`/config/${subject}`, config),
            identity,
            validateNumber
        );
    }

    setProperty(subject: string, property: model.Property, value: string): Promise<number> {
        return ResponseHandler.handle(
            this.client.put<number>(`/config/${subject}/properties/${property}`, value),
            identity,
            validateNumber
        );
    }

    getConfig(subject: string): Promise<model.RegistryConfig> {
        return ResponseHandler.handle(
            this.client.get<model.RegistryConfig>(`/config/${subject}`),
            (c) => new model.RegistryConfig(c.compatibility),
            validateObject
        );
    }

    getProperty(subject: string, property: model.Property): Promise<string> {
        return ResponseHandler.handle(
            this.client.get<string>(`/config/${subject}/properties/${property}`),
            identity,
            validateString
        );
    }

    deleteConfig(subject: string): Promise<number> {
        return ResponseHandler.handle(
            this.client.delete(`/config/${subject}`),
            identity,
            validateNumber
        );
    }

    deleteProperty(subject: string, property: model.Property): Promise<number> {
        return ResponseHandler.handle(
            this.client.delete(`/config/${subject}/properties/${property}`),
            identity,
            validateNumber
        );
    }
}
