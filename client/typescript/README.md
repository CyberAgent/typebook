
# TypeScript Client

TypeBook client for TypeScript.

## Installation

```npm
$ npm install --save typebook-client
```

## Usage

You can create Client object as follows:

```typescript
import axios from 'axios';
import { Client } from 'typebook-client';

const client = Client.create(axios.create({
    baseURL: 'http://localhost:8888',
    timeout: 2000
}));
```

As you seen, TypeBook client wraps [Axios](https://github.com/axios/axios) and thus it has Promise based API.
You can interact with TypeBook server as follows:

```typescript
client.createSubject('locations', 'history of user location')
    .then(() => client.listSubjects())
    .then((result) => console.log(result));
```
