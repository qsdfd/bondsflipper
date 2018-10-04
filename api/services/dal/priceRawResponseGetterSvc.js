const httpClientSvc = require('./httpClientSvc.js');
const url = require('url')
const cnsts = require('../../constants');


module.exports.getWebPageResponse = function(){
    return get(getWebPageEndpoint());
}

module.exports.getApiResponse = function (){
    return get(getAPIEndpoint());
}


function get(endpoint){
    return httpClientSvc.get(endpoint);
}

function getWebPageEndpoint(){
    return getEndpoint(
        cnsts.OSBUDDY_GE_WEBPAGE_URL,
        {
            id: cnsts.BOND_ID
        });
}

function getAPIEndpoint(){
    return getEndpoint(
        cnsts.OSBUDDY_GE_API_URL,
        {
            a: 'guidePrice',
            i: cnsts.BOND_ID
        });
}

function getEndpoint(urlStr, query){
    let endpoint = url.parse(urlStr);
    endpoint.query = query;
    return url.format(endpoint);;
}