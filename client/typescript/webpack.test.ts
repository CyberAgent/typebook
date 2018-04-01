import * as glob from 'glob';
import * as path from 'path';
import * as webpack from 'webpack';
import * as merge from 'webpack-merge';
import common from './webpack.common';
import * as excludeNodeModules from "webpack-node-externals";

const config: webpack.Configuration = merge(common, {
    context: path.resolve(__dirname),
    entry: glob.sync('./test/**/*.ts', { ignore: glob.sync('./test/out/**/*.js') }),
    externals: [ excludeNodeModules() ],
    output: {
        path: path.resolve(__dirname, 'test/out'),
        filename: 'test.js'
    },
    plugins: [
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': JSON.stringify('development')
        })
    ],
    devtool: 'inline-source-map'
});

export default config;
