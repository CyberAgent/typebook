import { test, TestContext } from 'ava';
import axios from 'axios';
import * as nock from 'nock';
import { Client } from '../../index';
import * as model from '../../lib/model/Model';

const baseURL = 'http://foo.bar:8888';
const client = Client.create(axios.create({baseURL}));

const testSubject = 'test-subject';
const testDescription = 'this is test';

const textPlain = { 'Content-Type': 'text/plain' };
const applicationJson = { 'Content-Type': 'application/json' };

test('createSubject should issue a request POST /subjects/(name: string)', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .post(`/subjects/${testSubject}`, testDescription)
        .reply(201, '0', textPlain);

    const actual: number = await client.createSubject(testSubject, testDescription);
    t.is(actual, 0);
});

test('createSubject should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .post(`/subjects/${testSubject}`, testDescription)
        .reply(201, 'OK', textPlain);

    await t.throws(client.createSubject(testSubject, testDescription));
});

test('getSubject should issue a request GET /subjects/(name: string)', async(t: TestContext) => {
    t.plan(1);

    const expected = new model.Subject(testSubject, testDescription);
    nock(baseURL)
        .replyContentLength()
        .get(`/subjects/${testSubject}`)
        .reply(200, expected, applicationJson);

    const actual = await client.getSubject(testSubject);
    t.deepEqual(actual, expected);
});

test('getSubject should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get(`/subjects/${testSubject}`)
        .reply(200, {name2: testSubject}, applicationJson);

    await t.throws(client.getSubject(testSubject));
});

test('listSubjects should issue a request GET /subjects', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get('/subjects')
        .reply(200, [testSubject, 'test-subject2'], applicationJson);

    const actual = await client.listSubjects();
    t.deepEqual(actual, [testSubject, 'test-subject2']);
});

test('listSubjects should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .get('/subjects')
        .reply(200, 1,  applicationJson);

    await t.throws(client.listSubjects());
});

test('updateDescription should issue a request PUT /subjects/(name: string)/', async(t: TestContext) => {
    t.plan(1);

    const expected = 1;
    nock(baseURL)
        .replyContentLength()
        .put(`/subjects/${testSubject}`, testDescription)
        .reply(200, expected, textPlain);

    const actual = await client.updateDescription(testSubject, testDescription);
    t.is(actual, expected);
});

test('updateDescription should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .put(`/subjects/${testSubject}`, testDescription)
        .reply(200, 'foo', textPlain);

    await t.throws(client.updateDescription(testSubject, testDescription));
});

test('deleteSubject should issue a request DELETE /subjects/(name: string)', async(t: TestContext) => {
    t.plan(1);

    const expected = 1;
    nock(baseURL)
        .replyContentLength()
        .delete(`/subjects/${testSubject}`)
        .reply(200, expected, textPlain);

    const actual = await client.deleteSubject(testSubject);
    t.is(actual, expected);
});

test('deleteSubject should fail against an invalid response', async(t: TestContext) => {
    t.plan(1);

    nock(baseURL)
        .replyContentLength()
        .delete(`/subjects/${testSubject}`)
        .reply(200, 'foo', textPlain);

    await t.throws(client.deleteSubject(testSubject));
});
