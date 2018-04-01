import { test, TestContext } from 'ava';
import * as model from '../../lib/model/Model';

test('SemanticVersion#fromString should construct from valid string', (t: TestContext) => {
    t.plan(3);

    const actual: model.SemanticVersion = model.SemanticVersion.fromString('v1.2.3');
    t.is(actual.major, 1);
    t.is(actual.minor, 2);
    t.is(actual.patch, 3);
});

test('SemanticVersion#fromString should fail against a invalid string', (t: TestContext) => {
    t.plan(1);
    t.throws(() => model.SemanticVersion.fromString('1.2.3'));
});
