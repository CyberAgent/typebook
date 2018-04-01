import * as path from 'path';
import * as webpack from 'webpack';
import * as excludeNodeModules from 'webpack-node-externals';
import * as CleanWebpackPlugin from 'clean-webpack-plugin';

const config: webpack.Configuration = {
    context: path.resolve(__dirname),
    externals: [ excludeNodeModules() ],
    entry: {
        app: './index.ts',
    },
    module: {
        rules: [
            { test: /\.ts$/, use: 'awesome-typescript-loader', exclude: ['/node_modules/'] }
        ]
    },
    resolve: {
        extensions: ['.ts', '.js']
    },
    optimization: {
        noEmitOnErrors: true
    },
    plugins: [
        new CleanWebpackPlugin(['dist'])
    ]
};

export default config;
