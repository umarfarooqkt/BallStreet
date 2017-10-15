var path = require('path');

module.exports = {
    entry: {
        index: './src/main/js/index.js',
        leagues : './src/main/js/leagues.js',
        player: './src/main/js/player.js',
        home: './src/main/js/home.js',
        transaction: './src/main/js/transaction.js',
        market: './src/main/js/market.js',
        stocks: './src/main/js/stocks.js',
    },
    output: {
        path: path.join(__dirname, './grails-app/assets/javascripts'),
        publicPath: '/assets/',
        filename: '[name].bundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.js$/,
                include: path.join(__dirname, 'src/main/js'),
                loader: 'babel-loader',
                query: {
                    presets: ['es2015', 'react']
                }
            }
        ]
    }
};