import { AxiosInstance } from 'axios';
import { ClientInterface } from './lib/ClientInterface';

export default class Client implements ClientInterface {

    static create(innerClient: AxiosInstance): Client;

    createSubject(name: string, description?: string): Promise<number>;
    getSubject(name: string): Promise<Subject>;
    listSubjects(): Promise<Array<string>>;
    updateDescription(name: string, description?: string): Promise<number>;
    deleteSubject(name: string): Promise<number>;

    setConfig(subject: string, config: RegistryConfig): Promise<number>;
    setProperty(subject: string, property: string, value: string): Promise<number>;
    getConfig(subject: string): Promise<RegistryConfig>;
    getProperty(subject: string, property: string): Promise<string>;
    deleteConfig(subject: string): Promise<number>;
    deleteProperty(subject: string, property: string): Promise<number>;

    registerSchema(subject: string, definition: string): Promise<SchemaId>;
    lookupSchema(subject: string, definition: string): Promise<Schema>;
    lookupAllSchemas(subject: string, definition: string): Promise<Array<Schema>>;
    getSchemaById(id: number): Promise<Schema>;
    getLatestSchema(subject: string): Promise<Schema>;
    getSchemaByMajorVersion(subject: string, major: number): Promise<Schema>;
    getSchemaByVersion(subject: string, version: SemanticVersion): Promise<Schema>;
    listVersions(subject: string): Promise<Array<SemanticVersion>>;
    checkCompatibilityWithLatest(subject: string, definition: string): Promise<Compatibility>;
    checkCompatibilityWithMajorVersion(subject: string, major: number, definition: string): Promise<Compatibility>;
    checkCompatibilityWithVersion(subject: string, version: SemanticVersion, definition: string): Promise<Compatibility>;
}

export declare class Subject {
    readonly name: string;
    readonly description?: string;
    constructor(name: string, description?: string);
}

export declare class Compatibility {
    readonly isCompatible: boolean;
    constructor(isCompatible: boolean);
}

export declare type CompatibilityType = 'FULL' | 'BACKWARD' | 'FORWARD' | 'NONE';

export declare type Property = 'compatibility';

export declare class Config {
    readonly subject: string;
    readonly property: string;
    readonly value: string;
    constructor(subject: string, property: Property, value: string);
}

export declare class RegistryConfig {
    readonly compatibility: CompatibilityType;
    constructor(compatibility: CompatibilityType);
}

export declare class Schema {
    readonly id: number;
    readonly subject: string;
    readonly version: SemanticVersion;
    readonly schema: string;
    constructor(id: number, subject: string, version: SemanticVersion, schema: string);
}

export declare class SchemaId {
    readonly id: number;
    constructor(id: number);
}

export declare class SemanticVersion {
    static fromString(ver: string): SemanticVersion;
    readonly major: number;
    readonly minor: number;
    readonly patch: number;
    constructor(major: number, minor: number, patch: number);
    toString(): string;
}

export declare class ErrorResponse extends Error {
    readonly errorCode: number;
    readonly message: string;
    constructor(errorCode: number, message: string);
}
