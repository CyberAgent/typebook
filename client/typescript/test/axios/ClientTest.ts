import { test, TestContext } from 'ava';
import axios from 'axios';
import Client from '../../index';

const baseURL = 'http://bar.foo:8888';
const client = Client.create(axios.create({
    baseURL,
    timeout: 2000
}));

test('any request to inexistent host should result in fail', async(t: TestContext) => {
    t.plan(1);
    await t.throws(client.listSubjects());
});
