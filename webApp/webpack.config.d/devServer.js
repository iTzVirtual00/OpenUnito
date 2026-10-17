config.devServer = Object.assign(
    {},
    config.devServer || {},
    {
        allowedHosts: 'all'
    }
);