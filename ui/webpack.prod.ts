import * as webpack from 'webpack';
import * as merge from 'webpack-merge';
import common from './webpack.common';

const config: webpack.Configuration = merge(common, {
    plugins: [
        new webpack.optimize.UglifyJsPlugin({
            sourceMap: true
        }),
        new webpack.HashedModuleIdsPlugin(),
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': JSON.stringify('production')
        })
    ]
});

export default config;
