import { test, TestContext } from 'ava';
import axios from 'axios';
import * as nock from 'nock';
import { Client } from '../../index';
import * as model from '../../lib/model/Model';

const baseURL = 'http://foo.bar:8888';
const client = Client.create(axios.create({baseURL}));

const testSubject = 'test-subject';
const testSchemaDefinition = `
{
    "namespace": "com.example",
    "type": "record",
    "name": "Person",
    "fields": [
        {
            "name": "id",
            "type": "int"
        },
        {
            "name": "first_name",
            "type": "string"
        }
    ]
}`;

const testSchema = new model.Schema(
    1,
    testSubject,
    new model.SemanticVersion(1, 0, 0),
    testSchemaDefinition
);
const testSchemaResponse = {
    id: testSchema.id,
    subject: testSchema.subject,
    version: testSchema.version.toString(),
    schema: testSchema.schema
};

const applicationJson = { 'Content-Type': 'application/json' };

test('registerSchema should issue a request POST /subjects/(name: string)/versions', async(t: TestContext) => {
    t.plan(1);

    const expected = new model.SchemaId(1);

    nock(baseURL)
        .replyContentLength()
        .post(`/subjects/${testSubject}/versions`)
        .reply(201, expected, applicationJson);

    const actual = await client.registerSchema(testSubject, testSchemaDefinition);
    t.deepEqual(actual, expected);
});

test('registerSchema should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .post(`/subjects/${testSubject}/versions`)
        .reply(201, 1, applicationJson);

    await t.throws(client.registerSchema(testSubject, testSchemaDefinition));
});

test('lookupSchema should issue a request POST /subjects/(subject: string)/schema/lookup', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .post(`/subjects/${testSubject}/schema/lookup`, testSchemaDefinition)
        .reply(200, testSchemaResponse, applicationJson);

    const actual = await client.lookupSchema(testSubject, testSchemaDefinition);
    t.deepEqual(actual, testSchema);
});

test('lookupSchema should fail against an invalid request', async(t: TestContext) => {
   t.plan(1);

   nock(baseURL)
       .replyContentLength()
       .post(`/subjects/${testSubject}/schema/lookup`, testSchemaDefinition)
       .reply(200, 'foo');

   await t.throws(client.lookupSchema(testSubject, testSchemaDefinition));
});

test('lookupAllSchemas should issue a request POST /subjects(subject: string)/schema/lookupAll', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .post(`/subjects/${testSubject}/schema/lookupAll`, testSchemaDefinition)
        .reply(200, [
            {
                id: testSchema.id,
                subject: testSchema.subject,
                version: testSchema.version.toString(),
                schema: testSchema.schema
            },
            {
                id: 3,
                subject: testSchema.subject,
                version: 'v1.0.2',
                schema: testSchema.schema
            }
        ], applicationJson);

    const actual = await client.lookupAllSchemas(testSubject, testSchemaDefinition);
    const expected = [
        new model.Schema(1, testSchema.subject, new model.SemanticVersion(1, 0, 0), testSchemaDefinition),
        new model.Schema(3, testSchema.subject, new model.SemanticVersion(1, 0, 2), testSchemaDefinition)
    ];
    t.deepEqual(actual, expected);
});

test('lookupAllSchemas should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .post(`/subjects/${testSubject}/schema/lookupAll`, testSchemaDefinition)
        .reply(200, [
            {
                id: testSchema.id,
                subject: testSchema.subject,
                ver: testSchema.version.toString(),
                scheme: testSchema.schema
            },
            {
                id: 3,
                subject: testSchema.subject,
                version: 'v1.0.2',
                schema: testSchema.schema
            }
        ], applicationJson);

    await t.throws(client.lookupAllSchemas(testSubject, testSchemaDefinition));
});

test('getSchemaById should issue a request /schemas/ids/(id: long)', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get(`/schemas/ids/1`)
        .reply(200, testSchemaResponse, applicationJson);

    const actual = await client.getSchemaById(1);
    t.deepEqual(actual, testSchema);
});

test('getSchemaById should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get(`/schemas/ids/1`)
        .reply(200, 'foo', applicationJson);

    await t.throws(client.getSchemaById(1));
});

test('getLatestSchema should issue a request GET /subjects/(subject: string)/versions/latest', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get(`/subjects/${testSubject}/versions/latest`)
        .reply(200, testSchemaResponse, applicationJson);

    const actual = await client.getLatestSchema(testSubject);
    t.deepEqual(actual, testSchema);
});

test('getSchemaByMajorVersion should issue a request GET /subjects/(subject: string)/versions/v(version: int)', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get(`/subjects/${testSubject}/versions/v1`)
        .reply(200, testSchemaResponse, applicationJson);

    const actual = await client.getSchemaByMajorVersion(testSubject, 1);
    t.deepEqual(actual, testSchema);
});

test('getSchemaByVersion should issue a request GET /subjects/(subject: string)/versions/(version; string)', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get(`/subjects/${testSubject}/versions/v1.0.0`)
        .reply(200, testSchemaResponse, applicationJson);

    const actual = await client.getSchemaByVersion(testSubject, new model.SemanticVersion(1, 0, 0));
    t.deepEqual(actual, testSchema);
});

test('getSchemaByVersion should fail against an invalid request', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get(`/subjects/${testSubject}/versions/v1.0.0`)
        .reply(200, 'foo', applicationJson);

    await t.throws(client.getSchemaByVersion(testSubject, new model.SemanticVersion(1, 0, 0)));
});

test('listVersions should issue a request GET /subjects/(subject: string)/versions', async(t: TestContext) => {
   t.plan(1);

   nock(baseURL)
       .replyContentLength()
       .get(`/subjects/${testSubject}/versions`)
       .reply(200, ['v1.0.0', 'v1.0.1', 'v2.0.0'], applicationJson);

   const actual = await client.listVersions(testSubject);
   t.deepEqual(actual, [
       new model.SemanticVersion(1, 0, 0),
       new model.SemanticVersion(1, 0, 1),
       new model.SemanticVersion(2, 0, 0)
   ]);
});

test('listVersions should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get(`/subjects/${testSubject}/versions`)
        .reply(200, 'foo', applicationJson);

    await t.throws(client.listVersions(testSubject));
});

test(
    'checkCompatibilityWithLatest should issue a request POST /compatibility/subjects/(subject: string)/versions/latest',
    async(t: TestContext) => {
    t.plan(1);

    const expected = new model.Compatibility(true);
    nock(baseURL)
        .replyContentLength()
        .post(`/compatibility/subjects/${testSubject}/versions/latest`, testSchemaDefinition)
        .reply(200, expected, applicationJson);

    const actual = await client.checkCompatibilityWithLatest(testSubject, testSchemaDefinition);
    t.deepEqual(actual, expected);
});

test(
    'checkCompatibilityWithMajorVersion should issue a request POST /compatibility/subjects/(subject: string)/versions/v(version: int)',
    async(t: TestContext) => {

    t.plan(1);

    const expected = new model.Compatibility(true);
    nock(baseURL)
        .replyContentLength()
        .post(`/compatibility/subjects/${testSubject}/versions/v1`, testSchemaDefinition)
        .reply(200, expected, applicationJson);

    const actual = await client.checkCompatibilityWithMajorVersion(testSubject, 1, testSchemaDefinition);
    t.deepEqual(actual, expected);
});

test(
    'checkCompatibilityWithVersion should issue a request POST /compatibility/subjects/(subject: string)/versions/(version: string)',
    async(t: TestContext) => {
    t.plan(1);

    const expected = new model.Compatibility(false);
    nock(baseURL)
        .replyContentLength()
        .post(`/compatibility/subjects/${testSubject}/versions/v1.0.0`, testSchemaDefinition)
        .reply(200, expected, applicationJson);

    const actual = await client.checkCompatibilityWithVersion(testSubject, new model.SemanticVersion(1, 0, 0), testSchemaDefinition);
    t.deepEqual(actual, expected);
});

test('checkCompatibilityWithVersion should fail against an invalid request', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .post(`/compatibility/subjects/${testSubject}/versions/v1.0.0`, testSchemaDefinition)
        .reply(200, 'foo', applicationJson);

    await t.throws(client.checkCompatibilityWithVersion(testSubject, new model.SemanticVersion(1, 0, 0), testSchemaDefinition));
});
