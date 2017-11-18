import * as webpack from 'webpack';
import * as merge from 'webpack-merge';
import * as path from "path";
import common from './webpack.common';

const config: webpack.Configuration = merge(common,{
    plugins: [
        new webpack.NamedModulesPlugin(),
        new webpack.DefinePlugin({
            'process.env.NODE_ENV': JSON.stringify('development')
        })
    ],
    devtool: 'inline-source-map',
    devServer: {
        contentBase: path.join(__dirname, 'dist'),
        compress: true,
        hot: true
    }
});

export default config;
