config.devServer = config.devServer || {};

// Set port and host
config.devServer.port = 8080;
config.devServer.host = '0.0.0.0';
config.devServer.open = true;

// Attach required COOP/COEP headers
config.devServer.headers = Object.assign(
    {},
    config.devServer.headers || {},
    {
        'Cross-Origin-Opener-Policy': 'same-origin',
        'Cross-Origin-Embedder-Policy': 'require-corp'
    }
);