import * as path from 'path';
import * as webpack from 'webpack';
import * as excludeNodeModules from 'webpack-node-externals';
import * as CleanWebpackPlugin from 'clean-webpack-plugin';

const config: webpack.Configuration = {
    context: path.resolve(__dirname),
    entry: {
        app: './index.ts',
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'index.js',
        libraryTarget: 'commonjs2'
    },
    externals: [ excludeNodeModules() ],
    module: {
        rules: [
            { test: /\.ts$/, use: 'ts-loader' }
        ]
    },
    resolve: {
        extensions: ['.ts', '.js']
    },
    optimization: {
        noEmitOnErrors: true
    },
    plugins: [
        new CleanWebpackPlugin([
            path.resolve(__dirname, 'dist'),
            path.resolve(__dirname, 'test/out')
        ])
    ]
};

export default config;
