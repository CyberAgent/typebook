import { test, TestContext } from 'ava';
import axios from 'axios';
import * as nock from 'nock';
import { Client } from '../../index';
import * as model from '../../lib/model/Model';

const baseURL = 'http://foo.bar:8888';
const client = Client.create(axios.create({baseURL}));

const testSubject = 'test-subject';
const testConfig = new model.RegistryConfig('FULL');

const textPlain = { 'Content-Type': 'text/plain' };
const applicationJson = { 'Content-Type': 'application/json' };

test('setConfig should issue a request PUT /config/(subject: string)', async(t: TestContext) => {
   t.plan(1);

   const expected = 1;

   nock(baseURL)
       .replyContentLength()
       .put(`/config/${testSubject}`, testConfig)
       .reply(200, expected, textPlain);

   const actual = await client.setConfig(testSubject, testConfig);
   t.is(actual, expected);
});

test('setConfig should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .put(`/config/${testSubject}`, testConfig)
        .reply(200, 'foo', textPlain);

    await t.throws(client.setConfig(testSubject, testConfig));
});

test('setProperty should issue a request PUT /config/(subject: string)/properties/compatibility', async(t: TestContext) => {
    t.plan(1);

    const property = 'compatibility';
    const value = 'FULL';
    const expected = 1;

    nock(baseURL)
        .replyContentLength()
        .put(`/config/${testSubject}/properties/${property}`, value)
        .reply(200, expected, textPlain);

    const actual = await client.setProperty(testSubject, property, value);
    t.is(actual, expected);
});

test('setProperty should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    const property = 'compatibility';
    const value = 'FULL';

    nock(baseURL)
        .replyContentLength()
        .put(`/config/${testSubject}/properties/${property}`, value)
        .reply(200, 'foo', textPlain);

    await t.throws(client.setProperty(testSubject, property, value));
});

test('getConfig should issue a request GET /config/(subject: string)', async(t: TestContext) => {
   t.plan(1);

   const expected = new model.RegistryConfig('FULL');

   nock(baseURL)
       .replyContentLength()
       .get(`/config/${testSubject}`)
       .reply(200, expected, applicationJson);

   const actual = await client.getConfig(testSubject);
   t.deepEqual(actual, expected);
});

test('getConfig should fail against an invalid response', async(t: TestContext) => {
   t.plan(1);

   nock(baseURL)
       .replyContentLength()
       .get(`/config/${testSubject}`)
       .reply(200, {foo: 'bar'}, applicationJson);

   await t.throws(client.getConfig(testSubject));
});

test('getProperty should issue a request GET /config/(subject: string)/properties/(property: string)', async(t: TestContext) => {
    t.plan(1);

    const property = 'compatibility';
    const expected = 'FULL';
    nock(baseURL)
        .replyContentLength()
        .get(`/config/${testSubject}/properties/${property}`)
        .reply(200, 'FULL', textPlain);

    const actual = await client.getProperty(testSubject, property);
    t.is(actual, expected);
});

test('getProperty should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    const property = 'compatibility';

    nock(baseURL)
        .replyContentLength()
        .get(`/config/${testSubject}/properties/${property}`)
        .reply(200, 1, textPlain);

    await t.throws(client.getProperty(testSubject, property));
});

test('deleteConfig should issue a request DELETE /config/(subject: string)', async(t: TestContext) => {
    t.plan(1);

    const expected = 1;
    nock(baseURL)
        .replyContentLength()
        .delete(`/config/${testSubject}`)
        .reply(200, expected, textPlain);

    const actual = await client.deleteConfig(testSubject);
    t.is(actual, expected);
});

test('deleteConfig should fail against an invalid response', async(t: TestContext) => {
   t.plan(1);

   nock(baseURL)
       .replyContentLength()
       .delete(`/config/${testSubject}`)
       .reply(200, 'foo', textPlain);

   await t.throws(client.deleteConfig(testSubject));
});

test('deleteProperty should issue a request DELETE /config/(subject: string)/properties/(property: string)', async(t: TestContext) => {
    t.plan(1);

    const property = 'compatibility';
    const expected = 1;
    nock(baseURL)
        .replyContentLength()
        .delete(`/config/${testSubject}/properties/${property}`)
        .reply(200, expected, textPlain);

    const actual = await client.deleteProperty(testSubject, property);
    t.is(actual, expected);
});

test('deleteProperty should fail against an invalid request', async(t: TestContext) => {
    t.plan(1);

    const property = 'compatibility';
    nock(baseURL)
        .replyContentLength()
        .delete(`/config/${testSubject}/properties/${property}`)
        .reply(200, undefined, textPlain);

    await t.throws(client.deleteProperty(testSubject, property));
});
